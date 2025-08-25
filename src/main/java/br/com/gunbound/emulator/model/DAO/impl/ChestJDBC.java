package br.com.gunbound.emulator.model.DAO.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import br.com.gunbound.emulator.db.DbException;
import br.com.gunbound.emulator.model.DAO.ChestDAO;
import br.com.gunbound.emulator.model.entities.DTO.ChestDTO;

public class ChestJDBC implements ChestDAO {
	private Connection conn;

	public ChestJDBC(Connection conn) {
		this.conn = conn;
	}

	// Buscar todos os itens de um jogador específico
	@Override
	public List<ChestDTO> getAllAvatarsByOwnerId(String ownerId) {
		List<ChestDTO> chestList = new ArrayList<>();
		String sql = "SELECT * FROM chest WHERE OwnerId = ?";
		try (PreparedStatement pst = conn.prepareStatement(sql)) {
			pst.setString(1, ownerId);
			try (ResultSet rs = pst.executeQuery()) {
				while (rs.next()) {
					chestList.add(populateChest(rs));
				}
			}
		} catch (SQLException e) {
			throw new DbException(e.getMessage());
		}
		return chestList;
	}

	// Buscar um item pelo Idx (PK)
	@Override
	public ChestDTO getByIdx(int idx) {
		String sql = "SELECT * FROM chest WHERE Idx = ?";
		try (PreparedStatement pst = conn.prepareStatement(sql)) {
			pst.setInt(1, idx);
			try (ResultSet rs = pst.executeQuery()) {
				if (rs.next()) {
					return populateChest(rs);
				}
			}
		} catch (SQLException e) {
			throw new DbException(e.getMessage());
		}
		return null;
	}

	// Inserir novo item no chest (retorna o Idx gerado)
	@Override
	public int insert(ChestDTO chest) {
		String sql = "INSERT INTO chest (Item, Wearing, Acquisition, Expire, Volume, PlaceOrder, Recovered, OwnerId, ExpireType) "
				+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
		try (PreparedStatement pst = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

			pst.setObject(1, chest.getItem(), Types.INTEGER);
			pst.setString(2, chest.getWearing());
			pst.setString(3, chest.getAcquisition());
			pst.setTimestamp(4, chest.getExpire());
			pst.setObject(5, chest.getVolume(), Types.INTEGER);
			pst.setString(6, chest.getPlaceOrder());
			pst.setString(7, chest.getRecovered());
			pst.setString(8, chest.getOwnerId());
			pst.setString(9, chest.getExpireType());

			pst.executeUpdate();
			try (ResultSet keys = pst.getGeneratedKeys()) {
				if (keys.next()) {
					return keys.getInt(1); // retorna o novo Idx
				}
			}
		} catch (SQLException e) {
			throw new DbException(e.getMessage());
		}
		return -1; // erro
	}

	// Deleta item por Idx
	@Override
	public boolean deleteByIdx(int idx) {
		String sql = "DELETE FROM chest WHERE Idx = ?";
		try (PreparedStatement pst = conn.prepareStatement(sql)) {
			pst.setInt(1, idx);
			return pst.executeUpdate() > 0;
		} catch (SQLException e) {
			throw new DbException(e.getMessage());
		}
	}

	/**
	 * Atualiza o campo PlaceOrder de um item do chest.
	 * 
	 * @param idx            Idx do item (PK)
	 * @param novoPlaceOrder Novo valor para PlaceOrder
	 * @return true se atualizou, false se falhou
	 */
	@Override
	public boolean updatePlaceOrder(int idx, String novoPlaceOrder) {
		String sql = "UPDATE chest SET PlaceOrder = ? WHERE Idx = ?";
		try (PreparedStatement pst = conn.prepareStatement(sql)) {
			pst.setString(1, novoPlaceOrder);
			pst.setInt(2, idx);
			return pst.executeUpdate() > 0;
		} catch (SQLException e) {
			throw new DbException(e.getMessage());
		}
	}

	@Override
	public boolean updateAvatarWearing(Integer idx, String isWearing, String ownerId) {
		try (PreparedStatement pst = (ownerId != null && idx == null && isWearing == null)
				? conn.prepareStatement("UPDATE chest SET Wearing = 0 WHERE OwnerId = ?")
				: conn.prepareStatement("UPDATE chest SET Wearing = ? WHERE Idx = ?")
						) {

			if (pst == null)
				return false;

			if (ownerId != null  && idx == null && isWearing == null) {
				pst.setString(1, ownerId);
			} else {
				pst.setString(1, isWearing);
				pst.setInt(2, idx);
			}

			return pst.executeUpdate() > 0;
		} catch (SQLException e) {
			throw new DbException(e.getMessage());
		}
	}

	// 'Mapper' auxiliar
	private ChestDTO populateChest(ResultSet rs) throws SQLException {
		ChestDTO c = new ChestDTO();
		c.setIdx(rs.getInt("Idx"));

		c.setItem((int) rs.getObject("Item"));
		c.setWearing(rs.getString("Wearing"));
		c.setAcquisition(rs.getString("Acquisition"));
		c.setExpire(rs.getTimestamp("Expire"));
		c.setVolume(rs.getInt("Volume"));
		c.setPlaceOrder(rs.getString("PlaceOrder"));
		c.setRecovered(rs.getString("Recovered"));
		c.setOwnerId(rs.getString("OwnerId"));
		c.setExpireType(rs.getString("ExpireType"));
		return c;
	}
}
