package br.com.gunbound.emulator.lobby;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.PriorityBlockingQueue;

import br.com.gunbound.emulator.model.entities.game.PlayerSession;

public class GunBoundLobby { // <-- Nome da classe alterado
	private static final int MAX_SLOTS_PER_CHANNEL = 255; // Define o número máximo de posicoes no channel

	private final int id;
	// Jogadores no LOBBY deste canal, identificados pelo NickName
	private final Map<String, PlayerSession> playersInLobby;

	// Fila thread-safe que mantém os IDs disponíveis, sempre oferecendo o menor
	// primeiro.
	private final Queue<Integer> availableSlots;

	public GunBoundLobby(int id) {
		this.id = id;
		this.playersInLobby = new ConcurrentHashMap<>();

		this.availableSlots = new PriorityBlockingQueue<>(MAX_SLOTS_PER_CHANNEL);
		// A fila é pré-populada com um NÚMERO FINITO de slots (0 a 49)
		for (int i = 0; i < MAX_SLOTS_PER_CHANNEL; i++) {
			this.availableSlots.add(i);
		}
	}

	public int getId() {
		return id;
	}

	public Map<String, PlayerSession> getPlayersInLobby() {
		return playersInLobby;
	}

	public int getPlayerCount() {
		return playersInLobby.size();
	}

	public Integer takeSlot() {
		// poll() retorna um slot se houver um disponível.
		// Se a fila estiver vazia (porque os 50 slots foram ocupados),
		// ele retorna NULL, e o jogador não consegue entrar.
		return this.availableSlots.poll();
	}

	public void releaseSlot(int slot) {
		this.availableSlots.add(slot);
	}

	/**
	 * Verifica se o canal atingiu a sua capacidade máxima.
	 * 
	 * @return true se o canal estiver cheio, false caso contrário.
	 */
	public boolean isFull() {
		return getPlayerCount() >= MAX_SLOTS_PER_CHANNEL;
	}

	public void addPlayerToLobby(PlayerSession player) {
		if (player != null) {
			playersInLobby.put(player.getNickName(), player);
			System.out.println("GS: Player " + player.getNickName() + " entered lobby");
			//broadcastPlayerJoined(player); // Notificação automática (Tipo Padrao Observer)
		}
	}

	public void removePlayerFromLobby(PlayerSession player) {
		if (player != null) {
			//broadcastPlayerLeft(player); // Notificação automática (Tipo Padrao Observer)
			playersInLobby.remove(player.getNickName());
			System.out.println("GS: Player " + player.getNickName() + " left lobby");
		}
	}

	public int findHighestChannelPosition() {
		if (playersInLobby.isEmpty()) {
			// Retorna -1 para indicar que não há ninguém, então a primeira posição pode ser
			// 0
			return -1;
		}

		// Retorna o valor máximo ou -1 em caso de erro (improvável)
		return playersInLobby.values().stream().mapToInt(PlayerSession::getChannelPosition).max().orElse(-1);
	}

}