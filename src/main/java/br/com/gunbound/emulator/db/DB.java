package br.com.gunbound.emulator.db;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;


public class DB {

	private static Connection conn = null;

	public static Connection getConnection() {
		if (conn == null) {
			try {
				Properties props = loadProperties();
				String url = props.getProperty("dburl");
				conn = DriverManager.getConnection(url, props);
				Class.forName("org.mariadb.jdbc.Driver");
			} catch (SQLException | ClassNotFoundException e) {
				throw new DbException(e.getMessage());
			} 
		}

		return conn;
	}

	private static Properties loadProperties() {
		//try (InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream("db.properties")) {
			try (InputStream in = DB.class.getClassLoader().getResourceAsStream("db.properties")) {
			Properties props = new Properties();
			props.load(in);
			return props;
		} catch (IOException e) {
			throw new DbException(e.getMessage());
		}
	}

	public static void closeConnection() {
		if (conn != null) {
			try {
				conn.close();
			} catch (SQLException e) {
				throw new DbException(e.getMessage());
			}
		}
	}

	public static void closeStatement(Statement st) {
		if (st != null) {
			try {
				st.close();
			} catch (SQLException e) {
				throw new DbException(e.getMessage());
			}
		}
	}

	public static void closeResultSet(ResultSet rs) {
		if (rs != null) {
			try {
				rs.close();
			} catch (SQLException e) {
				throw new DbException(e.getMessage());
			}
		}
	}
	
}
