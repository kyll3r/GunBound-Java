package br.com.gunbound.emulator.packets.readers;

import br.com.gunbound.emulator.handlers.GameAttributes;
import br.com.gunbound.emulator.model.entities.game.PlayerSession;
import br.com.gunbound.emulator.utils.PacketUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;

public class GoldUpdateReader {

	private static final int OPCODE_GOLD_UPDATE = 0x6104;
	private static final int OPCODE_GOLD_SUCCESS = 0x6105;

	public static void read(ChannelHandlerContext ctx, byte[] payload) {
		System.out.println("RECV> SVC_GOLD_UPDATE (0x" + Integer.toHexString(OPCODE_GOLD_UPDATE) + ")");
		goldUpdateWriter(ctx);//chama o metodo de escrita, goldUpdateWriter 
		//tb será chamado ao comprar avatares no shop

	}

	public static void goldUpdateWriter(ChannelHandlerContext ctx) {
		System.out.println("SEND> SVC_GOLD_UPDATE (0x" + Integer.toHexString(OPCODE_GOLD_UPDATE) + ")");
		PlayerSession session = ctx.channel().attr(GameAttributes.USER_SESSION).get();

		//int currentTxSum = ctx.channel().attr(GameAttributes.PACKET_TX_SUM).get();

		try {

			ByteBuf successPacket = Unpooled.buffer();
			successPacket.writeLongLE(session.getGold());
			
			successPacket = PacketUtils.generatePacket(session, OPCODE_GOLD_SUCCESS, successPacket,true);

			// Enviando packet
			ctx.writeAndFlush(successPacket);

			System.out.println("Comando de GOLD_UPDATE enviado com sucesso para " + session.getNickName());

		} catch (Exception e) {
			System.err.println("Erro fatal ao decodificar o pacote do GOLD_UPDATE");
			e.printStackTrace();
			ctx.close();
		}


	}

}
