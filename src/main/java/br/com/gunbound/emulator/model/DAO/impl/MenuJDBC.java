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
import br.com.gunbound.emulator.model.DAO.MenuDAO;
import br.com.gunbound.emulator.model.entities.DTO.MenuDTO;

public class MenuJDBC implements MenuDAO {
	private Connection conn;

	public MenuJDBC(Connection conn) {
		this.conn = conn;
	}

	// Buscar todos os itens de um jogador específico
	@Override
	 public List<MenuDTO> getAll() {
        List<MenuDTO> list = new ArrayList<>();
        String sql = "SELECT " +
                "Idx, No, ItemCount, Item1, Period1, Volume1, " +
                "Menu_Name, Menu_Desc, Menu_Image, ExType, " +
                "PriceByCashForW, PriceByCashForM, PriceByCashForY, PriceByCashForI, " +
                "PriceByGoldForW, PriceByGoldForM, PriceByGoldForY, PriceByGoldForI " +
                "FROM menu";
        try (PreparedStatement pst = conn.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {
            while (rs.next()) {
                list.add(populateMenuDTO(rs));
            }
        } catch (SQLException e) {
        	throw new DbException(e.getMessage());
        }
        return list;
    }

    public MenuDTO getByIdx(int idx) {
        String sql = "SELECT * FROM menu WHERE Idx = ?";
        try (PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setInt(1, idx);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return populateMenuDTO(rs);
                }
            }
        } catch (SQLException e) {
        	throw new DbException(e.getMessage());
        }
        return null;
    }

    @Override
    public int insert(MenuDTO menu) {
        String sql = "INSERT INTO menu (No, ItemCount, Item1, Period1, Volume1, Menu_Name, Menu_Desc, Menu_Image, " +
                "ExType, PriceByCashForW, PriceByCashForM, PriceByCashForY, PriceByCashForI, " +
                "PriceByGoldForW, PriceByGoldForM, PriceByGoldForY, PriceByGoldForI) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pst = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pst.setInt(1, menu.getNo());
            pst.setObject(2, menu.getItemCount(), Types.INTEGER);
            pst.setObject(3, menu.getItem1(), Types.INTEGER);
            pst.setObject(4, menu.getPeriod1(), Types.INTEGER);
            pst.setObject(5, menu.getVolume1(), Types.INTEGER);
            pst.setString(6, menu.getMenuName());
            pst.setString(7, menu.getMenuDesc());
            pst.setString(8, menu.getMenuImage());
            pst.setObject(9, menu.getExType(), Types.INTEGER);
            pst.setObject(10, menu.getPriceByCashForW(), Types.INTEGER);
            pst.setObject(11, menu.getPriceByCashForM(), Types.INTEGER);
            pst.setObject(12, menu.getPriceByCashForY(), Types.INTEGER);
            pst.setObject(13, menu.getPriceByCashForI(), Types.INTEGER);
            pst.setObject(14, menu.getPriceByGoldForW(), Types.INTEGER);
            pst.setObject(15, menu.getPriceByGoldForM(), Types.INTEGER);
            pst.setObject(16, menu.getPriceByGoldForY(), Types.INTEGER);
            pst.setObject(17, menu.getPriceByGoldForI(), Types.INTEGER);
            pst.executeUpdate();
            try (ResultSet keys = pst.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        } catch (SQLException e) {
        	throw new DbException(e.getMessage());
        }
        return -1;
    }


    private MenuDTO populateMenuDTO(ResultSet rs) throws SQLException {
        MenuDTO m = new MenuDTO();
        m.setIdx(rs.getInt("Idx"));
        m.setNo(rs.getInt("No"));
        m.setItemCount(JdbcUtils.getNullableInt(rs, "ItemCount"));
        m.setItem1(JdbcUtils.getNullableInt(rs, "Item1"));
        m.setPeriod1(JdbcUtils.getNullableInt(rs, "Period1"));
        m.setVolume1(JdbcUtils.getNullableInt(rs, "Volume1"));
        m.setMenuName(rs.getString("Menu_Name"));
        m.setMenuDesc(rs.getString("Menu_Desc"));
        m.setMenuImage(rs.getString("Menu_Image"));
        m.setExType(JdbcUtils.getNullableInt(rs,"ExType"));
        m.setPriceByCashForW(JdbcUtils.getNullableInt(rs,"PriceByCashForW"));
        m.setPriceByCashForM(JdbcUtils.getNullableInt(rs,"PriceByCashForM"));
        m.setPriceByCashForY(JdbcUtils.getNullableInt(rs,"PriceByCashForY"));
        m.setPriceByCashForI(JdbcUtils.getNullableInt(rs,"PriceByCashForI"));
        m.setPriceByGoldForW(JdbcUtils.getNullableInt(rs,"PriceByGoldForW"));
        m.setPriceByGoldForM(JdbcUtils.getNullableInt(rs,"PriceByGoldForM"));
        m.setPriceByGoldForY(JdbcUtils.getNullableInt(rs,"PriceByGoldForY"));
        m.setPriceByGoldForI(JdbcUtils.getNullableInt(rs,"PriceByGoldForI"));
        return m;
    }
}
