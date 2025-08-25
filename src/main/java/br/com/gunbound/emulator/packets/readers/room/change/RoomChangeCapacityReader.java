package br.com.gunbound.emulator.packets.readers.room.change;

import br.com.gunbound.emulator.handlers.GameAttributes;
import br.com.gunbound.emulator.model.entities.game.PlayerSession;
import br.com.gunbound.emulator.packets.writers.RoomWriter;
import br.com.gunbound.emulator.room.GameRoom;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;

public class RoomChangeCapacityReader {

	private static final int OPCODE_REQUEST = 0x3103;

	public static void read(ChannelHandlerContext ctx, byte[] payload) {
		System.out.println("RECV> SVC_ROOM_CHANGE_MAXMEN (0x" + Integer.toHexString(OPCODE_REQUEST) + ")");
		PlayerSession player = ctx.channel().attr(GameAttributes.USER_SESSION).get();
		if (player == null)
			return;

		GameRoom room = player.getCurrentRoom();
		if (room == null || !player.equals(room.getRoomMaster())) {
			// Apenas o dono da sala pode mudar a capacidade.
			return;
		}

		// Empacota toda a lógica em um Runnable e submeta para a fila da sala!
		room.submitAction(() -> processChangeMaxMen(payload, player, room));
	}

	private static void processChangeMaxMen(byte[] payload, PlayerSession player,
			GameRoom room) {

		// pega nova capacidade
		ByteBuf request = Unpooled.wrappedBuffer(payload);
		// 1. A nova capacidade é o primeiro byte do payload.
		int newCapacity = request.readByte();

		// 2. Atualiza a capacidade na instância da sala.
		room.setCapacity(newCapacity);
		System.out.println("RoomID: " + room.getRoomId() + ", nova capacidade da sala: " + newCapacity);

		// update sem payload com RTC.
		RoomWriter.writeRoomUpdate(player);

	}
}