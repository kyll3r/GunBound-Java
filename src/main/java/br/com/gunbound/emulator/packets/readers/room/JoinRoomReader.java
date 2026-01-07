// Arquivo renomeado: RoomJoinReader.java
package br.com.gunbound.emulator.packets.readers.room;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import br.com.gunbound.emulator.handlers.GameAttributes;
import br.com.gunbound.emulator.lobby.GunBoundLobbyManager;
import br.com.gunbound.emulator.model.entities.game.PlayerAvatar;
import br.com.gunbound.emulator.model.entities.game.PlayerSession;
import br.com.gunbound.emulator.room.GameRoom;
import br.com.gunbound.emulator.room.RoomManager;
import br.com.gunbound.emulator.utils.PacketUtils;
import br.com.gunbound.emulator.utils.Utils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;

public class JoinRoomReader {

	private static final int OPCODE_JOIN_REQUEST = 0x2110;
	private static final int OPCODE_JOIN_SUCCESS = 0x2111;
	private static final int OPCODE_PREPARE_JOIN = 0x21F5;

	public static void read(ChannelHandlerContext ctx, byte[] payload) {
		System.out.println("RECV> SVC_ROOM_JOIN (0x" + Integer.toHexString(OPCODE_JOIN_REQUEST) + ")");
		PlayerSession joiningPlayer = ctx.channel().attr(GameAttributes.USER_SESSION).get();
		if (joiningPlayer == null)
			return;

		// 1. Decodificar o payload da requisição
		ByteBuf request = Unpooled.wrappedBuffer(payload);

		int roomId = request.readShortLE();
		String password = Utils.stringDecode(request.readBytes(request.readableBytes()));
		request.release();

		// 2. Encontrar a sala e verificar a senha
		GameRoom room = RoomManager.getInstance().getRoomById(roomId);

		//int currentTxSum = ctx.channel().attr(GameAttributes.PACKET_TX_SUM).get();

		if (room == null || !room.checkPassword(password)) {
			System.err.println("Jogador " + joiningPlayer.getNickName() + " falhou ao entrar na sala " + roomId
					+ " (inválida ou senha incorreta).");

			ByteBuf preparePayload = Unpooled.wrappedBuffer(new byte[] { (byte) 0x11, (byte) 0x00 });
			ByteBuf preparePacket = PacketUtils.generatePacket(joiningPlayer, OPCODE_JOIN_SUCCESS, preparePayload,false);
			ctx.writeAndFlush(preparePacket);
			return;
		}

		try {
			// 3. Adicionar jogador à sala
			int playerSlot = room.addPlayer(joiningPlayer);
			if (playerSlot == -1) {
				System.err.println("Sala " + roomId + " cheia. " + joiningPlayer.getNickName() + " não pôde entrar.");
				ByteBuf preparePayload = Unpooled.wrappedBuffer(new byte[] { (byte) 0x01, (byte) 0x20 });
				ByteBuf preparePacket = PacketUtils.generatePacket(joiningPlayer, OPCODE_JOIN_SUCCESS, preparePayload,false);
				ctx.writeAndFlush(preparePacket);
				return;
			}

			// --- ENVIO ASSÍNCRONO ---

			// 1. Gera e envia o primeiro pacote (0x21F5).
			ByteBuf preparePayload = Unpooled.wrappedBuffer(new byte[] { 0x03 });
			ByteBuf preparePacket = PacketUtils.generatePacket(joiningPlayer, OPCODE_PREPARE_JOIN, preparePayload, true);

			// writeAndFlush retorna um "Future", uma promessa de que a operação será
			// concluída.
			ChannelFuture writeFuture = ctx.writeAndFlush(preparePacket);

			// 2. Adiciona um Listener que será executado QUANDO o envio do primeiro pacote
			// terminar.
			writeFuture.addListener((ChannelFutureListener) future -> {
				if (future.isSuccess()) {
					// NESTE PONTO, TEMOS A GARANTIA QUE O PacketSizeTracker RODOU.
					System.out.println("Pacote de preparação (0x21F5) enviado com sucesso.");

					// 3. AGORA, com o tx_sum atualizado, enviamos o segundo pacote.
					//int updatedTxSum = ctx.channel().attr(GameAttributes.PACKET_TX_SUM).get();
					ByteBuf successPayload = writeJoinRoomSuccess(room, joiningPlayer);
					ByteBuf successPacket = PacketUtils.generatePacket(joiningPlayer, OPCODE_JOIN_SUCCESS,
							successPayload,false);
					ctx.writeAndFlush(successPacket);

					System.out.println(
							"Pacote de estado completo da sala (0x2111) enviado para " + joiningPlayer.getNickName());

					// Notifica os outros jogadores. Isso pode acontecer em paralelo.
					room.notifyOthersPlayerJoined(joiningPlayer);

				} else {
					// Se o envio do primeiro pacote falhar, logamos o erro.
					System.err.println("Falha ao enviar pacote de preparação (0x21F5).");
					future.cause().printStackTrace();
					ctx.close();
				}
			});

			// remove o player do lobby pra entrar na sala
			GunBoundLobbyManager.getInstance().playerLeaveLobby(joiningPlayer);

		} catch (Exception e) {
			System.err.println("Erro fatal ao processar entrada na sala:");
			e.printStackTrace();
			ctx.close();
		}
	}

