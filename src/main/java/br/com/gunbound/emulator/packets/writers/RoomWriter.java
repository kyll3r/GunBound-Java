package br.com.gunbound.emulator.packets.writers;

import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import br.com.gunbound.emulator.model.entities.game.PlayerAvatar;
import br.com.gunbound.emulator.model.entities.game.PlayerSession;
import br.com.gunbound.emulator.playdata.SpawnPoint;
import br.com.gunbound.emulator.room.GameRoom;
import br.com.gunbound.emulator.utils.PacketUtils;
import br.com.gunbound.emulator.utils.Utils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

/**
 * Classe dedicada a construir os payloads de pacotes relacionados a salas
 * (Rooms). Segue o Princípio da Responsabilidade Única, separando a lógica de
 * pacotes de sala.
 */
public final class RoomWriter {

	// Construtor privado para evitar instanciação, já que todos os métodos são
	// estáticos.
	private RoomWriter() {
	}





	/**
	 * Constrói o payload para notificar que um jogador entrou na sala (0x3010).
	 *
	 * @param newPlayer O jogador que acabou de entrar.
	 * @param slot      O slot que o jogador ocupou.
	 * @return um ByteBuf com o payload pronto.
	 */
	public static ByteBuf writeNotifyPlayerJoinedRoom(PlayerSession newPlayer) {
		ByteBuf buffer = Unpooled.buffer();
		
		
		// avatares sendo usados
		List<PlayerAvatar> avatarWearing = newPlayer.getPlayerAvatars().stream()
				.filter(av -> "1".equals(av.getWearing())).collect(Collectors.toList());

		// Escreve os dados do NOVO jogador, que serão enviados para os jogadores
		// existentes.
		buffer.writeByte(newPlayer.getCurrentRoom().getSlotPlayer(newPlayer));
		buffer.writeBytes(Utils.resizeBytes(newPlayer.getNickName().getBytes(StandardCharsets.ISO_8859_1), 12)); //
		buffer.writeByte(newPlayer.getGender()); // gender
		// buffer.writeBytes(new byte[] { 01 }); // gender

		// Info de Conexão UDP
		try {
			byte[] playerIp = InetAddress
					.getByName(newPlayer.getPlayerCtx().remoteAddress().toString().split(":")[0].substring(1))
					.getAddress();
			buffer.writeBytes(playerIp);
			buffer.writeShort(19363); // Porta UDP
			buffer.writeBytes(playerIp);
			buffer.writeShort(19363);
		} catch (Exception e) {
			buffer.writeBytes(new byte[12]);
		}

		// Lê os valores da sessão do jogador, em vez de usar valores fixos.
		buffer.writeByte(newPlayer.getRoomTankPrimary());
		buffer.writeByte(newPlayer.getRoomTankSecondary());
		buffer.writeByte(newPlayer.getRoomTeam());
		// buffer.writeBytes(Utils.hexStringToByteArray("01"));
		// Avatar, Guild e Ranks
		// buffer.writeBytes(new byte[8]);

		buffer.writeBytes(Utils.resizeBytes(newPlayer.getGuild().getBytes(StandardCharsets.ISO_8859_1), 8)); //
		buffer.writeShortLE(newPlayer.getRankCurrent());
		buffer.writeShortLE(newPlayer.getRankSeason());
		buffer.writeByte(avatarWearing.size());
		buffer.writeBytes(new byte[] { 00});
			avatarWearing.forEach(avatar -> {
				buffer.writeBytes(PacketUtils.intToBytes(avatar.getItem(), 3, false));
				buffer.writeBytes(new byte[] { 00 });
			});
		buffer.writeBytes(new byte[] {  01 });


		return buffer;
	}

	/**
	 * Constrói o payload nulo para notificar a mudança de estado na sala
	 *
	 * @param ctx Channel do netty.
	 */
	private static final int OPCODE_ROOM_UPDATE = 0x3105;

	public static void writeRoomUpdate(PlayerSession player) {
		// O payload da notificação é vazio.
		ByteBuf notifyPayload = Unpooled.EMPTY_BUFFER;

		// Gera um pacote com a sequência CORRETA para este jogador.
		ByteBuf notifyPacket = PacketUtils.generatePacket(player, OPCODE_ROOM_UPDATE, notifyPayload, true);
		// Envia o pacote individualmente.
		player.getPlayerCtx().writeAndFlush(notifyPacket);
	}

	/**
	 * Constrói o payload para a lista de salas a ser enviada ao cliente (0x2103).
	 *
	 * @param rooms A coleção de salas a serem incluídas na lista.
	 * @return um ByteBuf com o payload pronto.
	 */
	public static ByteBuf writeRoomList(Collection<GameRoom> rooms) {
		ByteBuf buffer = Unpooled.buffer();

		// 1. Escreve o número total de salas na lista
		buffer.writeShortLE(rooms.size());

		// 2. Loop para cada sala
		for (GameRoom room : rooms) {
			buffer.writeShortLE(room.getRoomId());

			byte[] titleBytes = room.getTitle().getBytes(StandardCharsets.ISO_8859_1);
			buffer.writeByte(titleBytes.length);
			buffer.writeBytes(titleBytes);

			buffer.writeByte(room.getMapId());
			buffer.writeIntLE(room.getGameSettings());
			buffer.writeBytes(new byte[] { 00 });// tem pu talvez?
			buffer.writeByte(room.getPlayerCount());
			buffer.writeByte(room.getCapacity());
			buffer.writeByte(room.isGameStarted() ? 1 : 0); // Estado do jogo (1=jogando, 0=esperando)
			// buffer.writeBytes(new byte[] {00});
			buffer.writeByte(room.isPrivate() ? 1 : 0); // Trancada (1=senha, 0=aberta)
			System.out.println("[DEBUG] writeRoomList sala privada?: " + room.isPrivate());

		}
		buffer.writeBytes(new byte[] { 00 });// what? i dont know is padding?
		return buffer;
	}

