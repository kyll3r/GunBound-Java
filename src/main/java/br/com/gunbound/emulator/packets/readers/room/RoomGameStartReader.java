package br.com.gunbound.emulator.packets.readers.room;

import br.com.gunbound.emulator.handlers.GameAttributes;
import br.com.gunbound.emulator.model.entities.game.PlayerSession;
import br.com.gunbound.emulator.room.GameRoom;
import io.netty.channel.ChannelHandlerContext;

public class RoomGameStartReader {
	public static void read(ChannelHandlerContext ctx, byte[] payload) {
		PlayerSession player = ctx.channel().attr(GameAttributes.USER_SESSION).get();
		GameRoom room = player.getCurrentRoom();

		if (player == null || player.getCurrentRoom() == null)
			return;

		// Empacota toda a lógica em um Runnable e submeta para a fila da sala!
		processStartGame(ctx, payload, player, room);
	}

	private static void processStartGame(ChannelHandlerContext ctx, byte[] payload, PlayerSession player,
			GameRoom room) {

		// Apenas o dono da sala pode iniciar
		if (player.equals(player.getCurrentRoom().getRoomMaster())) {
			player.getCurrentRoom().startGame(payload);
		}
	}
}