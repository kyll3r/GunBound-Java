package br.com.gunbound.emulator.packets.readers.room.change;

import br.com.gunbound.emulator.handlers.GameAttributes;
import br.com.gunbound.emulator.model.entities.game.PlayerSession;
import br.com.gunbound.emulator.packets.writers.RoomWriter;
import br.com.gunbound.emulator.room.GameRoom;
import br.com.gunbound.emulator.utils.Utils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;

public class RoomChangeTitleReader {

	private static final int OPCODE_REQUEST = 0x3104;

	public static void read(ChannelHandlerContext ctx, byte[] payload) {
		System.out.println("RECV> SVC_ROOM_CHANGE_TITLE (0x" + Integer.toHexString(OPCODE_REQUEST) + ")");
		PlayerSession player = ctx.channel().attr(GameAttributes.USER_SESSION).get();
		if (player == null)
			return;

		GameRoom room = player.getCurrentRoom();
		if (room == null || !player.equals(room.getRoomMaster())) {
			// Apenas o dono da sala pode mudar o título.
			return;
		}

		// Empacota toda a lógica em um Runnable e submeta para a fila da sala!
		room.submitAction(() -> processChangeTitle(payload, player, room));
	}

	private static void processChangeTitle(byte[] payload, PlayerSession player,
			GameRoom room) {

		ByteBuf request = Unpooled.wrappedBuffer(payload);

		// 1. O payload inteiro é a string do novo título. Usamos o stringDecode.
		String newTitle = Utils.stringDecode(request);

		// 2. Atualiza o título na instância da sala.
		room.setTitle(newTitle);
		System.out.println("RoomID: " + room.getRoomId() + ", novo título da sala: '" + newTitle + "'");

		// update sem payload com RTC.
		RoomWriter.writeRoomUpdate(player);

	}

}