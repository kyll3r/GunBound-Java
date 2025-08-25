package br.com.gunbound.emulator.packets.readers.shop;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import br.com.gunbound.emulator.handlers.GameAttributes;
import br.com.gunbound.emulator.model.DAO.ChestDAO;
import br.com.gunbound.emulator.model.DAO.DAOFactory;
import br.com.gunbound.emulator.model.DAO.UserDAO;
import br.com.gunbound.emulator.model.entities.DTO.ChestDTO;
import br.com.gunbound.emulator.model.entities.DTO.MenuDTO;
import br.com.gunbound.emulator.model.entities.game.GameMenu;
import br.com.gunbound.emulator.model.entities.game.PlayerSession;
import br.com.gunbound.emulator.utils.PacketUtils;
import br.com.gunbound.emulator.utils.crypto.GunBoundCipher;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;

public class AvatarPlayerBuyReader {

	private static final int OPCODE_REQUEST = 0x6020;
	private static final int OPCODE_CONFIRMATION = 0x6021;

	public static void read(ChannelHandlerContext ctx, byte[] payload) {
		System.out.println("RECV> SVC_PROP_BUY (0x" + Integer.toHexString(OPCODE_REQUEST) + ")");
		PlayerSession player = ctx.channel().attr(GameAttributes.USER_SESSION).get();
		byte[] authToken = ctx.channel().attr(GameAttributes.AUTH_TOKEN).get();

		ChestDAO factoryChestDAO = DAOFactory.CreateChestDao();
		UserDAO factoryUserDAO = DAOFactory.CreateUserDao();

		if (player == null)
			return;

		ByteBuf request = Unpooled.buffer();
		try {

			byte[] decryptedPayload = GunBoundCipher.gunboundDynamicDecrypt(payload, player.getUserNameId(),
					player.getPassword(), authToken, OPCODE_REQUEST);

			request = Unpooled.wrappedBuffer(decryptedPayload);

			// 1. Lê o cabeçalho do payload
			int avatarCode = request.readIntLE(); // O primeiro byte é a quantidade
			// request.skipBytes(1); // Pula o byte 0x00 de padding
			int goldOrCash = request.readByte(); // é Gold ou é cash?

			GameMenu gameMenu = GameMenu.getInstance();

			MenuDTO avatarData = gameMenu.getByNo(avatarCode);

			// indice do avatar inserido
			Integer idNewAvatarOnChest = null;

			if (avatarData != null) {
				int priceAvatar = avatarData.getPriceByGoldForI();
				String highestPlaceOrder = player.getAvatarWithHighestPlaceOrder().getPlaceOrder();

				// calculo do place order (util apenas no WC pra frente)
				if (highestPlaceOrder == null) {
					highestPlaceOrder = "0";
				} else {
					highestPlaceOrder = Integer.toString((Integer.parseInt(highestPlaceOrder) + 10000));
				}

				ChestDTO avatarBought = new ChestDTO();
				avatarBought.setItem(avatarCode);
				avatarBought.setWearing(Integer.toString(0));

				if (avatarCode == 204802) {
					avatarBought.setItem(204801);//fixa o codigo do PU
					avatarBought.setExpire(Timestamp.valueOf(LocalDateTime.now().plusDays(7)));
				} else if (avatarCode == 204803) {
					avatarBought.setItem(204801);//fixa o codigo do PU
					avatarBought.setExpire(Timestamp.valueOf(LocalDateTime.now().plusDays(14)));
				} else if (avatarCode == 204804) {
					avatarBought.setItem(204801);//fixa o codigo do PU
					avatarBought.setExpire(Timestamp.valueOf(LocalDateTime.now().plusDays(30)));
				} else {
					avatarBought.setExpire(null);
				}
				avatarBought.setVolume(avatarData.getVolume1());
				avatarBought.setPlaceOrder(highestPlaceOrder);
				avatarBought.setRecovered("0");
				avatarBought.setOwnerId(player.getUserNameId());
				avatarBought.setExpireType("I");

				if (goldOrCash == 0) {// Comprado com Gold
					avatarBought.setAcquisition("G");
					factoryUserDAO.updateMinusGold(player.getUserNameId(), priceAvatar);
					player.setGold(player.getGold() - priceAvatar);

				} else if (goldOrCash == 1) {// Comprado com Cash
					avatarBought.setAcquisition("C");
					factoryUserDAO.updateMinusCash(player.getUserNameId(), priceAvatar);
					player.setCash(player.getCash() - priceAvatar);
				}

				idNewAvatarOnChest = factoryChestDAO.insert(avatarBought);

				ChestDTO avatar = factoryChestDAO.getByIdx(idNewAvatarOnChest);
				avatar.setItem(avatarCode);
				
				// chama o metodo para escrever o avatar no shopping
				//writeNewAvatar(ctx, factoryChestDAO, idNewAvatarOnChest); // desativado pelo hacky do PU
				writeNewAvatar(ctx, avatar);

				System.out.println("Recebida pedido de compra do Avatar : " + avatarData.getMenuName() + " [ "
						+ avatarCode + "] " + " Moeda: " + (goldOrCash == 0 ? "Gold" : "Cash") + " do jogador "
						+ player.getNickName());

			} else {
				System.err.println("Não foi encontrado o avatar solicitado. ID: [" + avatarCode + "]");
			}

		} catch (Exception e) {
			System.err.println("Erro ao processar Compra de avatar");
			e.printStackTrace();
		} finally {
			request.release();
		}

		/*
		 * Envia o pacote int txSum =
		 * player.getPlayerCtx().attr(GameAttributes.PACKET_TX_SUM).get(); ByteBuf
		 * finalPacket = PacketUtils.generatePacket(txSum, OPCODE_CONFIRMATION, //
		 * Unpooled.wrappedBuffer(Utils.hexStringToByteArray(new String("00 00 01 00 C8
		 * // 02 00 00 73 80 01 00").replace(" ","")))); //
		 * Unpooled.wrappedBuffer(Utils.hexStringToByteArray(new String("00 00 01 00 00
		 * // 00 00 00 04 20 03 00").replace(" ","")))); Unpooled.wrappedBuffer(Utils
		 * .hexStringToByteArray(new
		 * String("00 00 01 00 00 00 00 00 04 20 03 00").replace(" ", ""))));
		 * player.getPlayerCtx().writeAndFlush(finalPacket);
		 */

	}

