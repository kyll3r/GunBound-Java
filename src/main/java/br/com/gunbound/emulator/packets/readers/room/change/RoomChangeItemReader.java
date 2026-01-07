package br.com.gunbound.emulator.packets.readers.room.change;

import br.com.gunbound.emulator.handlers.GameAttributes;
import br.com.gunbound.emulator.model.entities.game.PlayerSession;
import br.com.gunbound.emulator.packets.writers.RoomWriter;
import br.com.gunbound.emulator.room.GameRoom;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;

public class RoomChangeItemReader {

	private static final int OPCODE_REQUEST = 0x3102;

	public static void read(ChannelHandlerContext ctx, byte[] payload) {
		System.out.println("RECV> SVC_ROOM_CHANGE_USEITEM (0x" + Integer.toHexString(OPCODE_REQUEST) + ")");
		PlayerSession masterPlayer = ctx.channel().attr(GameAttributes.USER_SESSION).get();
		if (masterPlayer == null)
			return;

		GameRoom room = masterPlayer.getCurrentRoom();
		if (room == null || !masterPlayer.equals(room.getRoomMaster())) {
			return;
		}

		// Empacota toda a lógica em um Runnable e submeta para a fila da sala!
		room.submitAction(() -> processChangeUseItem(payload, masterPlayer, room),ctx);
	}

	private static void processChangeUseItem(byte[] payload, PlayerSession player,
			GameRoom room) {

		ByteBuf request = Unpooled.wrappedBuffer(payload);
		try {
			int newItemState = request.readShortLE();

			// 1. Atualiza o estado na sala
			room.setItemState(newItemState);
			System.out.println(
					"Room " + room.getRoomId() + " item state changed to: 0x" + Integer.toHexString(newItemState));

			// update sem payload com RTC.
			RoomWriter.writeRoomUpdate(player);

			System.out.println("Notificação de atualização da sala (0x3105) enviada para " + room.getPlayerCount()
					+ " jogador(es).");

		} catch (Exception e) {
			System.err.println("Erro ao processar mudança de itens da sala:");
			e.printStackTrace();
		} finally {
			request.release();
		}
	}
}