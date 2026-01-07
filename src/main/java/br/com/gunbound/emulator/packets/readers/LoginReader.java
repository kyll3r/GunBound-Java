package br.com.gunbound.emulator.packets.readers;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import br.com.gunbound.emulator.handlers.GameAttributes;
import br.com.gunbound.emulator.model.DAO.DAOFactory;
import br.com.gunbound.emulator.model.DAO.UserDAO;
import br.com.gunbound.emulator.model.entities.DTO.UserDTO;
import br.com.gunbound.emulator.model.entities.game.PlayerSession;
import br.com.gunbound.emulator.model.entities.game.PlayerSessionManager;
import br.com.gunbound.emulator.packets.readers.lobby.LobbyJoin;
import br.com.gunbound.emulator.utils.FunctionRestrict;
import br.com.gunbound.emulator.utils.PacketUtils;
import br.com.gunbound.emulator.utils.Utils;
import br.com.gunbound.emulator.utils.crypto.GunBoundCipher;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;

public class LoginReader {

	private static final int LOGIN_REQUEST = 0x1010;
	private static final int LOGIN_SUCCESS = 0x1012;

	public static void read(ChannelHandlerContext ctx, byte[] payload) {
		System.out.println("RECV> SVC_LOGIN/ADMIN 0x" + Integer.toHexString(LOGIN_REQUEST) + ")");

		// Inicia a Factory para buscar no banco de dados
		UserDAO factory = DAOFactory.CreateUserDao();

		int currentTxSum = ctx.channel().attr(GameAttributes.PACKET_TX_SUM).get();
		// ByteBuf successPacket = Unpooled.buffer();

		// Descryptografa o nome de usuário (estaticamente).
		// O nome de usuário é um bloco de 16 bytes no início da carga util (payload) do
		// pacote.
		byte[] usernameEncryptedBlock = Arrays.copyOfRange(payload, 0, 0x10);
		String userId = null;
		try {
			byte[] usernameDecryptedBytes = GunBoundCipher.gunboundStaticDecrypt(usernameEncryptedBlock);
			// Decodifica a string, removendo o preenchimento nulo.
			userId = new String(usernameDecryptedBytes, StandardCharsets.UTF_8).trim();
		} catch (Exception e) {
			System.err.println("Erro na descriptografia estática do nome de usuário: " + e.getMessage());
			ctx.close();
			return;
		}

		// 2. Busca o usuário no "banco de dados". e armazena na sessão.
		UserDTO queriedUser = factory.getUserByUserId(userId);

		// Valida se usuario existe
		if (queriedUser == null) {
			System.err.println("Usuario informado inexistente");
			sendPlayerAnError(ctx, currentTxSum);
			return;
		}

		// Descriptografa a senha (dinamicamente).
		// A senha está nos blocos dinâmicos, que começam no offset 0x20 (32 bytes) do
		// payload.
		int passwordEncryptedOffset = 0x20;
		byte[] passwordEncryptedBlocks = Arrays.copyOfRange(payload, passwordEncryptedOffset, payload.length);

		byte[] authToken = ctx.channel().attr(GameAttributes.AUTH_TOKEN).get();
		if (authToken == null) {
			System.err.println("Token de autenticação não encontrado na sessão. Desconectando.");
			ctx.close();
			return;
		}

		String receivedPassword = null;
		try {
			// Função gunboundDynamicDecrypt_raw retorna os dados puros (com o checksum).
			// e a decrypt processa o checksum.
			byte[] passwordDecryptedPayload = GunBoundCipher.gunboundDynamicDecrypt(passwordEncryptedBlocks,
					queriedUser.getUserId(), queriedUser.getPassword(), // O código Python usa a senha do DB para gerar
																		// a
					// chave.
					authToken, LOGIN_REQUEST // O comando 0x1010
			);

			// A senha tem 12 bytes no payload descriptografado.
			// Extrai a senha e remove o preenchimento nulo.
			receivedPassword = new String(passwordDecryptedPayload, 0, 12, StandardCharsets.ISO_8859_1).trim();

		} catch (Exception e) {
			System.err.println("Erro na descriptografia dinâmica da senha: " + e.getMessage());
			ctx.close();
			return;
		}

		System.out.println("Username: " + queriedUser.getUserId() + ", Rcv Password: " + receivedPassword
				+ ", DB Password: " + queriedUser.getPassword());

		// Valida Login e Senha
		if (!receivedPassword.equals(queriedUser.getPassword())) {
			System.err.println("Senha do Usuario: " + queriedUser.getUserId() + " incorreta");
			sendPlayerAnError(ctx, currentTxSum);
		} else {
			// Autenticação bem-sucedida!
			System.out.println("Usuário " + queriedUser.getUserId() + " logado com sucesso!");

			try {
				byte[] versionEncryptedBlocks = Arrays.copyOfRange(payload, 0x20, payload.length);
				byte[] dynamicPayload = GunBoundCipher.gunboundDynamicDecrypt(versionEncryptedBlocks,
						queriedUser.getUserId(), queriedUser.getPassword(), authToken, 0x1010);
				int clientVersion = (dynamicPayload[0x14] & 0xFF) | ((dynamicPayload[0x15] & 0xFF) << 8);
				ctx.channel().attr(GameAttributes.CLIENT_VERSION).set(clientVersion);

				// Cria a sessão do jogador
				PlayerSession session = new PlayerSession(queriedUser, ctx);
				// --- Parte 4: Finalizar configuração da sessão e entrar no canal ---
				PlayerSessionManager.getInstance().addPlayer(session);
				ctx.channel().attr(GameAttributes.USER_SESSION).set(session);

				// **Usa o PacketWriter para construir o payload**
				ByteBuf loginPayload = writeLoginSuccess(session, authToken);

				// Gera o pacote final e envia
				ByteBuf finalPacket = PacketUtils.generatePacket(session, LOGIN_SUCCESS, loginPayload,false);
				// Envia o pacote de login e, no listener de sucesso, AGENDA a próxima etapa.
				ctx.writeAndFlush(finalPacket).addListener(future -> {
					if (future.isSuccess()) {
						System.out.println("Pacote de login (0x1012) enviado. Agendando entrada no canal em 150ms.");

						// PULO DO GATO: Agendamos a tarefa no EventLoop do channel (NETTY)
						// para ser executada após um atraso, dando tempo ao cliente.
						ctx.channel().eventLoop().schedule(() -> {
							LobbyJoin.read(ctx, null);// nao tem payload
						}, 150, java.util.concurrent.TimeUnit.MILLISECONDS);

					} else {
						System.err
								.println("Falha ao enviar o pacote de login (0x1012): " + future.cause().getMessage());
						future.cause().printStackTrace();
						ctx.close();
					}
				});
				System.out.println("Pacote de login bem-sucedido (0x1012) enviado para " + queriedUser.getUserId());

				ctx.flush();

			} catch (Exception e) {
				System.err.println("Erro ao processar e enviar pacote de sucesso: " + e.getMessage());
				e.printStackTrace();
				ctx.close();
			}

		}

	}

