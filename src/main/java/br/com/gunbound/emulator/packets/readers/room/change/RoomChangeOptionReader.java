package br.com.gunbound.emulator.packets.readers.room.change;

import br.com.gunbound.emulator.handlers.GameAttributes;
import br.com.gunbound.emulator.model.entities.game.PlayerSession;
import br.com.gunbound.emulator.packets.writers.RoomWriter;
import br.com.gunbound.emulator.room.GameRoom;
import br.com.gunbound.emulator.room.model.enums.GameMode;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;

public class RoomChangeOptionReader {

	private static final int OPCODE_REQUEST = 0x3101;

	public static void read(ChannelHandlerContext ctx, byte[] payload) {
		System.out.println("RECV> SVC_ROOM_CHANGE_OPTION (0x" + Integer.toHexString(OPCODE_REQUEST) + ")");
		PlayerSession player = ctx.channel().attr(GameAttributes.USER_SESSION).get();
		if (player == null)
			return;

		GameRoom room = player.getCurrentRoom();
		if (room == null || !player.equals(room.getRoomMaster())) {
			return;
		}

		// Empacota toda a lógica em um Runnable e submeta para a fila da sala!
		room.submitAction(() -> processChangeOption(payload, player, room));
	}

	private static void processChangeOption(byte[] payload, PlayerSession player,
			GameRoom room) {

		ByteBuf buffer = Unpooled.wrappedBuffer(payload);
		int config = buffer.readIntLE();

		System.out.println("[DEBUG] RoomChangeOptionReader: " + config);
		room.setGameSettings(config);
		System.out.println("Room " + room.getRoomId() + " opções de jogo alteradas.");
		
		
		short gameModeId = (short) (config >> 16);
		room.setGameMode(gameModeId);

		// update sem payload com RTC.
		RoomWriter.writeRoomUpdate(player);
	}
}