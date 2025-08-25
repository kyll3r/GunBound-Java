package br.com.gunbound.emulator.packets.readers.room.gameplay;

import br.com.gunbound.emulator.handlers.GameAttributes;
import br.com.gunbound.emulator.model.entities.game.PlayerSession;
import br.com.gunbound.emulator.utils.PacketUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;

public class GameReturnRoomResultReader {

	private static final int OPCODE_REQUEST = 0x3232;
	private static final int OPCODE_CONFIRMATION = 0x3233;

	public static void read(ChannelHandlerContext ctx, byte[] payload) {
		System.out.println("RECV> SVC_ROOM_RETURN_RESULT (0x" + Integer.toHexString(OPCODE_REQUEST) + ")");
		PlayerSession player = ctx.channel().attr(GameAttributes.USER_SESSION).get();
		if (player == null)
			return;

	

			// 3. Envia um pacote de confirmação (0x3233) com rtc=0.
			//int playerTxSum = player.getPlayerCtx().attr(GameAttributes.PACKET_TX_SUM).get();
			ByteBuf confirmationPacket = PacketUtils.generatePacket(player, OPCODE_CONFIRMATION,
					Unpooled.EMPTY_BUFFER, true);
			ctx.writeAndFlush(confirmationPacket);
	
	}
}