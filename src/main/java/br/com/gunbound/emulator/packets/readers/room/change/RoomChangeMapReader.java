package br.com.gunbound.emulator.packets.readers.room.change;

import br.com.gunbound.emulator.handlers.GameAttributes;
import br.com.gunbound.emulator.model.entities.game.PlayerSession;
import br.com.gunbound.emulator.packets.writers.RoomWriter;
import br.com.gunbound.emulator.room.GameRoom;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;

public class RoomChangeMapReader {

	private static final int OPCODE_REQUEST = 0x3100;

	public static void read(ChannelHandlerContext ctx, byte[] payload) {
		System.out.println("RECV> SVC_ROOM_CHANGE_STAGE (0x" + Integer.toHexString(OPCODE_REQUEST) + ")");
		PlayerSession player = ctx.channel().attr(GameAttributes.USER_SESSION).get();
		if (player == null)
			return;

		GameRoom room = player.getCurrentRoom();
		if (room == null || !player.equals(room.getRoomMaster())) {
			// Apenas o dono da sala pode mudar o mapa.
			return;
		}

		// Empacota toda a lógica em um Runnable e submeta para a fila da sala!
		room.submitAction(() -> processChangeStage(payload, player, room));
	}

	private static void processChangeStage(byte[] payload, PlayerSession player,
			GameRoom room) {

		ByteBuf request = Unpooled.wrappedBuffer(payload);

		// 1. O ID do mapa é o primeiro byte do payload.
		int newMapId = request.readUnsignedByte();

		// 2. Atualiza o mapa na instância da sala.
		room.setMapId(newMapId);
		System.out.println("RoomID: " + room.getRoomId() + ", mapa alterado para " + newMapId);

		// update sem payload com RTC.
		RoomWriter.writeRoomUpdate(player);

	}
}