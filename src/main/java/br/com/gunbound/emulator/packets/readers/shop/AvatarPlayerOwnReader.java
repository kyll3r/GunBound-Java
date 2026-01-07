package br.com.gunbound.emulator.packets.readers.shop;

import br.com.gunbound.emulator.handlers.GameAttributes;
import br.com.gunbound.emulator.model.DAO.ChestDAO;
import br.com.gunbound.emulator.model.DAO.DAOFactory;
import br.com.gunbound.emulator.model.entities.game.PlayerAvatar;
import br.com.gunbound.emulator.model.entities.game.PlayerSession;
import br.com.gunbound.emulator.utils.PacketUtils;
import br.com.gunbound.emulator.utils.TimestampRemaining;
import br.com.gunbound.emulator.utils.Utils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;

public class AvatarPlayerOwnReader {

	private static final int OPCODE_REQUEST = 0x6000;
	private static final int OPCODE_CONFIRMATION = 0x6001;

	public static void read(ChannelHandlerContext ctx, byte[] payload) {
		System.out.println("RECV> SVC_PROP_GET (0x" + Integer.toHexString(OPCODE_REQUEST) + ")");
		PlayerSession player = ctx.channel().attr(GameAttributes.USER_SESSION).get();
		if (player == null)
			return;

		// 2. Busca o usuário no "banco de dados". e armazena na sessão.
		ChestDAO factory = DAOFactory.CreateChestDao();

		player.getPlayerAvatars().clear();// Limpar a lista de avatares pra nao duplicar no cliente
		factory.getAllAvatarsByOwnerId(player.getUserNameId()).forEach(avatar -> {
			player.getPlayerAvatars().add(new PlayerAvatar(avatar));

		});

		int qtdAvatars = player.getPlayerAvatars().size();

		ByteBuf buffer = Unpooled.buffer();

		buffer.writeBytes(new byte[] { 00, 00 });
		buffer.writeByte(qtdAvatars);
		buffer.writeBytes(new byte[] { 00 });

		player.getPlayerAvatars().forEach(avatar -> {
			buffer.writeIntLE(avatar.getIdx());
			buffer.writeIntLE(avatar.getItem());
			buffer.writeByte(Integer.parseInt(avatar.getWearing()));
			
			if (avatar.getItem() == 204801) {
			buffer.writeBytes(new byte[] { 0x00});
			buffer.writeByte(avatar.getVolume());
		        // Calcula o timestamp restante em segundos
				buffer.writeIntLE(TimestampRemaining.calcTimestampRemaining(avatar.getExpire()));
				buffer.writeBytes(new byte[] { 0x00});
			} else {
				buffer.writeBytes(new byte[] { 0x00, 0x00 });
				buffer.writeByte(avatar.getVolume());
				buffer.writeIntLE(Integer.parseInt(avatar.getPlaceOrder()));
			}
			buffer.writeBytes(new byte[] { 00 });

		});

		// Envia o pacote
		//int txSum = player.getPlayerCtx().attr(GameAttributes.PACKET_TX_SUM).get();
		ByteBuf finalPacket = PacketUtils.generatePacket(player, OPCODE_CONFIRMATION, buffer,false);

		// Thread.sleep(150);
		player.getPlayerCtxChannel().writeAndFlush(finalPacket);

	}

	public static void test(ChannelHandlerContext ctx, byte[] payload) {
		System.out.println("RECV> SVC_PROP_GET (0x" + Integer.toHexString(OPCODE_REQUEST) + ")");
		PlayerSession player = ctx.channel().attr(GameAttributes.USER_SESSION).get();
		if (player == null)
			return;

		ByteBuf buffer = Unpooled.buffer();

		buffer.writeBytes(new byte[] { 00, 00 });
		buffer.writeByte(07);
		buffer.writeBytes(new byte[] { 00 });

		// IDBANCOD IDAVATAR AT ?? TT TPEXPIRA ?? ??
		// 12100000 01200300 00 00 01 063B0900 00 00
		buffer.writeBytes(Utils.hexStringToByteArray("12131415018001000000000110270000"));// head
		buffer.writeBytes(new byte[] { 00 });
		buffer.writeBytes(Utils
				.hexStringToByteArray(new String("12 13 14 99 5F 80 01 00 01 00 00 01 00 00 00 00").replace(" ", "")));
		buffer.writeBytes(new byte[] { 00 });
		buffer.writeBytes(Utils.hexStringToByteArray("223344550180000001000001204e0000"));// body
		buffer.writeBytes(new byte[] { 00 });
		buffer.writeBytes(Utils.hexStringToByteArray("324354651520030000000000409C0000"));// FORCE
		buffer.writeBytes(new byte[] { 00 });
		buffer.writeBytes(Utils.hexStringToByteArray("02030405D08003000000000130750000"));// Flag
		buffer.writeBytes(new byte[] { 00 });
		buffer.writeBytes(Utils
				.hexStringToByteArray(new String("52 53 54 55 16 20 03 00 00 00 00 00 50 C3 00 00").replace(" ", "")));// Lighting
		buffer.writeBytes(new byte[] { 00 });
		buffer.writeBytes(Utils
				.hexStringToByteArray(new String("00 00 00 00 01 20 03 00 00 00 01 06 3B 09 00 00").replace(" ", "")));// PU
		// buffer.writeBytes(Utils.hexStringToByteArray(new String("00 00 00 00 01 20 03
		// 00 00 00 01 E9 8C 27 00 00 00").replace(" ","")));// PU
		// buffer.writeBytes(Utils.hexStringToByteArray(new String("00 00 00 00 01 20 03
		// 00 00 00 00 00 00 00 00 00 00").replace(" ","")));// PU
		// buffer.writeBytes(Utils.hexStringToByteArray(new String("BD 02 00 00 04 20 03
		// 00 00 00 00 43 01 46 89 27 00 ").replace(" ","")));

		// PU WC -> weird
		// buffer.writeBytes(Utils.hexStringToByteArray(new String("BE 02 00 00 01 20 03
		// 00 00 43 01 E9 8C 27 00 01 01 00 00 00 01 00 00 00 00 00").replace(" ","")));

		// Envia o pacote criptografado
		//int txSum = player.getPlayerCtx().attr(GameAttributes.PACKET_TX_SUM).get();
		ByteBuf finalPacket = PacketUtils.generatePacket(player, OPCODE_CONFIRMATION, buffer,false);

		// Thread.sleep(150);
		player.getPlayerCtxChannel().writeAndFlush(finalPacket);

	}

}