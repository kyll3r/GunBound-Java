package br.com.gunbound.emulator.packets.readers.room.gameplay;

import java.util.Map;

import br.com.gunbound.emulator.handlers.GameAttributes;
import br.com.gunbound.emulator.model.entities.game.PlayerSession;
import br.com.gunbound.emulator.room.GameRoom;
import br.com.gunbound.emulator.room.model.enums.GameMode;
import br.com.gunbound.emulator.utils.PacketUtils;
import br.com.gunbound.emulator.utils.Utils;
import br.com.gunbound.emulator.utils.crypto.GunBoundCipher;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;

public class GamePlayerDeadReader {

	private static final int OPCODE_REQUEST = 0x4100;
	private static final int OPCODE_CONFIRMATION = 0x4101;

	public static synchronized void read(ChannelHandlerContext ctx, byte[] payload) {
		System.out.println("RECV> SVC_PLAY_USER_DEAD (0x" + Integer.toHexString(OPCODE_REQUEST) + ")");
		PlayerSession deadPlayer = ctx.channel().attr(GameAttributes.USER_SESSION).get();
		if (deadPlayer == null)
			return;

		GameRoom room = deadPlayer.getCurrentRoom();
		if (room == null)
			return;

		ByteBuf buffer = Unpooled.EMPTY_BUFFER;

		// ByteBuf buffer = Unpooled.buffer();
		// buffer.writeBytes(payload);
		// buffer.writeBytes(new byte[] {(byte)0xFF, (byte)0xFF, (byte)0xFF,
		// (byte)0x03, (byte)0x06, (byte)0x05, (byte)0x05, (byte)0x04, (byte)0x02,
		// (byte)0x03});

		try {
			// 1. Envia a confirmação 0x4101 de volta para o jogador que morreu.
			// A referência mostra um payload vazio e sem RTC.

			//int playerTxSum = deadPlayer.getPlayerCtx().attr(GameAttributes.PACKET_TX_SUM).get();
			ByteBuf confirmationPacket = PacketUtils.generatePacket(deadPlayer, OPCODE_CONFIRMATION, buffer,false);

			ctx.writeAndFlush(confirmationPacket);

			System.out.println("[DEBUG] Morte do Player: " + deadPlayer.getNickName() + " NO SLOT: "
					+ deadPlayer.getCurrentRoom().getSlotPlayer(deadPlayer));

		} catch (Exception e) {
			System.err.println("Erro ao processar morte do jogador:");
			e.printStackTrace();
		}

		
		deadPlayer.setIsAlive(0);
		announceDeadPlayer(room, deadPlayer.getCurrentRoom().getSlotPlayer(deadPlayer), deadPlayer.getRoomTeam(), ctx);

		int winnerTeam = ((deadPlayer.getRoomTeam() == 0) ? 1 : (deadPlayer.getRoomTeam() == 1) ? 0 : 0);
		boolean endGame = false;

		// Baseado no modo de jogo verifica o score
		if (GameMode.fromId(room.getGameMode()).equals(GameMode.SCORE)) {
			int teamPlayer = deadPlayer.getRoomTeam();
			room.setScoreTeam(teamPlayer);
			
			if (!room.isTeamHasScore(teamPlayer)) {
				endGame = true;
			}
		}
		
		
		if(!room.isTeamAlive(deadPlayer.getRoomTeam())) {
			endGame = true;
		}
		
		

		if (endGame) {
			ctx.channel().eventLoop().schedule(() -> {
				// Adiciona Delay proposital para sincronizar o result
				announceFinalScore(room, winnerTeam);
			}, 3500, java.util.concurrent.TimeUnit.MILLISECONDS);
		}

	}

	private static void announceDeadPlayer(GameRoom room, int slotRcv, int deadTeam, ChannelHandlerContext ctx) {
		for (Map.Entry<Integer, PlayerSession> entry : room.getPlayersBySlot().entrySet()) {
			PlayerSession player = entry.getValue();

			// Dados fixos vindos do hex
			// byte[] fixedData = Utils.hexStringToByteArray("1300000001490080BFD201");
			byte[] fixedData = Utils.hexStringToByteArray("13000000000000443447");

			// Novo array com tamanho 1 (slot) + tamanho de fixedData
			byte[] resultBytes = new byte[2 + fixedData.length];

			// Copiar o slot
			resultBytes[0] = (byte) slotRcv;
			resultBytes[resultBytes.length - 1] = (byte) deadTeam;

			// Copiar os dados fixos a partir do índice 1
			System.arraycopy(fixedData, 0, resultBytes, 1, fixedData.length);

			// byte[] resultBytes = Utils.hexStringToByteArray("011300000001490080BFD201");

			// Utils.hexStringToByteArray("011300000000000000000000");
			byte[] encryptedPayload;
			try {
				encryptedPayload = GunBoundCipher.gunboundDynamicEncrypt(resultBytes, player.getUserNameId(),
						player.getPassword(), player.getPlayerCtx().attr(GameAttributes.AUTH_TOKEN).get(), 0x4102);

				// 4. Gera o pacote final e envia
				//int txSum = player.getPlayerCtx().attr(GameAttributes.PACKET_TX_SUM).get();
				ByteBuf finalPacket = PacketUtils.generatePacket(player, 0x4102,
						Unpooled.wrappedBuffer(encryptedPayload),false);

				sendPacketWithEventLoop(player, finalPacket);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}

	private static void announceFinalScore(GameRoom room, int WinnerTeam) {
		for (Map.Entry<Integer, PlayerSession> entry : room.getPlayersBySlot().entrySet()) {

			PlayerSession player = entry.getValue();
			//byte slot = (byte) ((deadTeam == 0) ? 1 : (deadTeam == 1) ? 0 : 0);

			// Dados fixos vindos do hex
			// byte[] fixedData = Utils.hexStringToByteArray("1300000009490080BFD201");
			byte[] fixedData = Utils.hexStringToByteArray("0000000000000000000000");

			// Novo array com tamanho 1 (slot) + tamanho de fixedData
			byte[] resultBytes = new byte[1 + fixedData.length];

			// Copiar o time vencedor
			resultBytes[0] = (byte)WinnerTeam;

			// Copiar os dados fixos a partir do índice 1
			System.arraycopy(fixedData, 0, resultBytes, 1, fixedData.length);

			byte[] encryptedPayload;
			try {
				encryptedPayload = GunBoundCipher.gunboundDynamicEncrypt(resultBytes, player.getUserNameId(),
						player.getPassword(), player.getPlayerCtx().attr(GameAttributes.AUTH_TOKEN).get(), 0x4410);

				// 4. Gera o pacote final e envia
				//int txSum = player.getPlayerCtx().attr(GameAttributes.PACKET_TX_SUM).get();
				ByteBuf finalPacket = PacketUtils.generatePacket(player, 0x4410,
						Unpooled.wrappedBuffer(encryptedPayload),false);

				// Enviando packet
				sendPacketWithEventLoop(player, finalPacket);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}

	private static void sendPacketWithEventLoop(PlayerSession player, ByteBuf finalPacket) {
		player.getPlayerCtx().eventLoop().execute(() -> {
			player.getPlayerCtx().writeAndFlush(finalPacket);
		});
	}

}