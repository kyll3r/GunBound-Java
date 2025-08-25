package br.com.gunbound.emulator.packets.readers.room;

import java.nio.charset.StandardCharsets;

import br.com.gunbound.emulator.handlers.GameAttributes;
import br.com.gunbound.emulator.lobby.GunBoundLobbyManager;
import br.com.gunbound.emulator.model.entities.game.PlayerSession;
import br.com.gunbound.emulator.room.GameRoom;
import br.com.gunbound.emulator.room.RoomManager;
import br.com.gunbound.emulator.room.model.enums.GameMode;
import br.com.gunbound.emulator.utils.PacketUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;

public class RoomCreateReader {

	private static final int OPCODE_CREATE_REQUEST = 0x2120;
	private static final int OPCODE_CREATE_SUCCESS = 0x2121;

	/*
	 * +-------------------------------------------------+ | 0 1 2 3 4 5 6 7 8 9 a b
	 * c d e f |
	 * +--------+-------------------------------------------------+----------------+
	 * |00000000| 24 00 57 2b 20 21 14| 31 32 33 34 35 36 37 38 39 |.W+ !.123456789|
	 * |00000010| 30 41 42 43 44 45 46 47 48 49 4a| b2 62| 0c 00| 31
	 * |0ABCDEFGHIJ.b..1| |00000020| 32 33 34| 08 |234. |
	 * +--------+-------------------------------------------------+----------------+
	 */

	// ignora 6 bytes do cabeçalho
	// O primeiro byte pós cabeçalho do payload é o comprimento do título.
	// titulo coluna 6, 14 = 20 caracteres 1234567890ABCDEFGHIJ
	// restante é o bloco com informações da sala
	// b262 = game settings?
	// 0c00 = game mode
	// 31323334 = password
	// 08 = capacidade da sala

