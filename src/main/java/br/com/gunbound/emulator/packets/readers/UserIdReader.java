package br.com.gunbound.emulator.packets.readers;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import br.com.gunbound.emulator.handlers.GameAttributes;
import br.com.gunbound.emulator.model.entities.game.PlayerSession;
import br.com.gunbound.emulator.model.entities.game.PlayerSessionManager;
import br.com.gunbound.emulator.utils.PacketUtils;
import br.com.gunbound.emulator.utils.Utils;
import br.com.gunbound.emulator.utils.crypto.GunBoundCipher;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;

public class UserIdReader {

	private static final int OPCODE_REQUEST = 0x1020;
	private static final int OPCODE_RESPONSE = 0x1021;

	public static void read(ChannelHandlerContext ctx, byte[] payload) {
		System.out.println("RECV> SVC_USER_ID (0x" + Integer.toHexString(OPCODE_REQUEST) + ")");

		PlayerSession requesterSession = ctx.channel().attr(GameAttributes.USER_SESSION).get();
		if (requesterSession == null) {
			ctx.close();
			return;
		}

		ByteBuf request = Unpooled.wrappedBuffer(payload);
		try {

			// Pulamos esses bytes para chegar ao nome de usuário.
			request.skipBytes(2);

			// 2. Lemos os 12 bytes que contêm o nome de usuário diretamente do payload.
			String targetUsername = Utils.stringDecode(request.readBytes(12));
			System.out.println(requesterSession.getNickName() + " está procurando por: " + targetUsername);

			// 3. Busca a SESSÃO do jogador ALVO.
			PlayerSession targetSession = PlayerSessionManager.getInstance().getSessionPlayerByNickname(targetUsername);

			// 4. Constrói o payload da resposta.
			ByteBuf responsePayload = writeUserIdResponse(targetSession);

			// 5. CRIPTOGRAFA a resposta. A resposta para 0x1021 é criptografada.
			byte[] authToken = ctx.channel().attr(GameAttributes.AUTH_TOKEN).get();

			// payload é preenchido para um múltiplo de 12 ANTES da criptografia.
			byte[] bytesToEncrypt;
			// O .array() só funciona se o buffer for backed by an array. Mais seguro fazer
			// assim:
			if (responsePayload.hasArray()) {
				bytesToEncrypt = responsePayload.array();
			} else {
				bytesToEncrypt = new byte[responsePayload.readableBytes()];
				responsePayload.getBytes(responsePayload.readerIndex(), bytesToEncrypt);
			}

			int paddingAmount = 12 - (bytesToEncrypt.length % 12);
			if (paddingAmount != 12) {
				bytesToEncrypt = Arrays.copyOf(bytesToEncrypt, bytesToEncrypt.length + paddingAmount);
			}

			byte[] encryptedResponse = GunBoundCipher.gunboundDynamicEncrypt(bytesToEncrypt,
					requesterSession.getNickName(), requesterSession.getPassword(), authToken, OPCODE_RESPONSE);

			// 6. Gera e envia o pacote final com RTC=0.
			//int currentTxSum = ctx.channel().attr(GameAttributes.PACKET_TX_SUM).get();
			ByteBuf finalPacket = PacketUtils.generatePacket(requesterSession, OPCODE_RESPONSE,
					Unpooled.wrappedBuffer(encryptedResponse), true);
			ctx.writeAndFlush(finalPacket);

			System.out.println("Enviada resposta de ID de usuário (0x1021) para " + requesterSession.getNickName());

		} catch (Exception e) {
			System.err.println("Erro ao processar SVC_USER_ID: " + e.getMessage());
			e.printStackTrace();
			ctx.close();
		} finally {
			request.release();
		}
	}

	public static ByteBuf writeUserIdResponse(PlayerSession targetSession) {
		ByteBuf buffer = Unpooled.buffer();

		// Se a sessão alvo não for encontrada, retorna um buffer com dados "vazios".
		if (targetSession == null) {
			buffer.writeBytes(new byte[32]); // 12+12 para nick, 8 para guild
			buffer.writeShortLE(0); // rank
			buffer.writeShortLE(0); // rank
			return buffer;
		}

		// Escreve o nome de usuário duas vezes, como na referência
		buffer.writeBytes(Utils.resizeBytes(targetSession.getNickName().getBytes(StandardCharsets.ISO_8859_1), 12));
		buffer.writeBytes(Utils.resizeBytes(targetSession.getNickName().getBytes(StandardCharsets.ISO_8859_1), 12));

		// Escreve a guilda e os ranks
		buffer.writeBytes(Utils.resizeBytes(targetSession.getGuild().getBytes(StandardCharsets.ISO_8859_1), 8));
		buffer.writeShortLE(targetSession.getRankCurrent());
		buffer.writeShortLE(targetSession.getRankSeason());

		return buffer;
	}

}