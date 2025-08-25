package br.com.gunbound.emulator.packets.readers.room.gameplay;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.mariadb.jdbc.plugin.authentication.standard.ed25519.Utils;

import br.com.gunbound.emulator.handlers.GameAttributes;
import br.com.gunbound.emulator.model.entities.game.PlayerGameResult;
import br.com.gunbound.emulator.model.entities.game.PlayerSession;
import br.com.gunbound.emulator.room.GameRoom;
import br.com.gunbound.emulator.utils.PacketUtils;
import br.com.gunbound.emulator.utils.crypto.GunBoundCipher;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;

public class GameResultReader {

	private static final int OPCODE_REQUEST = 0x4412;
	private static final int OPCODE_CONFIRMATION = 0x4413;
	private static final int OPCODE_RESULT = 0x4415;

	public static void read(ChannelHandlerContext ctx, byte[] payload) {
		System.out.println("RECV> SVC_PLAY_RESULT (0x" + Integer.toHexString(OPCODE_REQUEST) + ")");
		PlayerSession ps = ctx.channel().attr(GameAttributes.USER_SESSION).get();
		if (ps == null)
			return;

		GameRoom room = ps.getCurrentRoom();
		if (room == null)
			return;

		try {
			// 1. Descriptografa o payload do chat
			byte[] authToken = ctx.channel().attr(GameAttributes.AUTH_TOKEN).get();
			byte[] decryptedPayload = GunBoundCipher.gunboundDynamicDecrypt(payload, ps.getUserNameId(),
					ps.getPassword(), authToken, OPCODE_REQUEST);

			System.out.println("ResultGame decrypted >> " + Utils.bytesToHex(decryptedPayload));
			
			
			ctx.channel().eventLoop().schedule(() -> {
				processEndGame(room, decryptedPayload);
			}, 150, java.util.concurrent.TimeUnit.MILLISECONDS);
			
			
			//processEndGame(room, decryptedPayload);

		} catch (Exception e) {
			System.err.println("Erro ao processar pacote ResultGame: " + e.getMessage());
			e.printStackTrace();
		}

	}

	public static void processEndGame(GameRoom room, byte[] payload) {

		ByteBuf rcvPayload = Unpooled.wrappedBuffer(payload);

		int qtdPlayers = rcvPayload.readByte();

		for (int i = 0; i < qtdPlayers; i++) {
			rcvPayload.skipBytes(1);

			int slot = rcvPayload.readByte();
			int gold = rcvPayload.readShortLE();
			int BonusGold = rcvPayload.readShortLE();
			rcvPayload.readShortLE();
			int gp = rcvPayload.readShortLE();
			int BonusGp = rcvPayload.readShortLE();
			rcvPayload.readShortLE();
			rcvPayload.readShortLE();
			rcvPayload.readShortLE();
			
			PlayerGameResult pResult = new PlayerGameResult(gold, BonusGold, gp, BonusGp);
			
			System.out.println("pResult >>> SLOT: " + slot + " Valores: " + pResult);

			room.setResultGameBySlot(slot, pResult);

		}

		rcvPayload.skipBytes(8);
		
		
		//announceScorePlayer(room);

		ByteBuf buffer = Unpooled.EMPTY_BUFFER;

		try {

			for (Map.Entry<Integer, PlayerSession> entry : room.getPlayersBySlot().entrySet()) {
				PlayerSession player = entry.getValue();
				//int playerTxSum = player.getPlayerCtx().attr(GameAttributes.PACKET_TX_SUM).get();
				ByteBuf confirmationPacket = PacketUtils.generatePacket(player, OPCODE_CONFIRMATION, buffer,false);
				player.getPlayerCtx().writeAndFlush(confirmationPacket);

			}

			
			

		} catch (Exception e) {
			System.err.println("Erro ao processar Resultado da partida");
			e.printStackTrace();
		} finally {
			buffer.release();
		}
		
		//announceScorePlayer(room);
		room.isGameStarted(false);// sala deixa de estar em estado playing
	}

	//This is Just used on Season 2 Versions.
	private static void announceScorePlayer(GameRoom room) {

		Collection<PlayerSession> recipients = new ArrayList<>(room.getPlayersBySlot().values());

		ByteBuf buffer = Unpooled.buffer();
		buffer.writeByte(0);
		buffer.writeShortLE(recipients.size());

		for (PlayerSession player : recipients) {

			int slot = player.getCurrentRoom().getSlotPlayer(player);

			PlayerGameResult pResult = room.getResultGameBySlot().get(slot);
			
			System.out.println("announceScorePlayer | pResult >>> SLOT: " + slot + " Valores: " + pResult);

			buffer.writeByte(slot);
			buffer.writeShortLE(pResult.getNormalGold());// NormalGold
			buffer.writeShortLE(0);// Unknown
			buffer.writeShortLE(pResult.getBonusGold());// BonusGold
			buffer.writeShortLE(0);// Unknown
			buffer.writeIntLE(0);// Unknown
			buffer.writeShortLE(pResult.getNormalGp());// NormalGP
			buffer.writeShortLE(pResult.getBonusGp());// BonusGP
			buffer.writeShortLE(0);// Unknown
			buffer.writeByte(0);// gb icon
			buffer.writeByte(0);// Unknown
			buffer.writeByte(100);// Hielo
			buffer.writeByte(0);// kill
			//buffer.writeBytes(new byte[] { (byte) 0x00, (byte) 0x00, (byte) 0x00, 
					//(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00 });

		}

		// Adiciona um padding para garantir o alinhamento de 12 bytes. (Por conta da
		// criptografia)
		int currentSize = buffer.writerIndex();
		int paddingSize = (12 - (currentSize % 12)) % 12; // O % 12 no final lida com o caso de já ser múltiplo.
		if (paddingSize > 0) {
			buffer.writeBytes(new byte[paddingSize]);
		}

		byte[] payloadSend = new byte[buffer.readableBytes()];
		buffer.readBytes(payloadSend);
		buffer.release(); // Libera o buffer temporário.
		
		System.out.println("To Encrpyt: >> " + Utils.bytesToHex(payloadSend));

		for (PlayerSession player : recipients) {

			// Converte o conteúdo do buffer para um array de bytes.

			byte[] encryptedPayload;
			try {
				encryptedPayload = GunBoundCipher.gunboundDynamicEncrypt(payloadSend, player.getUserNameId(),
						player.getPassword(), player.getPlayerCtx().attr(GameAttributes.AUTH_TOKEN).get(), OPCODE_RESULT);

				// Gera o pacote final e envia
				//int txSum = player.getPlayerCtx().attr(GameAttributes.PACKET_TX_SUM).get();
				ByteBuf finalPacket = PacketUtils.generatePacket(player, 0x4415,
						Unpooled.wrappedBuffer(encryptedPayload),false);

				// Enviando packet
				player.getPlayerCtx().eventLoop().execute(() -> {
					player.getPlayerCtx().writeAndFlush(finalPacket);
				});
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		room.isGameStarted(false);// sala deixa de estar em estado playing
	}

}