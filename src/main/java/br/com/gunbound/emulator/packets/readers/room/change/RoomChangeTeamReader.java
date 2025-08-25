package br.com.gunbound.emulator.packets.readers.room.change;

import br.com.gunbound.emulator.handlers.GameAttributes;
import br.com.gunbound.emulator.model.entities.game.PlayerSession;
import br.com.gunbound.emulator.room.GameRoom;
import br.com.gunbound.emulator.utils.PacketUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;

public class RoomChangeTeamReader {

	private static final int OPCODE_REQUEST = 0x3210;
	private static final int OPCODE_CONFIRMATION = 0x3211;

	public static void read(ChannelHandlerContext ctx, byte[] payload) {
		System.out.println("RECV> SVC_ROOM_SELECT_TEAM (0x" + Integer.toHexString(OPCODE_REQUEST) + ")");
		PlayerSession player = ctx.channel().attr(GameAttributes.USER_SESSION).get();
		if (player == null)
			return;

		GameRoom room = player.getCurrentRoom();
		if (room == null) {
			return;
		}

		// Empacota toda a lógica em um Runnable e submeta para a fila da sala!
		room.submitAction(() -> processSelectTeam(payload, player, room));
	}

	private static void processSelectTeam(byte[] payload, PlayerSession player,
			GameRoom room) {

		ByteBuf request = Unpooled.wrappedBuffer(payload);
		try {
			// 1. A nova posição de time é o primeiro byte do payload.
			int newTeam = request.readUnsignedByte();

			// 2. Atualiza o time na sessão do jogador.
			player.setRoomTeam(newTeam);
			System.out.println(player.getNickName() + " mudou para o time " + (newTeam == 0 ? "A" : "B"));

			// 3. Envia um pacote de confirmação VAZIO de volta ao jogador (0x3211).
			//int playerTxSum = player.getPlayerCtx().attr(GameAttributes.PACKET_TX_SUM).get();
			ByteBuf confirmationPacket = PacketUtils.generatePacket(player, OPCODE_CONFIRMATION,
					Unpooled.EMPTY_BUFFER, true);
			player.getPlayerCtx().writeAndFlush(confirmationPacket);

			// 4. Pede para a sala notificar todos os jogadores sobre a atualização geral.
			room.broadcastRoomUpdate();

			// Pede para a sala notificar todos com o ESTADO COMPLETO.
			// room.broadcastFullState(); acho que nao tem necessidade disso nao

		} catch (Exception e) {
			System.err.println("Erro ao processar mudança de time:");
			e.printStackTrace();
		} finally {
			request.release();
		}
	}
}