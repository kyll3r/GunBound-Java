package br.com.gunbound.emulator;

import io.netty.channel.Channel;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.net.InetSocketAddress;

public final class ConnectionManager {

	// A única instância da classe (Singleton) Escutei muito no curso de java sobre o Singleton
	private static final ConnectionManager INSTANCE = new ConnectionManager();

	// Um mapa thread-safe para armazenar as conexões
	// A chave é o canal (socket), e o valor é a nossa classe de conexão
	private final Map<Channel, Connection> connections;

	private ConnectionManager() {
		this.connections = new ConcurrentHashMap<>();
	}

	/**
	 * Retorna a única instância do ConnectionManager.
	 * 
	 * @return A instância do ConnectionManager.
	 */
	public static ConnectionManager getInstance() {
		return INSTANCE;
	}

	/**
	 * Registra uma nova conexão.
	 * 
	 * @param channel O canal do cliente conectado.
	 * @return O objeto Connection recém-criado.
	 */
	public Connection registerConnection(Channel channel) {
		if (channel == null || !channel.isActive()) {
			return null;
		}

		InetSocketAddress remoteAddress = (InetSocketAddress) channel.remoteAddress();
		String ip = remoteAddress.getAddress().getHostAddress();
		int port = remoteAddress.getPort();

		Connection connection = new Connection(ip, port);
		connections.put(channel, connection);

		System.out.println("Nova conexão registrada: " + connection);

		return connection;
	}

	/**
	 * Remove uma conexão registrada.
	 * 
	 * @param channel O canal do cliente a ser removido.
	 * @return O objeto Connection que foi removido, ou null se não for encontrado.
	 */
	public Connection removeConnection(Channel channel) {
		if (channel == null) {
			return null;
		}
		Connection connection = connections.remove(channel);
		if (connection != null) {
			System.out.println("Conexão removida: " + connection);
		}
		return connection;
	}

	/**
	 * Obtém uma conexão pelo seu canal.
	 * 
	 * @param channel O canal do cliente.
	 * @return O objeto Connection correspondente, ou null se não for encontrado.
	 */
	public Connection getConnection(Channel channel) {
		return connections.get(channel);
	}

	/**
	 * Retorna o número de conexões ativas.
	 * 
	 * @return O tamanho do mapa de conexões.
	 */
	public int getActiveConnectionCount() {
		return connections.size();
	}
}