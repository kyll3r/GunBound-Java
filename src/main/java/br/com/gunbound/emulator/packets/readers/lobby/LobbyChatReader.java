package br.com.gunbound.emulator.packets.readers.lobby;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;

import br.com.gunbound.emulator.handlers.GameAttributes;
import br.com.gunbound.emulator.model.entities.game.PlayerSession;
import br.com.gunbound.emulator.utils.PacketUtils;
import br.com.gunbound.emulator.utils.Utils;
import br.com.gunbound.emulator.utils.crypto.GunBoundCipher;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;

public class LobbyChatReader {

	private static final int OPCODE_INCOMING_CHAT = 0x2010; // Opcode comum para chat recebido

	public static void read(ChannelHandlerContext ctx, byte[] payload) {
		PlayerSession session = ctx.channel().attr(GameAttributes.USER_SESSION).get();
		if (session == null || session.getCurrentLobby() == null) {
			return; // Jogador não está logado ou não está num canal
		}

		try {
			// 1. Descriptografa o payload do chat
			byte[] authToken = ctx.channel().attr(GameAttributes.AUTH_TOKEN).get();
			byte[] decryptedPayload = GunBoundCipher.gunboundDynamicDecrypt(payload, session.getUserNameId(),
					session.getPassword(), authToken, OPCODE_INCOMING_CHAT);

			// 2. Decodifica a mensagem (formato: 1 byte de tamanho + N bytes de mensagem)
			int messageLength = decryptedPayload[0] & 0xFF;
			String chatMessage = new String(decryptedPayload, 1, messageLength, StandardCharsets.ISO_8859_1);

			System.out.println("[CHAT] " + session.getNickName() + ": " + chatMessage);

			// 3. Pede ao canal atual do jogador para fazer o broadcast da mensagem
			broadcastChatMessage(session, chatMessage);

		} catch (Exception e) {
			System.err.println("Erro ao processar pacote de chat: " + e.getMessage());
			e.printStackTrace();
		}
	}

	private static void broadcastChatMessage(PlayerSession sender, String message) {
		final int OPCODE_CHAT_BROADCAST = 0x201F;

		// logs para depuração
		System.out.println("[DEBUG] Iniciando broadcastChatMessage de: " + sender.getNickName() + " no Canal ID: "
				+ sender.getCurrentLobby().getId());

		// 1. Cria o payload do pacote de chat uma única vez. Agora ele é um byte[].
		byte[] chatPayload = writeChannelChat(sender, message);

		// 2. Itera sobre todos os jogadores no lobby
		// snapshot para evitar concorrência
		Collection<PlayerSession> recipients = new ArrayList<>(sender.getCurrentLobby().getPlayersInLobby().values());
		for (PlayerSession recipient : recipients) {
			//for (PlayerSession recipient : sender.getCurrentLobby().getPlayersInLobby().values()) {
			try {
				// 3. Encripta o payload para cada destinatário.
				byte[] authToken = recipient.getPlayerCtxChannel().attr(GameAttributes.AUTH_TOKEN).get();

				// Enviamos aqui o chatPayload (byte[]) diretamente para a função de
				// encriptação.
				byte[] encryptedPayload = GunBoundCipher.gunboundDynamicEncrypt(chatPayload, recipient.getUserNameId(),
						recipient.getPassword(), authToken, OPCODE_CHAT_BROADCAST);

				// 4. Gera o pacote final e envia
				//int txSum = recipient.getPlayerCtx().attr(GameAttributes.PACKET_TX_SUM).get();
				ByteBuf finalPacket = PacketUtils.generatePacket(recipient, OPCODE_CHAT_BROADCAST,
						Unpooled.wrappedBuffer(encryptedPayload),false);

				System.out.println("[DEBUG] Mensagem de: " + sender.getNickName() + " sendo enviada para : "
						+ recipient.getNickName() + " no Canal ID: " + sender.getCurrentLobby().getId());

				recipient.getPlayerCtxChannel().eventLoop().execute(() -> {
					recipient.getPlayerCtxChannel().writeAndFlush(finalPacket);
				});

			} catch (Exception e) {
				System.err.println(
						"Falha ao enviar broadcast de chat para " + recipient.getNickName() + ": " + e.getMessage());
			}
		}
	}

	private static byte[] writeChannelChat(PlayerSession sender, String message) {
		// ByteBuf temporário para a construção do pacote (facilitador).
		ByteBuf buffer = Unpooled.buffer();
		byte[] messageBytes = message.getBytes(StandardCharsets.ISO_8859_1);

		buffer.writeByte(sender.getChannelPosition());
		buffer.writeBytes(Utils.resizeBytes(sender.getNickName().getBytes(StandardCharsets.ISO_8859_1), 12));
		buffer.writeByte(messageBytes.length);
		buffer.writeBytes(messageBytes);

		// Adiciona um padding para garantir o alinhamento de 12 bytes. (Por conta da
		// criptografia)
		int currentSize = buffer.writerIndex();
		int paddingSize = (12 - (currentSize % 12)) % 12; // O % 12 no final lida com o caso de já ser múltiplo.
		if (paddingSize > 0) {
			buffer.writeBytes(new byte[paddingSize]);
		}

		// Converte o conteúdo do buffer para um array de bytes.
		byte[] payload = new byte[buffer.readableBytes()];
		buffer.readBytes(payload);
		buffer.release(); // Liberta o buffer temporário.

		return payload;
	}
}