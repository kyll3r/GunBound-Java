package br.com.gunbound.emulator.packets.readers.room.gameplay;

import org.mariadb.jdbc.plugin.authentication.standard.ed25519.Utils;

import br.com.gunbound.emulator.handlers.GameAttributes;
import br.com.gunbound.emulator.model.entities.game.PlayerSession;
import br.com.gunbound.emulator.room.GameRoom;
import br.com.gunbound.emulator.utils.crypto.GunBoundCipher;
import io.netty.channel.ChannelHandlerContext;

public class GamePlayerResurrectReader {

	private static final int OPCODE_REQUEST = 0x4104;
	//private static final int OPCODE_CONFIRMATION = 0x4410;

	public static void read(ChannelHandlerContext ctx, byte[] payload) {
		System.out.println("RECV> SVC_PLAY_RESURRECT (0x" + Integer.toHexString(OPCODE_REQUEST) + ")");
		PlayerSession player = ctx.channel().attr(GameAttributes.USER_SESSION).get();
		if (player == null)
			return;

		GameRoom room = player.getCurrentRoom();
		if (room == null)
			return;
		
		
		//ja seta que voltou a viver
		player.setIsAlive(1);

		// Calcula o próximo múltiplo de 16
		int paddingLength = 16 - (payload.length % 16);
		if (paddingLength != 16) { // Se já for múltiplo de 16, não precisa preencher
			byte[] padded = new byte[payload.length + paddingLength];
			System.arraycopy(payload, 0, padded, 0, payload.length);
			payload = padded; // agora payload está com padding
		}

		try {
			// 1. Descriptografa o payload do chat
			byte[] authToken = ctx.channel().attr(GameAttributes.AUTH_TOKEN).get();
			byte[] decryptedPayload = GunBoundCipher.gunboundDynamicDecrypt(payload, player.getUserNameId(),
					player.getPassword(), authToken, OPCODE_REQUEST);
			
			
			System.out.println("[DEBUG] RESURRECT >>>> " + Utils.bytesToHex(decryptedPayload));


		} catch (Exception e) {
			System.err.println("Erro ao processar pacote de Ressureição " + e.getMessage());
			e.printStackTrace();
		}
	}
}