	//Hacky por causa do PU
	//private static void writeNewAvatar(ChannelHandlerContext ctx, ChestDAO chestDao, int idxAvatar) {
		private static void writeNewAvatar(ChannelHandlerContext ctx, ChestDTO chestNew) {
		System.out.println("SEND> SVC_ITEM_CONFIRMATION (0x" + Integer.toHexString(OPCODE_CONFIRMATION) + ")");
		PlayerSession player = ctx.channel().attr(GameAttributes.USER_SESSION).get();

		//ChestDTO avatar = chestDao.getByIdx(idxAvatar);
		ChestDTO avatar = chestNew;
		if (player == null || avatar == null)
			return;

		int avatarCount = 1; // hardcoded nao implementou os sets ainda

		ByteBuf avatarData = Unpooled.buffer();
		try {
			avatarData.writeShort(0);
			avatarData.writeByte(1);
			avatarData.writeByte(0);

			// 3. Itera sobre os dados dos avatares
			for (int i = 0; i < avatarCount; i++) {
				avatarData.writeIntLE(avatar.getIdx());
				avatarData.writeIntLE(avatar.getItem());
			}

		} catch (Exception e) {
			System.err.println("Erro ao processar Adição de  de avatar(es) no shopping :");
			e.printStackTrace();
		}

		// Envia o pacote
		//int txSum = player.getPlayerCtx().attr(GameAttributes.PACKET_TX_SUM).get();
		ByteBuf finalPacket = PacketUtils.generatePacket(player, OPCODE_CONFIRMATION, avatarData,false);

		player.getPlayerCtx().writeAndFlush(finalPacket);

	}
		
		//packet para erro no shop
		// 09 00 A0 6C 21 60 04 00  00 

}