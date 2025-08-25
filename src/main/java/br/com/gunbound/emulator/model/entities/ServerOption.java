package br.com.gunbound.emulator.model.entities;

import java.net.InetAddress;

/**
 * Descreve um servidor de jogo a ser listado pelo broker.
 */
public class ServerOption {
	private final String serverName;
	private final String serverDescription;
	private final InetAddress serverAddress;
	private final int serverPort;
	private int serverUtilization; // Ocupação atual do servidor
	private final int serverCapacity; // Capacidade máxima do servidor
	private final boolean serverEnabled; // Se o servidor está ativo ou não

	public ServerOption(String serverName, String serverDescription, String serverAddress, int serverPort,
			int serverUtilization, int serverCapacity, boolean serverEnabled) throws Exception {
		this.serverName = serverName;
		this.serverDescription = serverDescription;
		this.serverAddress = InetAddress.getByName(serverAddress); // Converte o endereço IP de String para objeto
		this.serverPort = serverPort;
		this.serverUtilization = serverUtilization;
		this.serverCapacity = serverCapacity;
		this.serverEnabled = serverEnabled;
	}

	// Getters
	public String getServerName() {
		return serverName;
	}

	public String getServerDescription() {
		return serverDescription;
	}

	public InetAddress getServerAddress() {
		return serverAddress;
	}

	public int getServerPort() {
		return serverPort;
	}

	public int getServerUtilization() {
		return serverUtilization;
	}

	public int getServerCapacity() {
		return serverCapacity;
	}

	public boolean isServerEnabled() {
		return serverEnabled;
	}

	// Setter para a ocupação, pois ela pode mudar dinamicamente
	public void setServerUtilization(int serverUtilization) {
		this.serverUtilization = serverUtilization;
	}
}