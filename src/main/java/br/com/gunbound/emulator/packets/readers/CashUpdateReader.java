package br.com.gunbound.emulator.packets.readers;

import br.com.gunbound.emulator.handlers.GameAttributes;
import br.com.gunbound.emulator.model.entities.game.PlayerSession;
import br.com.gunbound.emulator.utils.PacketUtils;
import br.com.gunbound.emulator.utils.Utils;
import br.com.gunbound.emulator.utils.crypto.GunBoundCipher;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;

public class CashUpdateReader {

	private static final int OPCODE_CASH_UPDATE = 0x6100;
	private static final int OPCODE_CASH_SUCCESS = 0x1032;

	public static void read(ChannelHandlerContext ctx, byte[] payload) {
		System.out.println("RECV> SVC_CASH_UPDATE (0x" + Integer.toHexString(OPCODE_CASH_UPDATE) + ")");
		PlayerSession creator = ctx.channel().attr(GameAttributes.USER_SESSION).get();

			int cashAmount = creator.getCash();
			//int currentTxSum = ctx.channel().attr(GameAttributes.PACKET_TX_SUM).get();

			try {

				byte[] cashBytes = GunBoundCipher.intToBytesLE(cashAmount, 4);
				ByteBuf successPacket = Unpooled.buffer();
				byte[] paddedPayload = new byte[12];
				System.arraycopy(cashBytes, 0, paddedPayload, 0, 4);

				// Envia o pacote encriptado.
				successPacket = Utils.toByteBuf(paddedPayload);
				successPacket = PacketUtils.generatePacket(creator, OPCODE_CASH_SUCCESS, successPacket,false);

				// Enviando packet
				ctx.writeAndFlush(successPacket);

				System.out.println("Comando de CASH_UPDATE enviado com sucesso para " + creator.getNickName());

			} catch (Exception e) {
				System.err.println("Erro fatal ao decodificar o pacote do CASH_UPDATE");
				e.printStackTrace();
				ctx.close();
			} 
			
			GoldUpdateReader.goldUpdateWriter(ctx);//atualizar gold
		} 
	
		
}