	private static ByteBuf writeLoginSuccess(PlayerSession session, byte[] authToken)
			throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException,
			BadPaddingException, IOException {

		// Para ativar Avatares, Thor e um Evento
		int enabledFunctionsMultiple = FunctionRestrict.getFunctionValue(FunctionRestrict.EFFECT_FORCE,
				FunctionRestrict.EFFECT_TORNADO, FunctionRestrict.EFFECT_LIGHTNING, FunctionRestrict.EFFECT_WIND,
				FunctionRestrict.EFFECT_THOR, FunctionRestrict.EFFECT_MOON, FunctionRestrict.EFFECT_ECLIPSE);

		ByteBuf buffer = Unpooled.buffer();

		// --- Campos do Pacote ---
		buffer.writeBytes(new byte[] { 0x00, 0x00 }); // ????
		buffer.writeBytes(session.getPlayerCtxChannel().attr(GameAttributes.SESSION_UNIQUE).get()); // session_unique
																								// (4 bytes)
		buffer.writeBytes(Utils.resizeBytes(session.getNickName().getBytes(StandardCharsets.ISO_8859_1), 0xC)); // Username
		buffer.writeByte(session.getGender());
		buffer.writeBytes(Utils.resizeBytes(session.getGuild().getBytes(StandardCharsets.ISO_8859_1), 8)); // Guild (8
																											// bytes)

		buffer.writeShortLE(session.getRankCurrent());
		buffer.writeShortLE(session.getRankSeason());

		// Valores padrão para campos não implementados
		buffer.writeShortLE(session.getMemberGuildCount()); // MemberCount Guild
		buffer.writeIntLE(session.getTotalRank()); // TotalRank
		buffer.writeIntLE(session.getSeasonRank()); // SeasonRank
		buffer.writeIntLE(session.getGuildRank()); // GuildRank
		buffer.writeIntLE(0); // padding
		buffer.writeIntLE(0); // padding
		buffer.writeIntLE(session.getTotalScore()); // TotalScore (GP)
		buffer.writeIntLE(session.getSeasonScore()); // SeasonScore (GP da Temporada)
		buffer.writeIntLE(session.getGold());
		// Scores de Evento (zerados)
		buffer.writeIntLE(0); // EventScore0
		buffer.writeIntLE(0); // EventScore1
		buffer.writeIntLE(0); // EventScore2
		buffer.writeIntLE(0); // EventScore3

		// --- Parte 2: Dados a Serem Criptografados ---

		// Cria um buffer temporário para os dados que serão criptografados
		ByteBuf dataToEncrypt = Unpooled.buffer();
		dataToEncrypt.writeIntLE(enabledFunctionsMultiple);
		dataToEncrypt.writeIntLE(100); // ScoreFactor
		dataToEncrypt.writeIntLE(100); // GoldFactor

		/*
		 * byte[] passwordDecryptedPayload =
		 * GunBoundCipher.gunboundDynamicEncrypt(Utils.
		 * hexStringToByteArray("00 E0 0F 00 C8 00 00 00 D2 00 00 00 00 E0 0F 00 00 00 00 01 03 03 04 05 "
		 * ), session.getNickName(), session.getPassword(), // O código Python usa a
		 * senha do DB para gerar a // chave. authToken, 0x1012 // O comando 0x1010 );
		 */

		try {
			// Converte o buffer temporário para um array de bytes
			byte[] plainBytes = new byte[dataToEncrypt.readableBytes()];
			dataToEncrypt.readBytes(plainBytes);

			// Criptografa o array de bytes
			byte[] encryptedBytes = GunBoundCipher.gunboundDynamicEncrypt(plainBytes, session.getUserNameId(),
					session.getPassword(), authToken, 0x1012 // Opcode 4114 em decimal
			);

			// Adiciona os dados criptografados ao buffer principal
			buffer.writeBytes(encryptedBytes);
		} catch (Exception e) {
			System.err.println("Falha ao criptografar o bloco de FuncRestrict no login.");
			e.printStackTrace();
		} finally {
			// Libera o buffer temporário
			dataToEncrypt.release();
		}

		return buffer;
	}

	private static void sendPlayerAnError(ChannelHandlerContext ctx, int currentTxSum) {
		// Senha incorreta, rejeita o cliente.
		System.out.println("Usuario ou senha incorretos desconectando socket.");
		// 10 00 é para login incorreto eu particularmente acho errado avisar se é o
		// login ou senha incorreto
		//60 versao incorreta
		//30 login proibido (prefiro essa porque fecha o cliente)
		
		ByteBuf finalPacket = PacketUtils.generatePacket(currentTxSum, 0x1312,
				Unpooled.wrappedBuffer(new byte[] { (byte) 0x30, (byte) 0x00 }));
		// Envia o pacote com erro.
		ctx.writeAndFlush(finalPacket);

		// sendPacket(ctx, 0x1012, hexStringToBytes("1100")); // Erro: Senha ou user
		// incorretos
		ctx.close();
	}

}
