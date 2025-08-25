package br.com.gunbound.emulator.packets.writers;

import java.nio.charset.StandardCharsets;

import br.com.gunbound.emulator.model.entities.game.PlayerSession;
import br.com.gunbound.emulator.utils.Utils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class PacketWriter {

	public static ByteBuf writeJoinNotification(PlayerSession player) {
		ByteBuf buffer = Unpooled.buffer();

		System.out.println("DEBUG : ChannelPosition no writeJoinNotification: " + player.getChannelPosition());

		// Posição (slot) do jogador no canal.
		buffer.writeByte(player.getChannelPosition());

		System.out.println(
				"DEBUG : ChannelPosition no writeJoinNotification (byte) : " + (byte) player.getChannelPosition());

		// Nickname (13 bytes)
		buffer.writeBytes(Utils.resizeBytes(player.getNickName().getBytes(StandardCharsets.ISO_8859_1), 12));
		buffer.writeByte(player.getGender());
		// Guild (8 bytes)
		buffer.writeBytes(Utils.resizeBytes(player.getGuild().getBytes(StandardCharsets.ISO_8859_1), 8));

		// Avatar Equipado (a string hexadecimal é convertida para bytes)
		// O código de referência usa 8 bytes para o avatar aqui.
		// byte[] avatarBytes = Utils.hexStringToByteArray(player.getAvatarEquipped());
		// buffer.writeBytes(Utils.resizeBytes(avatarBytes, 8));

		// Ranks (2 bytes cada)
		buffer.writeShortLE(player.getRankCurrent());
		buffer.writeShortLE(player.getRankSeason());

		// PPNNNNNNNNNNNNNNNNNNNNNNNNAAAAAAAAAAAAAAAAGGGGGGGGGGGGGGGGRCRCRSRS
		// buffer.writeBytes(Utils.hexStringToByteArray(("014142434445464748494A4B4C|14|4E4F50515253544E|565857595A|4142434445464748494A4B4C4D|4E4F505152535455|565857595A").replace("|",
		// "")));
		// buffer.writeBytes(Utils.hexStringToByteArray(("014142434445464748494A4B4C|00|4E4F50515253544E|565857595A|4142434445464748494A4B4C4D|4E4F505152535455|565857595A").replace("|",
		// "")));

		return buffer;
	}

}