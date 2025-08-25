package br.com.gunbound.emulator.model.DAO.impl;

import java.sql.ResultSet;
import java.sql.SQLException;

public class JdbcUtils {
	/**
	 * Lê um campo numérico do ResultSet e retorna como Integer (null-safe). Aceita
	 * Integer, Long, Short, Byte, etc.
	 */
	public static Integer getNullableInt(ResultSet rs, String column) throws SQLException {
		Object obj = rs.getObject(column);
		return (obj == null) ? null : ((Number) obj).intValue();
	}

	/**
	 * Igual ao acima, mas para Long.
	 */
	public static Long getNullableLong(ResultSet rs, String column) throws SQLException {
		Object obj = rs.getObject(column);
		return (obj == null) ? null : ((Number) obj).longValue();
	}
}