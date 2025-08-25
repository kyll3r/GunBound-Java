package br.com.gunbound.emulator.packets.writers.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import br.com.gunbound.emulator.model.entities.game.PlayerSession;
import br.com.gunbound.emulator.utils.Utils;

public class JoinChannelSuccessPacket {
	// Opcode para o pacote. 
	private static final short OPCODE = 0x2001;

	private String nickNamePlayer;
	private int desiredChannelId;
	private int highestChannelPosition;
	private List<PlayerSession> activeChannelUsers;
	private String motdChannel;
	private String clientVersion;

	// Construtor principal para o pacote
	public JoinChannelSuccessPacket(int desiredChannelId, int highestChannelPosition,
			List<PlayerSession> activeChannelUsers, String motdChannel, String clientVersion, String nickNamePlayer) {
		this.desiredChannelId = desiredChannelId;
		this.highestChannelPosition = highestChannelPosition;
		this.activeChannelUsers = activeChannelUsers;
		this.motdChannel = motdChannel;
		this.clientVersion = clientVersion;
		this.nickNamePlayer = nickNamePlayer;
	}

	// Getters para acessar os dados do pacote
	public static short getOpcode() {
		return OPCODE;
	}

	public int getDesiredChannelId() {
		return desiredChannelId;
	}

	public int getHighestChannelPosition() {
		return highestChannelPosition;
	}

	public List<PlayerSession> getActiveChannelUsers() {
		return activeChannelUsers;
	}
	
	public String getNickNamePlayer() {
		return nickNamePlayer;
	}

	// Constrói a mensagem estendida do dia (MOTD) conforme o Python
	public String getExtendedChannelMotd() {
		
		String msgPlayer = Utils.msgByHour()+ nickNamePlayer;
		
		return motdChannel + "\r\n" + msgPlayer + "\r\nRequesting SVC_CHANNEL_JOIN " + (desiredChannelId+1) + " at "
				+ LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "\r\n"
				+ "Client Version: " + clientVersion;
	}

}