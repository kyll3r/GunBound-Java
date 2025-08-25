package br.com.gunbound.emulator.model.entities.DTO;

import java.sql.Timestamp;

public class UserDTO {
	// --- user table ---
	private int id;
	private String userId;
	private int gender;
	private String password;
	private String status;
	private Timestamp muteTime;
	private Timestamp restrictTime;
	private int authority;
	private int authority2;
	private int authorityBackup;
	private String email;
	private int country;
	private int userLevel;
	private int dia;
	private int mes;
	private int ano;
	private Timestamp created;

	// --- game table ---
	private String nickname;
	private String guild;
	private int guildRank;
	private int memberGuildCount;
	
	//money
	private int gold;
	private int cash;
	
	private int eventScore0;
	private int eventScore1;
	private int eventScore2;
	private int eventScore3;
	private String prop1;
	private String prop2;
	private int adminGift;
	private int totalScore;
	private int seasonScore;
	private int totalGrade;
	private int seasonGrade;
	private int totalRank;
	private int seasonRank;
	private int accumShot;
	private int accumDamage;
	private Timestamp lastUpdateTime;
	private boolean noRankUpdate;
	private byte[] clientData;
	private int gameCountry;
	private Timestamp giftProhibitTime;


	public UserDTO() {
		
	}
	
	/*public User(int id, String userId, int gender, String password, String status, Timestamp muteTime,
                  Timestamp restrictTime, int authority, int authority2, int authorityBackup, String email,
                  int country, int userLevel, int dia, int mes, int ano, Timestamp created,
                  String nickname, String guild, int guildRank, int memberCount, int money,
                  int eventScore0, int eventScore1, int eventScore2, int eventScore3,
                  String prop1, String prop2, int adminGift, int totalScore, int seasonScore,
                  int totalGrade, int seasonGrade, int totalRank, int seasonRank, int accumShot,
                  int accumDamage, Timestamp lastUpdateTime, boolean noRankUpdate, byte[] clientData,
                  int gameCountry, Timestamp giftProhibitTime) {
        this.id = id;
        this.userId = userId;
        this.gender = gender;
        this.password = password;
        this.status = status;
        this.muteTime = muteTime;
        this.restrictTime = restrictTime;
        this.authority = authority;
        this.authority2 = authority2;
        this.authorityBackup = authorityBackup;
        this.email = email;
        this.country = country;
        this.userLevel = userLevel;
        this.dia = dia;
        this.mes = mes;
        this.ano = ano;
        this.created = created;
        this.nickname = nickname;
        this.guild = guild;
        this.guildRank = guildRank;
        this.memberCount = memberCount;
        this.money = money;
        this.eventScore0 = eventScore0;
        this.eventScore1 = eventScore1;
        this.eventScore2 = eventScore2;
        this.eventScore3 = eventScore3;
        this.prop1 = prop1;
        this.prop2 = prop2;
        this.adminGift = adminGift;
        this.totalScore = totalScore;
        this.seasonScore = seasonScore;
        this.totalGrade = totalGrade;
        this.seasonGrade = seasonGrade;
        this.totalRank = totalRank;
        this.seasonRank = seasonRank;
        this.accumShot = accumShot;
        this.accumDamage = accumDamage;
        this.lastUpdateTime = lastUpdateTime;
        this.noRankUpdate = noRankUpdate;
        this.clientData = clientData;
        this.gameCountry = gameCountry;
        this.giftProhibitTime = giftProhibitTime;
    }*/

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getUserId() {
		return userId.toLowerCase();
	}

