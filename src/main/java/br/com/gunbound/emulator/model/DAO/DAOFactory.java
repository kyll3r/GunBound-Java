package br.com.gunbound.emulator.model.DAO;

import java.sql.SQLException;

import br.com.gunbound.emulator.db.DatabaseManager;
import br.com.gunbound.emulator.db.DbException;
import br.com.gunbound.emulator.model.DAO.impl.ChestJDBC;
import br.com.gunbound.emulator.model.DAO.impl.MenuJDBC;
import br.com.gunbound.emulator.model.DAO.impl.UserJDBC;

public class DAOFactory {

	public static UserDAO CreateUserDao() {
		// return new UserJDBC(DB.getConnection());
		try {
			return new UserJDBC(DatabaseManager.getConnection());
		} catch (SQLException e) {
			throw new DbException(e.getMessage());
		}
	}

	public static ChestDAO CreateChestDao() {
		/// return new ChestJDBC(DB.getConnection());
		try {
			return new ChestJDBC(DatabaseManager.getConnection());
		} catch (SQLException e) {
			throw new DbException(e.getMessage());
		}
	}

	public static MenuDAO CreateMenuDao() {
		// return new MenuJDBC(DB.getConnection());
		try {
			return new MenuJDBC(DatabaseManager.getConnection());
		} catch (SQLException e) {
			throw new DbException(e.getMessage());
		}
	}

}
