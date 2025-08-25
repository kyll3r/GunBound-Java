package br.com.gunbound.emulator.packets.readers.room.gameplay;

import java.util.ArrayList;
import java.util.List;

import org.mariadb.jdbc.plugin.authentication.standard.ed25519.Utils;

import br.com.gunbound.emulator.handlers.GameAttributes;
import br.com.gunbound.emulator.model.entities.game.PlayerSession;
import br.com.gunbound.emulator.room.GameRoom;
import br.com.gunbound.emulator.utils.PacketUtils;
import br.com.gunbound.emulator.utils.crypto.GunBoundCipher;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;

public class GameResultEndJewel {

	private static final int OPCODE_REQUEST = 0x4200;
	private static final int OPCODE_CONFIRMATION = 0x4410;

	public static void read(ChannelHandlerContext ctx, byte[] payload) {
		System.out.println("RECV> SVC_PLAY_END_JEWEL (0x" + Integer.toHexString(OPCODE_REQUEST) + ")");
		PlayerSession player = ctx.channel().attr(GameAttributes.USER_SESSION).get();
		if (player == null)
			return;

		GameRoom room = player.getCurrentRoom();
		if (room == null)
			return;

		// Calcula o próximo múltiplo de 16
		int paddingLength = 16 - (payload.length % 16);
		if (paddingLength != 16) { // Se já for múltiplo de 16, não precisa preencher
			byte[] padded = new byte[payload.length + paddingLength];
			System.arraycopy(payload, 0, padded, 0, payload.length);
			payload = padded; // agora payload está com padding
		}

		try {
			// 1. Descriptografa o payload do chat
			byte[] authToken = ctx.channel().attr(GameAttributes.AUTH_TOKEN).get();
			byte[] decryptedPayload = GunBoundCipher.gunboundDynamicDecrypt(payload, player.getUserNameId(),
					player.getPassword(), authToken, OPCODE_REQUEST);

			// Empacota toda a lógica em um Runnable e submeta para a fila da sala!
			//room.submitAction(() -> processEndJewel(decryptedPayload, room));
			
			ctx.channel().eventLoop().schedule(() -> {
			//Adiciona Delay proposital para sincronizar o result
				processEndJewel(decryptedPayload, room);
			}, 2000, java.util.concurrent.TimeUnit.MILLISECONDS);

		} catch (Exception e) {
			System.err.println("Erro ao processar pacote de partida jewel: " + e.getMessage());
			e.printStackTrace();
		}
	}

	private static void processEndJewel(byte[] payload, GameRoom room) {
		System.out.println("DEBUG(TO ENCRYPT) processEndJewel >>>" + Utils.bytesToHex(payload));

		List<PlayerSession> recipients = new ArrayList<>(room.getPlayersBySlot().values());

		for (PlayerSession player : recipients) {

			byte[] encryptedPayload;

			try {

				encryptedPayload = GunBoundCipher.gunboundDynamicEncrypt(payload, player.getUserNameId(),
						player.getPassword(), player.getPlayerCtx().attr(GameAttributes.AUTH_TOKEN).get(),
						OPCODE_CONFIRMATION);

				// 4. Gera o pacote final e envia
				//int txSum = player.getPlayerCtx().attr(GameAttributes.PACKET_TX_SUM).get();
				ByteBuf finalPacket = PacketUtils.generatePacket(player, OPCODE_CONFIRMATION,
						Unpooled.wrappedBuffer(encryptedPayload),false);

				// Enviando packet
				player.getPlayerCtx().eventLoop().execute(() -> {
					player.getPlayerCtx().writeAndFlush(finalPacket);
				});

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

	}
}