	public void setUserId(String userId) {
		this.userId = userId;
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

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Timestamp getMuteTime() {
		return muteTime;
	}

	public void setMuteTime(Timestamp muteTime) {
		this.muteTime = muteTime;
	}

	public Timestamp getRestrictTime() {
		return restrictTime;
	}

	public void setRestrictTime(Timestamp restrictTime) {
		this.restrictTime = restrictTime;
	}

	public int getAuthority() {
		return authority;
	}

	public void setAuthority(int authority) {
		this.authority = authority;
	}

	public int getAuthority2() {
		return authority2;
	}

	public void setAuthority2(int authority2) {
		this.authority2 = authority2;
	}

	public int getAuthorityBackup() {
		return authorityBackup;
	}

	public void setAuthorityBackup(int authorityBackup) {
		this.authorityBackup = authorityBackup;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public int getCountry() {
		return country;
	}

	public void setCountry(int country) {
		this.country = country;
	}

	public int getUserLevel() {
		return userLevel;
	}

	public void setUserLevel(int userLevel) {
		this.userLevel = userLevel;
	}

	public int getDia() {
		return dia;
	}

	public void setDia(int dia) {
		this.dia = dia;
	}

	public int getMes() {
		return mes;
	}

	public void setMes(int mes) {
		this.mes = mes;
	}

	public int getAno() {
		return ano;
	}

	public void setAno(int ano) {
		this.ano = ano;
	}

	public Timestamp getCreated() {
		return created;
	}

	public void setCreated(Timestamp created) {
		this.created = created;
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public String getGuild() {
		return guild;
	}

	public void setGuild(String guild) {
		this.guild = guild;
	}

	public int getGuildRank() {
		return guildRank;
	}

	public void setGuildRank(int guildRank) {
		this.guildRank = guildRank;
	}

	public int getMemberGuildCount() {
		return memberGuildCount;
	}

	public void setMemberGuildCount(int memberGuildCount) {
		this.memberGuildCount = memberGuildCount;
	}

	public int getGold() {
		return gold;
	}

	public void setGold(int gold) {
		this.gold = gold;
	}

	public int getCash() {
		return cash;
	}
	
	public void setCash(int cash) {
		this.cash = cash;
	}

	public int getEventScore0() {
		return eventScore0;
	}

	public void setEventScore0(int eventScore0) {
		this.eventScore0 = eventScore0;
	}

	public int getEventScore1() {
		return eventScore1;
	}

	public void setEventScore1(int eventScore1) {
		this.eventScore1 = eventScore1;
	}

	public int getEventScore2() {
		return eventScore2;
	}

	public void setEventScore2(int eventScore2) {
		this.eventScore2 = eventScore2;
	}

	public int getEventScore3() {
		return eventScore3;
	}

	public void setEventScore3(int eventScore3) {
		this.eventScore3 = eventScore3;
	}

	public String getProp1() {
		return prop1;
	}

	public void setProp1(String prop1) {
		this.prop1 = prop1;
	}

	public String getProp2() {
		return prop2;
	}

	public void setProp2(String prop2) {
		this.prop2 = prop2;
	}

	public int getAdminGift() {
		return adminGift;
	}

	public void setAdminGift(int adminGift) {
		this.adminGift = adminGift;
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

	public int getTotalGrade() {
		return totalGrade;
	}

	public void setTotalGrade(int totalGrade) {
		this.totalGrade = totalGrade;
	}

	public int getSeasonGrade() {
		return seasonGrade;
	}

	public void setSeasonGrade(int seasonGrade) {
		this.seasonGrade = seasonGrade;
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

	public int getAccumShot() {
		return accumShot;
	}

	public void setAccumShot(int accumShot) {
		this.accumShot = accumShot;
	}

	public int getAccumDamage() {
		return accumDamage;
	}

	public void setAccumDamage(int accumDamage) {
		this.accumDamage = accumDamage;
	}

	public Timestamp getLastUpdateTime() {
		return lastUpdateTime;
	}

	public void setLastUpdateTime(Timestamp lastUpdateTime) {
		this.lastUpdateTime = lastUpdateTime;
	}

	public boolean isNoRankUpdate() {
		return noRankUpdate;
	}

	public void setNoRankUpdate(boolean noRankUpdate) {
		this.noRankUpdate = noRankUpdate;
	}

	public byte[] getClientData() {
		return clientData;
	}

	public void setClientData(byte[] clientData) {
		this.clientData = clientData;
	}

	public int getGameCountry() {
		return gameCountry;
	}

	public void setGameCountry(int gameCountry) {
		this.gameCountry = gameCountry;
	}

	public Timestamp getGiftProhibitTime() {
		return giftProhibitTime;
	}

	public void setGiftProhibitTime(Timestamp giftProhibitTime) {
		this.giftProhibitTime = giftProhibitTime;
	}

}
