package br.com.gunbound.emulator.packets.readers;

import java.util.Arrays;

import br.com.gunbound.emulator.handlers.GameAttributes;
import br.com.gunbound.emulator.utils.AuthTokenUtils;
import br.com.gunbound.emulator.utils.PacketUtils;
import br.com.gunbound.emulator.utils.Utils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;

public class AuthReader {
	public static void read(ChannelHandlerContext ctx, byte[] payload) {
		int currentTxSum = ctx.channel().attr(GameAttributes.PACKET_TX_SUM).get();
		// Gera um Int Randomico para gerar o AuthToken
		// authToken = Utils.randomIntNumber();
		// Armazena o token na sessão do cliente.
		// ctx.channel().attr(AUTH_TOKEN).set(authToken);

		// Gera o ByteBuf do token de 4 bytes usando a classe utilitária otimizada.
		ByteBuf authTokenBuf = AuthTokenUtils.generateAuthToken(ctx.alloc(), Utils.randomIntNumber());
		// Converte o ByteBuf para um array de bytes para armazenar na sessão para o payload do pacote.
		byte[] authToken = new byte[authTokenBuf.readableBytes()];
		authTokenBuf.readBytes(authToken);
		authTokenBuf.release(); // Libere o ByteBuf temporário!
		// Armazena o token de 4 bytes na sessão do cliente.
		ctx.channel().attr(GameAttributes.AUTH_TOKEN).set(authToken);
		
		
		byte[] sessionUnique = Utils.fourBytesTokenGet(Utils.randomIntNumber());
		// Armazena o id unico de 4 bytes na sessão do cliente.
		ctx.channel().attr(GameAttributes.SESSION_UNIQUE).set(sessionUnique);
		
		
		

		System.out.println("Token de 4 bytes gerado (positivo): " + Arrays.toString(authToken));

		ByteBuf responsePayload = Unpooled.buffer();
		responsePayload.writeBytes(authToken); // Bytes desconhecidos

		ByteBuf successPacket = PacketUtils.generatePacket(currentTxSum, 0x1001, responsePayload);
		ctx.writeAndFlush(successPacket);
		System.out.println("Token Test:" + Utils.bytesToHex(authToken));
		responsePayload.release(); // Libere o payload após o uso (EH BOA PRATICA CHAPA)
		System.out.println("GS: Resposta de handshake 0x1001 enviada.");

	}
}
