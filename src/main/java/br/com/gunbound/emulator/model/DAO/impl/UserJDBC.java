package br.com.gunbound.emulator.model.DAO.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import br.com.gunbound.emulator.db.DbException;
import br.com.gunbound.emulator.model.DAO.UserDAO;
import br.com.gunbound.emulator.model.entities.DTO.UserDTO;
import br.com.gunbound.emulator.model.entities.game.PlayerSession;

public class UserJDBC implements UserDAO {
	private Connection conn;

	public UserJDBC(Connection conn) {
		this.conn = conn;
	}

	@Override
	public UserDTO getUserByUserId(String userIdQuery) {
		String sql = "SELECT " + "u.Id, u.UserId, u.Gender, u.Password, u.Status, u.MuteTime, u.RestrictTime, "
				+ "u.Authority, u.Authority2, u.AuthorityBackup, u.E_Mail, u.Country, u.User_Level, u.Dia, u.Mes, u.Ano, u.Created,"
				+ "g.NickName, g.Guild, g.GuildRank, g.MemberGuildCount, g.Gold, g.Cash, "
				+ "g.EventScore0, g.EventScore1, g.EventScore2, g.EventScore3, "
				+ "g.Prop1, g.Prop2, g.AdminGift, g.TotalScore, g.SeasonScore, g.TotalGrade, g.SeasonGrade, "
				+ "g.TotalRank, g.SeasonRank, g.AccumShot, g.AccumDamage, g.LastUpdateTime, g.NoRankUpdate, "
				+ "g.ClientData, g.Country as gameCountry, g.GiftProhibitTime "
				+ "FROM user u JOIN game g ON u.UserId = g.UserId WHERE u.UserId = ?";

		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setString(1, userIdQuery);
			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					UserDTO user = new UserDTO();

					user.setId(rs.getInt("Id"));
					user.setUserId(rs.getString("UserId"));
					user.setGender(rs.getInt("Gender"));
					user.setPassword(rs.getString("Password"));
					user.setStatus(rs.getString("Status"));
					user.setMuteTime(rs.getTimestamp("MuteTime"));
					user.setRestrictTime(rs.getTimestamp("RestrictTime"));
					user.setAuthority(rs.getInt("Authority"));
					user.setAuthority2(rs.getInt("Authority2"));
					user.setAuthorityBackup(rs.getInt("AuthorityBackup"));
					user.setEmail(rs.getString("E_Mail"));
					user.setCountry(rs.getInt("Country"));
					user.setUserLevel(rs.getInt("User_Level"));
					user.setDia(rs.getInt("Dia"));
					user.setMes(rs.getInt("Mes"));
					user.setAno(rs.getInt("Ano"));
					user.setCreated(rs.getTimestamp("Created"));

					user.setNickname(rs.getString("NickName"));
					user.setGuild(rs.getString("Guild"));
					user.setGuildRank(rs.getInt("GuildRank"));
					user.setMemberGuildCount(rs.getInt("MemberGuildCount"));
					user.setGold(rs.getInt("Gold"));
					user.setCash(rs.getInt("Cash"));

					user.setEventScore0(rs.getInt("EventScore0"));
					user.setEventScore1(rs.getInt("EventScore1"));
					user.setEventScore2(rs.getInt("EventScore2"));
					user.setEventScore3(rs.getInt("EventScore3"));

					user.setProp1(rs.getString("Prop1"));
					user.setProp2(rs.getString("Prop2"));
					user.setAdminGift(rs.getInt("AdminGift"));
					user.setTotalScore(rs.getInt("TotalScore"));
					user.setSeasonScore(rs.getInt("SeasonScore"));
					user.setTotalGrade(rs.getInt("TotalGrade"));
					user.setSeasonGrade(rs.getInt("SeasonGrade"));
					user.setTotalRank(rs.getInt("TotalRank"));
					user.setSeasonRank(rs.getInt("SeasonRank"));
					user.setAccumShot(rs.getInt("AccumShot"));
					user.setAccumDamage(rs.getInt("AccumDamage"));
					user.setLastUpdateTime(rs.getTimestamp("LastUpdateTime"));

					user.setNoRankUpdate(rs.getBoolean("NoRankUpdate"));
					user.setClientData(rs.getBytes("ClientData"));
					user.setGameCountry(rs.getInt("gameCountry"));
					user.setGiftProhibitTime(rs.getTimestamp("GiftProhibitTime"));

					return user;
				}
			}
		} catch (SQLException e) {
			throw new DbException(e.getMessage());
		}
		return null;
	}
	
	
	@Override
	public void updateAddGold(String playerId, int value) {
		updateGold(playerId, value, "+");
	}
	
	@Override
	public void updateMinusGold(String playerId, int value) {
		updateGold(playerId, value, "-");
	}


	public void updateGold(String playerId, int value, String op) {
		if(op == null)
			return;
		
		String sql = "UPDATE Game SET Gold = Gold "+ op +" ? WHERE UserId = ?";
		try (PreparedStatement stmt = conn.prepareStatement(sql)) {

			stmt.setInt(1, value);
			stmt.setString(2, playerId);

			stmt.executeUpdate();
		} catch (SQLException e) {
			throw new DbException(e.getMessage());
		} 

	}
	
	@Override
	public void updateAddCash(String playerId, int value) {
		updateCash(playerId, value, "+");
	}
	
	@Override
	public void updateMinusCash(String playerId, int value) {
		updateCash(playerId, value, "-");
	}


	public void updateCash(String playerId, int value, String op) {
		if(op == null)
			return;
		
		String sql = "UPDATE Game SET Cash = Cash "+ op +" ? WHERE UserId = ?";
		try (PreparedStatement stmt = conn.prepareStatement(sql)) {

			stmt.setInt(1, value);
			stmt.setString(2, playerId);

			stmt.executeUpdate();
		} catch (SQLException e) {
			throw new DbException(e.getMessage());
		} 

	}


	private PlayerSession instantiateSkuList(ResultSet rs) throws SQLException {
		PlayerSession get = new PlayerSession();
		/*
		 * get.setId(rs.getInt("id")); get.setIdProduto(rs.getString("id_produto"));
		 * get.setSkuProduto(rs.getString("sku_produto"));
		 * get.setNome(rs.getString("nome"));
		 * get.setNomeResumido(rs.getString("nome_resumido"));
		 * get.setSigla(rs.getString("sigla")); get.setAtivo(rs.getInt("ativo"));
		 */
		return get;
	}

}
