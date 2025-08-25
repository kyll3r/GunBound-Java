package br.com.gunbound.emulator.packets.readers.room;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import br.com.gunbound.emulator.handlers.GameAttributes;
import br.com.gunbound.emulator.model.entities.game.PlayerSession;
import br.com.gunbound.emulator.room.GameRoom;
import br.com.gunbound.emulator.room.RoomManager;
import br.com.gunbound.emulator.utils.PacketUtils;
import br.com.gunbound.emulator.utils.Utils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;

public class RoomDetailReader {

	private static final int OPCODE_REQUEST = 0x2104;
	private static final int OPCODE_RESPONSE = 0x2105;

	public static void read(ChannelHandlerContext ctx, byte[] payload) {
		System.out.println("RECV> SVC_ROOM_DETAIL (0x" + Integer.toHexString(OPCODE_REQUEST) + ")");
		PlayerSession player = ctx.channel().attr(GameAttributes.USER_SESSION).get();
		if (player == null)
			return;

		ByteBuf request = Unpooled.wrappedBuffer(payload);
		try {
			// 1. O ID da sala solicitada são os 2 bytes do payload.
			int requestedRoomId = request.readShortLE();

			// 2. Encontra a sala no RoomManager.
			GameRoom room = RoomManager.getInstance().getRoomById(requestedRoomId);

			if (room == null) {
				System.err.println("O jogador " + player.getNickName() + " solicitou detalhes de uma sala inválida: "
						+ requestedRoomId);
				// O cliente geralmente lida com isso não recebendo resposta, mas um pacote de
				// erro poderia ser enviado.
				return;
			}

			System.out.println("Encontrada sala " + requestedRoomId + " para detalhes.");

			// 3. Constrói e envia o pacote de resposta detalhado.
			//int playerTxSum = player.getPlayerCtx().attr(GameAttributes.PACKET_TX_SUM).get();

			// O RoomWriter constrói o payload detalhado.
			ByteBuf responsePayload = writeRoomDetail(room);

			// O pacote de resposta 0x2105 usa RTC.
			ByteBuf responsePacket = PacketUtils.generatePacket(player, OPCODE_RESPONSE, responsePayload, true);

			ctx.writeAndFlush(responsePacket);

		} catch (Exception e) {
			System.err.println("Erro ao processar detalhes da sala:");
			e.printStackTrace();
		} finally {
			request.release();
		}
	}

	/**
	 * Constrói o payload detalhado de uma única sala para o opcode 0x2105.
	 *
	 * @param room A sala para a qual os detalhes serão escritos.
	 * @return um ByteBuf com o payload pronto.
	 */
	public static ByteBuf writeRoomDetail(GameRoom room) {
		ByteBuf buffer = Unpooled.buffer();

		// 1. Escreve as informações gerais da sala (semelhante ao RoomList)
		byte[] titleBytes = room.getTitle().getBytes(StandardCharsets.ISO_8859_1);
		buffer.writeByte(titleBytes.length);
		buffer.writeBytes(titleBytes);

		buffer.writeByte(room.getMapId());
		buffer.writeIntLE(room.getGameSettings());
		buffer.writeBytes(new byte[] { 01 });// tem pu talvez?
		buffer.writeByte(room.getPlayerCount());
		buffer.writeByte(room.getCapacity());

		buffer.writeByte(room.isGameStarted() ? 1 : 0);

		buffer.writeByte(room.isPrivate() ? 1 : 0);

		// 2. Itera sobre os jogadores na sala para adicionar seus detalhes
		for (Map.Entry<Integer, PlayerSession> entry : room.getPlayersBySlot().entrySet()) {
			PlayerSession roomPlayer = entry.getValue();
			buffer.writeBytes(Utils.resizeBytes(roomPlayer.getNickName().getBytes(StandardCharsets.ISO_8859_1), 12));
			buffer.writeBytes(new byte[] { (byte)0x00});// gender?
			//buffer.writeByte(roomPlayer.getRoomTeam());

			buffer.writeBytes(Utils.resizeBytes(roomPlayer.getGuild().getBytes(StandardCharsets.ISO_8859_1), 8));

			//buffer.writeShortLE(roomPlayer.getRankCurrent());
			//buffer.writeShortLE(roomPlayer.getRankSeason());
			
			buffer.writeIntLE(1);

		}

		return buffer;
	}

	public static ByteBuf writeRoomDetail() {
		ByteBuf buffer = Unpooled.buffer();

		buffer.writeBytes(new byte[] { (byte) 0x01, (byte) 0x20, (byte) 0x00, (byte) 0xB2, (byte) 0x62, (byte) 0x04,
				(byte) 0x00, (byte) 0x01, (byte) 0x01, (byte) 0x06, (byte) 0x00, (byte) 0x00, (byte) 0x52, (byte) 0x65,
				(byte) 0x44, (byte) 0x79, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, 0x00, (byte) 0x00,
				(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
				(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0xFC, (byte) 0xFF, (byte) 0xFC, (byte) 0xFF,
				(byte) 0xFC, (byte) 0xFF, (byte) 0x1C, (byte) 0x00 });

		return buffer;
	}
}