	public static void read(ChannelHandlerContext ctx, byte[] payload) {
		System.out.println("RECV> SVC_ROOM_CREATE (0x" + Integer.toHexString(OPCODE_CREATE_REQUEST) + ")");
		PlayerSession creator = ctx.channel().attr(GameAttributes.USER_SESSION).get();
		if (creator == null)
			return;

		ByteBuf request = Unpooled.wrappedBuffer(payload);
		try {

			// 1. Lê o título
			int titleLength = request.readUnsignedByte();
			String title = request.readBytes(titleLength).toString(StandardCharsets.ISO_8859_1);

			// 2. Lê o bloco de 4 bytes que contém as configurações de jogo.
			int gameSettings = request.readIntLE();

			// 3. Lê os 4 bytes da senha.
			// String password = request.readBytes(4).toString(StandardCharsets.ISO_8859_1);

			// ************************* BYPASS DO BUG DA SENHA *************************
			String password;
			byte[] passwordBytes = new byte[4];
			request.readBytes(passwordBytes); // Lê os 4 bytes da senha

			boolean passwordIsValid = true;
			// Itera sobre os 4 bytes lidos
			for (byte b : passwordBytes) {
				// Verifica se o byte é nulo (0x00) ou um espaço em branco (0x20)
				if (b == 0 || b == ' ') {

					passwordIsValid = false;
					break; // Se encontrar um inválido, para o loop
				}
			}

			if (passwordIsValid) {
				// Se todos os caracteres forem válidos, decodifica a senha
				password = new String(passwordBytes, StandardCharsets.ISO_8859_1);
			} else {
				// Caso contrário, a senha é definida como vazia
				password = "";

				// Debug para avisar qeu a senha é "zoada"
				System.out.println("[DEBUG] Senha invalida: " + new String(passwordBytes, StandardCharsets.ISO_8859_1));
			}
			// ************************* FIM DO BYPASS DO BUG DA SENHA
			// *************************

			// 4. Lê 1 byte da capacidade.
			int capacity = request.readUnsignedByte();

			// --- EXTRAINDO O GAMEMODE PARA DEBUG ---
			short gameModeId = (short) (gameSettings >> 16);
			GameMode gameModeEnum = GameMode.fromId(gameModeId);
			//seta o gameMode para verificações
			

			/*
			 * 1. O primeiro byte do payload é o comprimento do título. int titleLength =
			 * request.readUnsignedByte();
			 * 
			 * // 2. Lê o título em si. String title =
			 * request.readBytes(titleLength).toString(StandardCharsets.ISO_8859_1);
			 * 
			 * // 3. O restante do buffer é o bloco 'otherData'. ByteBuf otherData =
			 * request.readBytes(request.readableBytes());
			 * 
			 * 
			 * // 4. Lê os campos a partir de offsets fixos DENTRO de 'otherData'. int
			 * gameConfigId = otherData.getIntLE(0); // Pega 4 bytes do índice 0 // 4. Lê os
			 * campos a partir de offsets fixos DENTRO de 'otherData'. int gameModeId =
			 * otherData.getByte(2); // Pega 1 bytes do índice 2 GameMode gameMode =
			 * GameMode.fromId(gameModeId); String password = otherData.slice(4,
			 * 4).toString(StandardCharsets.ISO_8859_1).trim(); // Pega 4 bytes do // índice
			 * 4 int capacity = otherData.getUnsignedByte(8); // Pega 1 byte do índice 8
			 * 
			 * // Libera o buffer temporário otherData.release();
			 */

			System.out.println("Tentativa de criar sala: Título='" + title + "', Senha='" + password + "', Capacidade="
					+ capacity);

			// 5. Usa o RoomManager para criar a sala
			GameRoom createdRoom = RoomManager.getInstance().createRoom(creator, title, password, capacity);

			if (createdRoom == null) {
				System.err.println("Falha ao criar a sala. Não há IDs disponíveis.");
				// TODO: Enviar pacote de erro para o cliente
				return;
			}

			System.out.println("[SALA CRIADA] gamesettings: " + gameSettings);
			createdRoom.setGameSettings(gameSettings);
			
			//vamos usar isso aqui
			createdRoom.setGameMode(gameModeId);

			// 6. Envia pacote de sucesso
			//int currentTxSum = ctx.channel().attr(GameAttributes.PACKET_TX_SUM).get();
			ByteBuf successPayload = writeCreateRoomSuccess(createdRoom);
			ByteBuf successPacket = PacketUtils.generatePacket(creator, OPCODE_CREATE_SUCCESS, successPayload,false);

			ctx.writeAndFlush(successPacket);

			System.out.println("Sala '" + title + "' criada com sucesso com ID " + createdRoom.getRoomId() + "', Modo="
					+ gameModeEnum.getName());

			// O jogador não pode estar no lobby e em uma sala ao mesmo tempo.
			GunBoundLobbyManager.getInstance().playerLeaveLobby(creator); //
			System.out.println("Jogador " + creator.getNickName() + " removido do lobby para entrar na sala.");

		} catch (Exception e) {
			System.err.println("Erro fatal ao decodificar o pacote de criação de sala:");
			e.printStackTrace();
			ctx.close();
		} finally {

			request.release();
		}
	}

	/**
	 * Constrói o payload para o pacote de sucesso na criação da sala (0x2121).
	 *
	 * @param createdRoom A sala que acabou de ser criada.
	 * @return um ByteBuf com o payload pronto.
	 */
	public static ByteBuf writeCreateRoomSuccess(GameRoom createdRoom) {
		ByteBuf buffer = Unpooled.buffer();

		// 204301

		buffer.writeBytes(new byte[] { (byte) 0x00, (byte) 0x00, (byte) 0x00 }); // 3 bytes nulos desconhecidos
		buffer.writeShortLE(createdRoom.getRoomId());

		// MOTD da Sala (pode ser customizável)
		String motd = "Sala criada com sucesso!";
		buffer.writeBytes(motd.getBytes(StandardCharsets.ISO_8859_1));

		return buffer;
	}

}