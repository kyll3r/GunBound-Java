package br.com.gunbound.emulator.packets.readers.room.change;

import br.com.gunbound.emulator.handlers.GameAttributes;
import br.com.gunbound.emulator.model.entities.game.PlayerSession;
import br.com.gunbound.emulator.room.GameRoom;
import br.com.gunbound.emulator.utils.PacketUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;

public class RoomUserReadyReader {

	private static final int OPCODE_REQUEST = 0x3230;
	private static final int OPCODE_CONFIRMATION = 0x3231;

	public static void read(ChannelHandlerContext ctx, byte[] payload) {
		System.out.println("RECV> SVC_ROOM_USER_READY (0x" + Integer.toHexString(OPCODE_REQUEST) + ")");
		PlayerSession player = ctx.channel().attr(GameAttributes.USER_SESSION).get();
		if (player == null)
			return;

		GameRoom room = player.getCurrentRoom();
		if (room == null) {
			return;
		}
		
		
		// Empacota toda a lógica em um Runnable e submeta para a fila da sala!
		room.submitAction(() -> processReadyStatus(payload, player, room));
	}

	private static void processReadyStatus(byte[] payload, PlayerSession player,
			GameRoom room) {

		ByteBuf request = Unpooled.wrappedBuffer(payload);
		try {
			// 1. O estado de "pronto" (0 ou 1) é o primeiro byte.
			boolean isReady = request.readUnsignedByte() == 1;

			// Encontra o slot do jogador para atualizar o status corretamente.
			int playerSlot = -1;
			for (var entry : room.getPlayersBySlot().entrySet()) {
				if (entry.getValue().equals(player)) {
					playerSlot = entry.getKey();
					break;
				}
			}

			if (playerSlot != -1) {
				// 2. Atualiza o estado na sala.
				room.setPlayerReady(playerSlot, isReady);
				System.out.println(player.getNickName() + " está " + (isReady ? "Pronto" : "Não Pronto"));
			}

			// 3. Envia um pacote de confirmação (0x3231) com rtc=0.
			//int playerTxSum = player.getPlayerCtx().attr(GameAttributes.PACKET_TX_SUM).get();
			ByteBuf confirmationPacket = PacketUtils.generatePacket(player, OPCODE_CONFIRMATION,
					Unpooled.wrappedBuffer(new byte[] {0x00,0x00}), true);
			player.getPlayerCtx().writeAndFlush(confirmationPacket);


		} catch (Exception e) {
			System.err.println("Erro ao processar estado de 'Pronto':");
			e.printStackTrace();
		} finally {
			request.release();
		}
	}
}