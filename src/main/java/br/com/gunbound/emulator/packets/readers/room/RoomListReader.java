package br.com.gunbound.emulator.packets.readers.room;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import br.com.gunbound.emulator.handlers.GameAttributes;
import br.com.gunbound.emulator.model.entities.game.PlayerSession;
import br.com.gunbound.emulator.packets.writers.RoomWriter;
import br.com.gunbound.emulator.room.GameRoom;
import br.com.gunbound.emulator.room.RoomManager;
import br.com.gunbound.emulator.utils.PacketUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;

public class RoomListReader {

	private static final int OPCODE_REQUEST = 0x2100;
	private static final int OPCODE_RESPONSE = 0x2103;
	// cliente exibe 6 salas por página.
	private static final int ROOMS_PER_PAGE = 6;

	public static void read(ChannelHandlerContext ctx, byte[] payload) {
		System.out.println("RECV> SVC_ROOM_SORTED_LIST (0x" + Integer.toHexString(OPCODE_REQUEST) + ")");
		PlayerSession player = ctx.channel().attr(GameAttributes.USER_SESSION).get();
		if (player == null)
			return;

		ByteBuf request = Unpooled.wrappedBuffer(payload);
		try {
			// 1. Decodificar o filtro e o índice inicial da página
			int filterMode = request.readUnsignedByte();
			int startIndex = 0; // O índice inicial da sala a ser exibida. Padrão é 0.
			if (request.isReadable()) {
				// O cliente envia o índice da primeira sala da página (0, 6, 12, etc.)
				startIndex = request.readUnsignedByte();
			}

			String filterName = filterMode == 1 ? "ALL" : (filterMode == 2 ? "WAITING" : "UNKNOWN");
			System.out.println("Filtro de sala: " + filterName + ", Índice Inicial Solicitado: " + startIndex);

			// 2. Obter todas as salas e aplicar o filtro
			Collection<GameRoom> allRooms = RoomManager.getInstance().getAllRooms();
			List<GameRoom> filteredRooms; // Usar List para permitir a criação de sub-listas

			if (filterMode == 2) { // Apenas salas esperando (WAITING)
				filteredRooms = allRooms.stream().filter(room -> !room.isGameStarted()).collect(Collectors.toList());
			} else { // ALL ou UNKNOWN
				filteredRooms = new ArrayList<>(allRooms);
			}

			// 3. Calcular a paginação com base no índice inicial
			int totalRooms = filteredRooms.size();
			int endIndex = Math.min(startIndex + ROOMS_PER_PAGE, totalRooms);

			List<GameRoom> roomsForPage;
			if (startIndex >= totalRooms) {
				roomsForPage = Collections.emptyList(); // A página solicitada está fora dos limites
			} else {
				roomsForPage = filteredRooms.subList(startIndex, endIndex);
			}

			// 4. Construir e enviar o pacote de resposta
			//int playerTxSum = player.getPlayerCtx().attr(GameAttributes.PACKET_TX_SUM).get();

			// A chamada passa a lista da página atual
			ByteBuf responsePayload = RoomWriter.writeRoomList(roomsForPage);

			ByteBuf responsePacket = PacketUtils.generatePacket(player, OPCODE_RESPONSE, responsePayload, true);

			ctx.writeAndFlush(responsePacket);

		} catch (Exception e) {
			System.err.println("Erro ao processar a lista de salas");
			e.printStackTrace();
		} finally {
			request.release();
		}
	}
}
