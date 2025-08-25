package br.com.gunbound.emulator.packets.readers;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;

import br.com.gunbound.emulator.handlers.GameAttributes;
import br.com.gunbound.emulator.model.entities.game.PlayerSession;
import br.com.gunbound.emulator.model.entities.game.PlayerSessionManager;
import br.com.gunbound.emulator.utils.PacketUtils;
import br.com.gunbound.emulator.utils.crypto.GunBoundCipher;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;

public class MessageBcmReader {

	private static final int OPCODE_MSG_BCM_REQUEST = 0x5010; //
	private static final int OPCODE_MSG_BCM_RESPONSE = 0x5101; //

	public static void read(ChannelHandlerContext ctx, byte[] payload) {
		System.out.println("RECV> SVC_BCM_MSG (0x" + Integer.toHexString(OPCODE_MSG_BCM_REQUEST) + ")");
		PlayerSession session = ctx.channel().attr(GameAttributes.USER_SESSION).get();
		if (session == null || session.getCurrentRoom() == null) {
			return; // Jogador não está logado ou não está em uma sala (Possivel Hack)
		}

		try {
			// 1. Descriptografa o payload do chat
			byte[] authToken = ctx.channel().attr(GameAttributes.AUTH_TOKEN).get();
			byte[] decryptedPayload = GunBoundCipher.gunboundDynamicDecrypt(payload, session.getUserNameId(),
					session.getPassword(), authToken, OPCODE_MSG_BCM_REQUEST);

			// 2. Decodifica a mensagem (formato: 1 byte de tamanho + N bytes de mensagem)
			int messageLength = decryptedPayload[1] & 0xFF;
			String chatMessage = new String(decryptedPayload, 2, messageLength, StandardCharsets.ISO_8859_1);

			System.out.println("[BCM] " + session.getNickName() + ": " + chatMessage);

			// logs para depuração
			System.out.println("[DEBUG] Iniciando broadcast de: " + session.getNickName());
			
			// 3. Pede ao canal atual do jogador para fazer o broadcast da mensagem
			broadcastSendMessage(chatMessage);

		} catch (Exception e) {
			System.err.println("Erro ao processar pacote de chat: " + e.getMessage());
			e.printStackTrace();
		}
	}

	public static void broadcastSendMessage(String message) {
		// Itera sobre todos os jogadores no lobby
		// snapshot para evitar concorrência
		Collection<PlayerSession> recipients = new ArrayList<>(PlayerSessionManager.getInstance().getAllPlayers());
		for (PlayerSession recipient : recipients) {
			try {
				
				printMsgToPlayer(recipient, message);

			} catch (Exception e) {
				System.err.println(
						"Falha ao enviar bcm para " + recipient.getNickName() + ": " + e.getMessage());
			}
		}
	}
	
	
	/**
	 * Gera uma mensagem para que determinado player visualize (Usado no BCM)
	 * 
	 * @param player
	 * @param msg
	 */
	public static void printMsgToPlayer(PlayerSession player, String msg) {

		byte[] messageBytes = msg.getBytes(StandardCharsets.ISO_8859_1);
		ByteBuf buffer = Unpooled.wrappedBuffer(messageBytes);

		ByteBuf confirmationPacket = PacketUtils.generatePacket(player, OPCODE_MSG_BCM_RESPONSE, buffer,false);

		// Envia o pacote individualmente.
		player.getPlayerCtx().eventLoop().execute(() -> {
			player.getPlayerCtx().writeAndFlush(confirmationPacket);
		});

	}
	
	
	
}