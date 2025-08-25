package br.com.gunbound.emulator;

import java.util.UUID;

public class Connection {
	private final UUID id;
	private final String ip;
	private final int port;

	public Connection(String ip, int port) {
		this.id = UUID.randomUUID(); // Gera um ID único (UUID) no construtor
		this.ip = ip;
		this.port = port;
	}

	public UUID getId() {
		return id;
	}

	public String getIp() {
		return ip;
	}

	public int getPort() {
		return port;
	}

	@Override
	public String toString() {
		return "Connection{" + "id=" + id + ", ip='" + ip + '\'' + ", port=" + port + '}';
	}
}