package br.com.gunbound.emulator.db;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class DatabaseManager {

	private static HikariDataSource dataSource;

	static {
		try {
			// 1. Carregar as propriedades do arquivo db.properties
			Properties props = loadProperties();

			// 2. Configurar o HikariCP
			HikariConfig config = new HikariConfig();
			config.setJdbcUrl(props.getProperty("dburl"));
			config.setUsername(props.getProperty("user"));
			config.setPassword(props.getProperty("password"));
			config.setDriverClassName("org.mariadb.jdbc.Driver");

			// 3. Configurações recomendadas para o pool
			config.setMaximumPoolSize(20); // Número máximo de conexões
			config.setMinimumIdle(5); // Número mínimo de conexões ociosas
			config.setConnectionTimeout(30000); // 30 segundos para obter uma conexão
			config.setIdleTimeout(600000); // 10 minutos para uma conexão ociosa ser removida
			config.setMaxLifetime(3600000); // 60 minutos de vida máxima para uma conexão

			// 4. Inicializar o DataSource do HikariCP
			dataSource = new HikariDataSource(config);

		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Falha ao inicializar o pool de conexões.", e);
		}
	}

	/**
	 * Pega uma conexão do pool.
	 * 
	 * @return Uma conexão de banco de dados ativa.
	 * @throws SQLException
	 */
	public static Connection getConnection() throws SQLException {
		return dataSource.getConnection();
	}

	/**
	 * Carrega as propriedades do arquivo db.properties.
	 */
	private static Properties loadProperties() {
		try (InputStream in = DatabaseManager.class.getClassLoader().getResourceAsStream("db.properties")) {
			if (in == null) {
				throw new IOException("Arquivo db.properties não encontrado no classpath.");
			}
			Properties props = new Properties();
			props.load(in);
			return props;
		} catch (IOException e) {
			throw new DbException(e.getMessage());
		}
	}

	// Construtor privado para evitar instanciação
	private DatabaseManager() {
	}
}