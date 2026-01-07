package br.com.gunbound.emulator.packets.readers.shop;

import java.util.List;

import br.com.gunbound.emulator.handlers.GameAttributes;
import br.com.gunbound.emulator.model.DAO.ChestDAO;
import br.com.gunbound.emulator.model.DAO.DAOFactory;
import br.com.gunbound.emulator.model.entities.game.PlayerAvatar;
import br.com.gunbound.emulator.model.entities.game.PlayerSession;
import br.com.gunbound.emulator.utils.PacketUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;

public class AvatarPlayerSortReader {

	private static final int OPCODE_REQUEST = 0x600A;
	private static final int OPCODE_CONFIRMATION = 0x600B;

	public static void read(ChannelHandlerContext ctx, byte[] payload) {
		System.out.println("RECV> SVC_ITEM_SORT (0x" + Integer.toHexString(OPCODE_REQUEST) + ")");
		PlayerSession player = ctx.channel().attr(GameAttributes.USER_SESSION).get();
		if (player == null)
			return;

		// 2. Busca o usuário no "banco de dados". e armazena na sessão.
		ChestDAO factory = DAOFactory.CreateChestDao();

		ByteBuf request = Unpooled.wrappedBuffer(payload);
		try {
			// 1. Lê o cabeçalho do payload
			int avatarCount = request.readUnsignedByte(); // O primeiro byte é a quantidade
			request.skipBytes(1); // Pula o byte 0x00 de padding

			System.out.println(
					"Recebida reordenação para " + avatarCount + " avatares do jogador " + player.getNickName());

			// 2. Cria uma lista para armazenar as informações
			List<PlayerAvatar> reorderedAvatars = player.getPlayerAvatars();

			// 3. Itera sobre os dados dos avatares
			for (int i = 0; i < avatarCount; i++) {
				// Lê o bloco de 8 bytes para cada avatar
				int avatarId = request.readIntLE();
				int newPosition = request.readIntLE();


				PlayerAvatar avatarToUpdate = reorderedAvatars.stream()
					    .filter(a -> a.getIdx() == avatarId)
					    .findFirst()
					    .orElse(null);

                // 5. Se o avatar for encontrado, atualiza sua posição.
                if (avatarToUpdate != null) {
                    avatarToUpdate.setPlaceOrder(String.valueOf(newPosition));
                    
                    boolean sucesso = factory.updatePlaceOrder(avatarId, String.valueOf(newPosition));
                    if (sucesso) {
                        System.out.println("PlaceOrder atualizado com sucesso.");
                    } else {
                        System.out.println("Falha ao atualizar PlaceOrder.");
                    }
                    
                    System.out.println(" -> Avatar ID " + avatarId + " movido para a posição " + newPosition);
                } else {
                    System.err.println(" -> AVISO: O jogador " + player.getNickName() + " tentou reordenar um avatar (ID: " + avatarId + ") que não possui.");
                }
			}

			// 4. (Para Debug) Imprime a lista de avatares reordenados
			System.out.println("Lista de reordenação recebida:");
			for (PlayerAvatar info : reorderedAvatars) {
				System.out.println(" -> " + info.getIdx() + " POSITION: " + info.getPlaceOrder());
			}

		} catch (Exception e) {
			System.err.println("Erro ao processar reordenação de avatares:");
			e.printStackTrace();
		} finally {
			request.release();
		}

		// Envia o pacote
		//int txSum = player.getPlayerCtx().attr(GameAttributes.PACKET_TX_SUM).get();
		ByteBuf finalPacket = PacketUtils.generatePacket(player, OPCODE_CONFIRMATION, Unpooled.EMPTY_BUFFER,false);

		// Thread.sleep(150);
		player.getPlayerCtxChannel().writeAndFlush(finalPacket);

	}

}