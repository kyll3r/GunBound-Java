package br.com.gunbound.emulator.packets.readers.shop;

import br.com.gunbound.emulator.handlers.GameAttributes;
import br.com.gunbound.emulator.model.DAO.ChestDAO;
import br.com.gunbound.emulator.model.DAO.DAOFactory;
import br.com.gunbound.emulator.model.entities.game.PlayerSession;
import br.com.gunbound.emulator.utils.PacketUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;

public class AvatarPlayerConfirmReader {

	private static final int OPCODE_REQUEST = 0x600E;
	private static final int OPCODE_CONFIRMATION = 0x600F;

	public static void read(ChannelHandlerContext ctx, byte[] payload) {
		System.out.println("RECV> SVC_PROP_WEARING (0x" + Integer.toHexString(OPCODE_REQUEST) + ")");
		PlayerSession player = ctx.channel().attr(GameAttributes.USER_SESSION).get();

		// 2. Busca o usuário no "banco de dados". e armazena na sessão.
		ChestDAO factory = DAOFactory.CreateChestDao();

		ByteBuf request = Unpooled.wrappedBuffer(payload);
		try {

			// Zera tudo que ta vestindo
			factory.updateAvatarWearing(null, null, player.getUserNameId());

			// 1. Lê o cabeçalho do payload
			int avatarCount = request.readUnsignedByte(); // O primeiro byte é a quantidade
			request.skipBytes(1); // Pula o byte 0x00 de padding

			boolean isSucess = false;
			// 3. Itera sobre os dados dos avatares
			for (int i = 0; i < avatarCount; i++) {
				// Lê o bloco de 8 bytes para cada avatar
				int avatarId = request.readIntLE();//hacky das posições 
				int iSwearing = request.readByte();
				isSucess = factory.updateAvatarWearing(avatarId, Integer.toString(iSwearing), null);

				if (isSucess) {
					System.out.println("Avatar: ["+avatarId+"] Wearing: "+iSwearing+" updated sucessfully");
				} else {
					{
						System.out.println("Avatar: ["+avatarId+"] Wearing: "+iSwearing+" not updated");
					}
				}

			}

		} catch (Exception e) {
			System.err.println("Erro ao processar reordenação de avatares:");
			e.printStackTrace();
		} finally {
			request.release();
		}

		ByteBuf buffer = Unpooled.EMPTY_BUFFER;

		// Envia o pacote criptografado
		//int txSum = player.getPlayerCtx().attr(GameAttributes.PACKET_TX_SUM).get();
		ByteBuf finalPacket = PacketUtils.generatePacket(player, OPCODE_CONFIRMATION, buffer, true);

		// Thread.sleep(150);
		player.getPlayerCtx().writeAndFlush(finalPacket);

	}

}