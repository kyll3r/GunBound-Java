package br.com.gunbound.emulator.packets.writers;

import br.com.gunbound.emulator.handlers.GameAttributes;
import br.com.gunbound.emulator.model.entities.game.PlayerSession;
import br.com.gunbound.emulator.utils.PacketUtils;
import br.com.gunbound.emulator.utils.Utils;
import br.com.gunbound.emulator.utils.crypto.GunBoundCipher;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class PlayerPacketAux {
	public static void cashUpdate(PlayerSession ps) {

		// 1. Obtém os dados de sessão do canal.
		byte[] authToken = ps.getPlayerCtx().attr(GameAttributes.AUTH_TOKEN).get();
		//int currentTxSum = ps.getPlayerCtx().attr(GameAttributes.PACKET_TX_SUM).get();

		// 2. Verifica se o usuário e o token estão disponíveis.
		if (ps == null || authToken == null) {
			System.err
					.println("cashUpdate: Sessão do usuário ou token não encontrados. Não é possível enviar o pacote.");
			return;
		}

		// 3. Converte o valor do cash para um array de 4 bytes little-endian.
		int cashAmount = ps.getCash();
		byte[] cashBytes = GunBoundCipher.intToBytesLE(cashAmount, 4);
		ByteBuf successPacket = Unpooled.buffer();

		// 4. Preenche o payload de 4 bytes para um bloco de 12 bytes.
		// O restante do array (8 bytes) será preenchido com zeros.
		byte[] paddedPayload = new byte[12];
		System.arraycopy(cashBytes, 0, paddedPayload, 0, cashBytes.length);

		// 5. Encripta o payload preenchido.
		try {
			// A encriptação dinâmica deve possuir o username, password (hash), token e o
			// comando.
			byte[] encryptedPayload = GunBoundCipher.gunboundDynamicEncrypt(paddedPayload, ps.getNickName(),
					ps.getPassword(), authToken, 0x6101 // Comando do pacote
			);

			// Envia o pacote encriptado.
			successPacket = Utils.toByteBuf(encryptedPayload);
			successPacket = PacketUtils.generatePacket(ps, 0x6101, successPacket,false);
			
			//testando opcoes
			ps.getPlayerCtx().writeAndFlush(successPacket);
			System.out.println("Chego aqui? Token Test:" + Utils.bytesToHex(authToken));
			// successPacket.release(); // Libera o payload após o uso. nao pode liberar
			// aqui se não ferra a conexao

			System.out
					.println("Pacote de cash (0x6101) enviado para " + ps.getNickName() + " com valor: " + cashAmount);
			// successPacket.release(); //tambem nao pode liberar aqui
		} catch (Exception e) {
			System.err.println("Erro ao encriptar e enviar o pacote de cash: " + e.getMessage());
			e.printStackTrace();
		}

	}

}