	/**
	 * 
	 * 
	 * Constrói o payload do pacote de sucesso ao entrar na
	 * 
	 * sala (0x2111).
	 *
	 * @param room A sala para a qual o pacote está sendo construído.
	 * @return um ByteBuf com o payload pronto.
	 */

	public static ByteBuf writeJoinRoomSuccess(GameRoom room, PlayerSession ps) {
		ByteBuf buffer = Unpooled.buffer();

		// 1. Cabeçalho do Payload
		buffer.writeShortLE(0); // RTC
		// buffer.writeShortLE(0x0100); // Valor desconhecido, mas constante
		buffer.writeByte(room.getRoomMasterSlot());
		buffer.writeByte(room.getSlotPlayer(ps));

		buffer.writeShortLE(room.getRoomId());

		// 2. Informações da Sala
		byte[] roomTitleBytes = room.getTitle().getBytes(StandardCharsets.ISO_8859_1);
		buffer.writeByte(roomTitleBytes.length);
		buffer.writeBytes(roomTitleBytes);
		buffer.writeByte(room.getMapId());

		// Escreve as configurações de jogo (modo) a partir do método corrigido
		buffer.writeIntLE(room.getGameSettings());

		// itens (dual, dual+, teleport etc)
		buffer.writeIntLE(room.getItemState());
		// pode ser um shortLE (2 bytes) ai aumenta o padding aqui em baixo
		buffer.writeBytes(new byte[] { (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF });

		// buffer.writeBytes(new byte[] { (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte)
		// 0xFF, (byte) 0xFF, (byte) 0xFF,
		// buffer.writeBytes(new byte[] { (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte)
		// 0xFF, (byte) 0xFF, (byte) 0xFF,
		// (byte) 0xFF, (byte) 0x00 });

		// buffer.writeBytes(Utils.hexStringToByteArray("1231000000001111"));//itens

		// buffer.writeBytes(room.getGameSettings()); // Configurações de jogo (4 bytes)
		// buffer.writeBytes(Utils.hexStringToByteArray("FFFFFFFFFFFFFFFF"));

		// 3. Informações de Ocupação
		buffer.writeByte(room.getCapacity());
		buffer.writeByte(room.getPlayerCount());

		// 4. Loop para cada jogador na sala
		System.out.println("[MAPA DA SALA] no RoomWriter");
		for (Map.Entry<Integer, PlayerSession> entry : room.getPlayersBySlot().entrySet()) {

			System.out.println("[Entrou no for] no RoomWriter");
			int slot = entry.getKey();
			PlayerSession player = entry.getValue();

			// avatares sendo usados
			List<PlayerAvatar> avatarWearing = player.getPlayerAvatars().stream()
					.filter(av -> "1".equals(av.getWearing())).collect(Collectors.toList());

			System.out.println(
					"{DEBUG} LOOPING NO {writeJoinRoomSuccess}: " + player.getNickName() + " SLOT: [" + slot + "]");

			buffer.writeByte(slot);
			buffer.writeBytes(Utils.resizeBytes(player.getNickName().getBytes(StandardCharsets.ISO_8859_1), 12));
			buffer.writeByte(player.getGender()); // gender
			// buffer.writeBytes(new byte[] { 01 }); // gender

			try {
				// Obtém o endereço remoto do jogador
				InetSocketAddress remoteAddress = (InetSocketAddress) player.getPlayerCtxChannel().remoteAddress();

				// Pega os bytes do IP DIRETAMENTE do objeto InetAddress, sem conversão para
				// String Hex.
				byte[] ipBytes = remoteAddress.getAddress().getAddress();

				System.out.println("[DEBUG] IP DO PLAYER: " + remoteAddress.getAddress().getHostAddress());

				// Escreve os bytes do IP no buffer.
				buffer.writeBytes(ipBytes);
				buffer.writeShort(19363); // Porta UDP hardcoded
				buffer.writeBytes(ipBytes);
				buffer.writeShort(19363);
			} catch (Exception e) {
				// Fallback em caso de erro, escreve 12 bytes nulos.
				buffer.writeBytes(new byte[12]);
			}

			buffer.writeByte(player.getRoomTankPrimary());
			buffer.writeByte(player.getRoomTankSecondary());
			// buffer.writeByte(player.getRoomTeam());

			// buffer.writeByte(0xFF); // Mobile Primário (0xFF = aleatório)
			// buffer.writeByte(0xFF); // Mobile Secundário (0xFF = aleatório)
			buffer.writeByte(player.getRoomTeam()); // Time (0=A, 1=B)

			buffer.writeBytes(Utils.hexStringToByteArray("01"));

			// Avatar, Guilda e Ranks
			// buffer.writeBytes(Utils.hexStringToByteArray(player.getAvatarEquipped()));
			// buffer.writeBytes(new byte[8]);

			// buffer.writeBytes(Utils.hexStringToByteArray("008000"));
			buffer.writeBytes(Utils.resizeBytes(player.getGuild().getBytes(StandardCharsets.ISO_8859_1), 8));
			buffer.writeShortLE(player.getRankCurrent());
			buffer.writeShortLE(player.getRankSeason());

			if (avatarWearing.size() < 1) {
				buffer.writeBytes(Utils.hexStringToByteArray("02000000000000000000"));
			} else {
				buffer.writeByte(avatarWearing.size());
				buffer.writeBytes(new byte[] { 00 });
				avatarWearing.forEach(avatar -> {
					buffer.writeBytes(PacketUtils.intToBytes(avatar.getItem(), 3, false));
					buffer.writeBytes(new byte[] { 00 });
				});
			}

			buffer.writeBytes(new byte[] { 01 });

			// buffer.writeBytes(Utils.hexStringToByteArray("00022003"));//ex-item?
			// buffer.writeBytes(Utils.hexStringToByteArray("00000000"));//ex-item?
			// buffer.writeByte(04);
			// buffer.writeBytes(Utils.hexStringToByteArray("00018001"));// head
			// buffer.writeBytes(Utils.hexStringToByteArray("00018000"));// body
			// buffer.writeBytes(Utils.hexStringToByteArray("00058002"));// glass
			// buffer.writeBytes(Utils.hexStringToByteArray("00018003"));// Flag
			// buffer.writeBytes(Utils.hexStringToByteArray("00022003"));//ex-item?
			// buffer.writeBytes(Utils.hexStringToByteArray("00000000"));//ex-item?
			// buffer.writeBytes(new byte[] { 00, 01 });

			// buffer.writeBytes(Utils.hexStringToByteArray(player.getAvatarEquipped()));
			// buffer.writeByte(0x08);
			// buffer.writeBytes(Utils.hexStringToByteArray("0080008000800000"));//avatar
			// buffer.writeBytes(Utils.hexStringToByteArray("8000000000000000"));
			// buffer.writeBytes(new byte[] {00,00,00});
			// buffer.writeBytes(Utils.hexStringToByteArray("FFFFFF"));
		}

		// 5. MOTD da sala no final
		String motd = new String("*Jogue Limpo!");
		byte[] motdBytes = motd.getBytes(StandardCharsets.ISO_8859_1);

		// Escreve os bytes da MOTD DIRETAMENTE, SEM um prefixo de tamanho.
		buffer.writeBytes(motdBytes);

		return buffer;
	}

}