	/**
	 * Constrói o payload para o pacote de início de jogo (0x3432).
	 */
	public static ByteBuf writeGameStartPacket(GameRoom room, List<Integer> turnOrder,
			Map<Integer, SpawnPoint> spawnPoints, byte[] payloadRcv) {
		ByteBuf buffer = Unpooled.buffer(12);

		// 1. Mapa e contagem de jogadores
		buffer.writeByte(room.getMapId());
		buffer.writeIntLE(room.getPlayerCount());

		// 2. Loop para cada jogador
		int turnCounter = 0;
		for (Map.Entry<Integer, PlayerSession> entry : room.getPlayersBySlot().entrySet()) {
			int slot = entry.getKey();
			PlayerSession player = entry.getValue();
			SpawnPoint spawn = spawnPoints.get(slot);

			buffer.writeByte(slot);
			buffer.writeBytes(Utils.resizeBytes(player.getNickName().getBytes(StandardCharsets.ISO_8859_1), 12));
			// buffer.writeBytes(new byte[] { 0x01 });// gender?
			buffer.writeByte(player.getRoomTeam());
			buffer.writeByte(player.getRoomTankPrimary());
			buffer.writeByte(player.getRoomTankSecondary());

			// Posição X e Y
			System.out.println("[DEBUG] spawn.getXMin " + spawn.getXMin());
			buffer.writeShortLE(spawn.getXMin()); // Usando xMin como exemplo, você pode randomizar
			System.out.println("[DEBUG] spawn.getY() " + spawn.getY());
			buffer.writeShortLE(spawn.getY());

			// buffer.writeBytes(Utils.hexStringToByteArray("36020000"));

			// Ordem do turno
			buffer.writeShortLE(turnOrder.get(turnCounter++));

		}

		buffer.writeBytes(Utils.hexStringToByteArray("0000"));// Evento ativa com FF00
		buffer.writeBytes(payloadRcv);

		return buffer;
	}

	public static byte[] writeGameStartPacketTest(GameRoom room, List<Integer> turnOrder,
			Map<Integer, SpawnPoint> spawnPoints, byte[] payloadRcv) {
		ByteBuf buffer = Unpooled.buffer();

		//buffer.writeBytes(new byte[] { (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00 });
		buffer.writeIntLE(room.getGameSettings());
		// buffer.writeBytes(payloadRcv);

		buffer.writeByte(room.getMapId());
		buffer.writeShortLE(room.getPlayerCount());
		// 2. Loop para cada jogador
		int turnCounter = 0;
		for (Map.Entry<Integer, PlayerSession> entry : room.getPlayersBySlot().entrySet()) {
			int slot = entry.getKey();
			PlayerSession player = entry.getValue();
			SpawnPoint spawn = spawnPoints.get(slot);

			System.out.println("[DEBUG - writeGameStartPacketTest] Associando slot do player: " + player.getNickName()
					+ " [" + slot + "]");

			buffer.writeByte(slot);
			buffer.writeBytes(Utils.resizeBytes(player.getNickName().getBytes(StandardCharsets.ISO_8859_1), 12));
			// buffer.writeByte((byte)0x01);// gender?
			buffer.writeByte(player.getRoomTeam());
			buffer.writeByte((player.getRoomTankPrimary()) == 0xFF ? Utils.randomMobile(99) : player.getRoomTankPrimary());
			buffer.writeByte(player.getRoomTankSecondary() == 0xFF ? Utils.randomMobile(99) : player.getRoomTankSecondary());

			int playerX = new Random().nextInt(spawn.getXMax() - spawn.getXMin() + 1) + spawn.getXMin();
			int playerY = spawn.getY(); // getY() já trata o null, retornando 0

			// buffer.writeShortLE(playerX); // Usando xMin como exemplo, você pode
			// randomizar
			// buffer.writeShortLE(playerY);

			System.out.println("[DEBUG] spawn.getXMin " + spawn.getXMin());
			buffer.writeShortLE(spawn.getXMin()); // Usando xMin como exemplo, você pode randomizar
			System.out.println("[DEBUG] spawn.getY() " + spawn.getY());
			buffer.writeShortLE(spawn.getY());

			buffer.writeShortLE(turnOrder.get(turnCounter++));
		}

		buffer.writeBytes(new byte[] { (byte) 0xFF, (byte) 0xFF });// Evento ativa com FF00
		buffer.writeBytes(payloadRcv);

		// Adiciona um padding para garantir o alinhamento de 12 bytes. (Por conta da
		// criptografia)
		int currentSize = buffer.writerIndex();
		int paddingSize = (12 - (currentSize % 12)) % 12; // O % 12 no final lida com o caso de já ser múltiplo.
		if (paddingSize > 0) {
			buffer.writeBytes(new byte[paddingSize]);
		}

		// Converte o conteúdo do buffer para um array de bytes.
		byte[] payload = new byte[buffer.readableBytes()];
		buffer.readBytes(payload);
		buffer.release(); // Liberta o buffer temporário.

		return payload;
	}

	public static ByteBuf writeGameStartPacketBuggy(GameRoom room, List<Integer> turnOrder,
			Map<Integer, SpawnPoint> spawnPoints, byte[] payloadRcv) {
		ByteBuf buffer = Unpooled.wrappedBuffer(Utils.hexStringToByteArray(
				"09020000000075730000000000000000000000000D0D290100000000014B794C4C335200000000000000010D054D050000010000FF681CD00F"));

		return buffer;
	}
}