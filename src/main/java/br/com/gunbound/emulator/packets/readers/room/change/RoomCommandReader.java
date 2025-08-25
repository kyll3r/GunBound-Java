package br.com.gunbound.emulator.packets.readers.room.change;

import java.util.ArrayList;
import java.util.List;

import br.com.gunbound.emulator.handlers.GameAttributes;
import br.com.gunbound.emulator.model.entities.game.PlayerSession;
import br.com.gunbound.emulator.packets.readers.MessageBcmReader;
import br.com.gunbound.emulator.room.GameRoom;
import br.com.gunbound.emulator.room.RoomManager;
import br.com.gunbound.emulator.utils.PacketUtils;
import br.com.gunbound.emulator.utils.Utils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;

public class RoomCommandReader {

	private static final int OPCODE_REQUEST = 0x5100;
	private static final Integer OPCODE_CONFIRMATION_CMD = 0x3FFF;

	public static void read(ChannelHandlerContext ctx, byte[] payload) {
		System.out.println("RECV> GENERIC_COMMAND (0x" + Integer.toHexString(OPCODE_REQUEST) + ")");
		PlayerSession player = ctx.channel().attr(GameAttributes.USER_SESSION).get();

		if (player == null)
			return;

		// Empacota toda a lógica em um Runnable e submeta para a fila da sala!
		processCommand(ctx, payload, player);
	}

	private static void processCommand(ChannelHandlerContext ctx, byte[] payload, PlayerSession player) {

		ByteBuf request = Unpooled.wrappedBuffer(payload).skipBytes(1);

		// 1. O payload inteiro é a string do commando. Usamos o stringDecode.
		String[] commandParts = Utils.stringDecode(request).split(" ", 2);//limita comando em 2 partes
		String command = commandParts[0];
		String paramCmd = commandParts.length > 1 ? commandParts[1] : ""; // Valor padrão vazio caso não haja parâmetro
		System.out.println("COMMAND: " + command);

		if (command.equals("close")) {
			GameRoom room = player.getCurrentRoom();
			// deixa fechar se for Master (alterar para authority > 99
			checkIfaRoomMaster(player, room);
			MessageBcmReader.printMsgToPlayer(player, "The Room Was Closed");
			room.submitAction(() -> closeRoom(player, room));
		} else if (command.equals("bcm")) {
			MessageBcmReader.broadcastSendMessage(paramCmd);
		}else {
			MessageBcmReader.printMsgToPlayer(player, "ADMIN >> Unknown Command");
		}

	}

	private static void closeRoom(PlayerSession player, GameRoom room) {
		// snapshot para evitar concorrência
		List<PlayerSession> recipients = new ArrayList<>(room.getPlayersBySlot().values());
		for (PlayerSession playerInRoom : recipients) {
			RoomManager.getInstance().handlePlayerLeave(playerInRoom);

			ByteBuf confirmationPacket = PacketUtils.generatePacket(playerInRoom, OPCODE_CONFIRMATION_CMD,
					Unpooled.EMPTY_BUFFER, false);

			playerInRoom.getPlayerCtx().writeAndFlush(confirmationPacket)
					.addListener((ChannelFutureListener) future -> {
						if (!future.isSuccess()) {
							System.err.println("Falha ao fechar sala para: " + playerInRoom.getNickName());
							future.cause().printStackTrace();
							// Caso o jogador nao esteja impossibilitado de receber pacotes.
							playerInRoom.getPlayerCtx().close();
						} else {
							System.out.println("RoomID: " + (room.getRoomId() + 1) + ", Command Sent: '0x"
									+ Integer.toHexString(OPCODE_CONFIRMATION_CMD) + "'");
							// update sem payload com RTC.
							//RoomWriter.writeRoomUpdate(playerInRoom);
						}
					});
		}
	}

	// logica para verificar se é um GM.
	private static void checkIfaRoomMaster(PlayerSession player, GameRoom room) {
		if (room == null || !player.equals(room.getRoomMaster())) {
			// Apenas o dono da sala pode fechar ela.
			return;
		}
	}

}