package br.com.gunbound.emulator.model.entities.game;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import br.com.gunbound.emulator.handlers.GameAttributes;
import br.com.gunbound.emulator.lobby.GunBoundLobby;
import br.com.gunbound.emulator.model.entities.DTO.UserDTO;
import br.com.gunbound.emulator.room.GameRoom;
import io.netty.channel.Channel;

public class PlayerSession {
	private String userId;
	private String nickName;

	private int gender;

	private String password;
	private String guild;
	private int rankCurrent;
	private int rankSeason;

	private int totalScore;
	private int seasonScore;

	private int totalRank;
	private int seasonRank;

	private int memberGuildCount;
	private int guildRank;

	private int cash;
	private int gold;

	// Avatares
	private List<PlayerAvatar> playerAvatars = new ArrayList<PlayerAvatar>();

	// registrar o contexto do camarada aqui
	private Channel ctx;

	// Gerencia do Lobby
	private GunBoundLobby currentLobby;
	private int channelPosition = -1;

	// Gerencia do Room
	private GameRoom currentRoom;
	private int roomTankPrimary = 0xFF; // Inicia como Random
	private int roomTankSecondary = 0xFF; // Inicia como Random
	private int roomTeam = 0; // 0 para Time A, 1 para Time B. Padrão A.
	private int isAlive = 1;

	// Construtor para Iniciar o PlayerSession
	public PlayerSession() {
	}

	// Construtor que usa os dados que vieram do Banco
	public PlayerSession(UserDTO user) {

		this.userId = user.getUserId();
		this.nickName = user.getNickname();
		this.gender = user.getGender();
		this.password = user.getPassword();
		this.guild = user.getGuild();
		// Score
		this.totalScore = user.getTotalScore();
		this.seasonScore = user.getSeasonScore();
		// Level
		this.rankCurrent = user.getTotalGrade();
		this.rankSeason = user.getSeasonGrade();
		// Ranking
		this.totalRank = user.getTotalRank();
		this.seasonRank = user.getSeasonRank();

		// guild
		this.memberGuildCount = user.getMemberGuildCount();
		this.guildRank = user.getGuildRank();

		this.gold = user.getGold();
		this.cash = user.getCash();
		this.channelPosition = -1;

		System.out.println("PlayerSession: Jogador: " + this.nickName + " Adicionado na Sessão");
	}

	// Construtor que usa os dados que vieram do Banco
	public PlayerSession(UserDTO user, Channel ctx) {

		// Chama o construtor acima
		this(user);
		// Seta o contexto
		this.ctx = ctx;

	}

	public String getUserNameId() {
		return userId;
	}

	public void setUserNameId(String userId) {
		this.userId = userId;
	}

	public String getNickName() {
		return nickName;
	}

	public void setNickName(String nickName) {
		this.nickName = nickName;
	}

	public int getGender() {
		return gender;
	}

	public void setGender(int gender) {
		this.gender = gender;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getGuild() {
		return guild;
	}

	public int getTotalScore() {
		return totalScore;
	}

	public void setTotalScore(int totalScore) {
		this.totalScore = totalScore;
	}

	public int getSeasonScore() {
		return seasonScore;
	}

	public void setSeasonScore(int seasonScore) {
		this.seasonScore = seasonScore;
	}

	public int getRankCurrent() {
		return rankCurrent;
	}

	public int getRankSeason() {
		return rankSeason;
	}

	public int getTotalRank() {
		return totalRank;
	}

	public void setTotalRank(int totalRank) {
		this.totalRank = totalRank;
	}

	public int getSeasonRank() {
		return seasonRank;
	}

	public void setSeasonRank(int seasonRank) {
		this.seasonRank = seasonRank;
	}

	public List<PlayerAvatar> getPlayerAvatars() {
		return playerAvatars;
	}

	public void setPlayerAvatars(List<PlayerAvatar> playerAvatars) {
		this.playerAvatars = playerAvatars;
	}

	public GunBoundLobby getCurrentLobby() {
		return currentLobby;
	}

	public void setCurrentLobby(GunBoundLobby currentLobby) {
		this.currentLobby = currentLobby;
	}

	// Getters para acessar os dados do jogador
	public int getChannelPosition() {
		return channelPosition;
	}

	public void setChannelPosition(int pos) {
		this.channelPosition = pos;
	}

	public int getCash() {
		return cash;
	}
	
	public void setCash(int cash) {
		this.cash = cash;
	}

	public int getGold() {
		return gold;
	}

	public void setGold(int gold) {
		this.gold = gold;
	}

	public int getMemberGuildCount() {
		return memberGuildCount;
	}

	public int getGuildRank() {
		return guildRank;
	}

	public Channel getPlayerCtx() {
		return this.ctx;
	}
	
	public int getCurrentTxSum() {
		return this.ctx.attr(GameAttributes.PACKET_TX_SUM).get();
	}

	public GameRoom getCurrentRoom() {
		return currentRoom;
	}

	public void setCurrentRoom(GameRoom currentRoom) {
		this.currentRoom = currentRoom;
	}

	// Crie os getters e setters para os novos campos
	public int getRoomTankPrimary() {
		return roomTankPrimary;
	}

	public void setRoomTankPrimary(int roomTankPrimary) {
		this.roomTankPrimary = roomTankPrimary;
	}

	public int getRoomTankSecondary() {
		return roomTankSecondary;
	}

	public void setRoomTankSecondary(int roomTankSecondary) {
		this.roomTankSecondary = roomTankSecondary;
	}

	public int getRoomTeam() {
		return roomTeam;
	}

	public void setRoomTeam(int roomTeam) {
		this.roomTeam = roomTeam;
	}
	
	public int getIsAlive() {
		return isAlive;
	}

	public void setIsAlive(int isAlive) {
		this.isAlive = isAlive;
	}


	public PlayerAvatar getAvatarWithHighestPlaceOrder() {
		return playerAvatars.stream().max((a, b) -> {
			Integer orderA = parsePlaceOrder(a.getPlaceOrder());
			Integer orderB = parsePlaceOrder(b.getPlaceOrder());
			return orderA.compareTo(orderB);
		}).orElse(null);
	}

	private static Integer parsePlaceOrder(String s) {
		if (s == null)
			return Integer.MIN_VALUE;
		try {
			return Integer.parseInt(s);
		} catch (NumberFormatException e) {
			return Integer.MIN_VALUE;
		}
	}

	@Override
	public int hashCode() {
		return Objects.hash(nickName, userId);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PlayerSession other = (PlayerSession) obj;
		return Objects.equals(nickName, other.nickName) && Objects.equals(userId, other.userId);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("PlayerSession [userId=");
		builder.append(userId);
		builder.append(", nickName=");
		builder.append(nickName);
		builder.append(", gender=");
		builder.append(gender);
		builder.append(", guild=");
		builder.append(guild);
		builder.append(", rankCurrent=");
		builder.append(rankCurrent);
		builder.append(", cash=");
		builder.append(cash);
		builder.append(", gold=");
		builder.append(gold);
		builder.append("]");
		return builder.toString();
	}


}