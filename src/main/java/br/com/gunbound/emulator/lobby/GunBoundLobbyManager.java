package br.com.gunbound.emulator.lobby;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

import br.com.gunbound.emulator.model.entities.game.PlayerSession;
import br.com.gunbound.emulator.packets.writers.PacketWriter;
import br.com.gunbound.emulator.utils.PacketUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class GunBoundLobbyManager {
	// 1. A única instância da classe (Singleton)
	private static volatile GunBoundLobbyManager instance;

	private static final int NUM_CHANNELS = 8;
	private final List<GunBoundLobby> channels;

	// 2. Construtor privado para evitar instâncias diretas de fora
	private GunBoundLobbyManager() {
		channels = new CopyOnWriteArrayList<>();
		for (int i = 0; i < NUM_CHANNELS; i++) {
			// Inicializa os canais de lobby. Use 'i + 1' para IDs de 1 a NUM_CHANNELS.
			channels.add(new GunBoundLobby(i));
		}
		System.out.println("GS: " + NUM_CHANNELS + " canais de lobby inicializados.");
	}

	// 3. Método estático público para obter a única instância da classe
	// (thread-safe)
	public static GunBoundLobbyManager getInstance() {
		if (instance == null) { // Primeira checagem (sem lock)
			synchronized (GunBoundLobbyManager.class) { // Sincroniza apenas se a instância for nula
				if (instance == null) { // Segunda checagem (dentro do lock)
					instance = new GunBoundLobbyManager();
				}
			}
		}
		return instance;
	}

	// --- Métodos de Gerenciamento do Lobby ---

	public List<GunBoundLobby> getAllLobby() {
		return channels;
	}

	public Optional<GunBoundLobby> getLobbyById(int id) {
		// Valida o ID do canal para evitar IndexOutOfBoundsException e retornar
		// Optional.empty()
		if (id < 0 || id > NUM_CHANNELS) {
			System.err.println("GS: Tentativa de acessar canal inválido: " + (id + 1) + " [" + id + "]");
			return Optional.empty();
		}
		// Os canais são armazenados em uma lista de 0 a N-1, então 'id - 1'
		return Optional.of(channels.get(id));
	}

	/**
	 * Encontra o melhor canal para um novo jogador, priorizando os 3 primeiros.
	 * 
	 * @return O ID do canal recomendado (1-8), ou -1 se todos os canais estiverem
	 *         cheios.
	 */
	public int findBestChannelForNewPlayer() {
		// --- Nível 1: Canais de Alta Prioridade (1, 2, 3) ---
		for (int i = 0; i <= 3; i++) {
			final int channelId = i;
			boolean channelAvailable = getLobbyById(channelId).map(channel -> !channel.isFull()).orElse(false);

			if (channelAvailable) {
				System.out.println(
						"GS: [Distribuição] Canal prioritário " + channelId + " encontrado para novo jogador.");
				return channelId;
			}
		}

		// --- Nível 2: Canais de Overflow (4 a 8) ---
		System.out.println("GS: [Distribuição] Canais prioritários cheios. Procurando em canais de overflow...");

		return channels.stream().filter(channel -> channel.getId() > 3 && !channel.isFull())
				.min(Comparator.comparingInt(GunBoundLobby::getPlayerCount)).map(GunBoundLobby::getId)
				.orElse(-1);
	}

	public void playerJoinLobby(PlayerSession player, int channelId) {
		if (player == null) {
			System.err.println("GS: Tentativa de mover um PlayerSession nulo para Lobby " + (channelId + 1));
			return;
		}

		getLobbyById(channelId).ifPresentOrElse(lobby -> {
			// Remove o jogador do lobby atual, se ele já estiver em um
			if (player.getCurrentLobby() != null) {
				player.getCurrentLobby().removePlayerFromLobby(player);
				System.out.println("GS: Jogador " + player.getNickName() + " saiu do lobby anterior.");
			}

			// Adiciona o jogador ao novo lobby
			lobby.addPlayerToLobby(player);
			
			player.setCurrentLobby(lobby); // Atualiza a referência de lobby no PlayerSession
			System.out.println("GS: Jogador " + player.getNickName() + " entrou no lobby " + (channelId + 1));

			// TODO: Notificar outros jogadores no lobby sobre a entrada deste jogador
		}, () -> {
			System.err.println("GS: Tentativa de jogador " + player.getNickName() + " entrar em lobby inválido: "
					+ (channelId + 1));
		});
		broadcastPlayerJoined(player); // Notificação automática
	}

	public void playerLeaveLobby(PlayerSession player) {
		if (player == null || player.getCurrentLobby() == null) {
			// O jogador não está em nenhum lobby ou o objeto PlayerSession é nulo.
			System.out.println("GS: Tentativa de remover jogador nulo ou sem lobby.");
			return;
		}

		player.getCurrentLobby().releaseSlot(player.getChannelPosition()); // Devolve o slot para a fila
		broadcastPlayerLeft(player); // inserido agora pra tirar da classe
		player.getCurrentLobby().removePlayerFromLobby(player);
		player.setCurrentLobby(null); // Limpa a referência de lobby no PlayerSession
		player.setChannelPosition(-1); // adicionado depois (VERIFICAR) ********************************************
		System.out.println("GS: Jogador " + player.getNickName() + " saiu do lobby.");

		// TODO: Notificar outros jogadores no lobby sobre a saída deste jogador
	}

	public Collection<PlayerSession> getPlayersInLobby(int channelId) {
		return getLobbyById(channelId).map(GunBoundLobby::getPlayersInLobby) // Mapeia o Optional para o Map de
																					// jogadores
				.map(Map::values) // Mapeia o Map para a Coleção de jogadores
				.orElse(Collections.emptyList()); // Retorna uma lista vazia se o canal não existir
	}

	/**
	 * Notifica todos os jogadores no lobby (exceto o que acabou de entrar) que um
	 * novo jogador chegou.
	 * 
	 * @param newPlayer O jogador que acabou de entrar.
	 */
	private void broadcastPlayerJoined(PlayerSession newPlayer) {
		
		GunBoundLobby currenttLobby = newPlayer.getCurrentLobby();
		// logs para depuração
		System.out.println("[DEBUG] Iniciando broadcastPlayerJoined para: " + newPlayer.getNickName() + " no Canal ID: "
				+ currenttLobby.getId());
		System.out.println("[DEBUG] Tamanho atual do lobby: " + currenttLobby.getPlayersInLobby().size());
		System.out.println("[DEBUG] Jogadores no lobby: " + currenttLobby.getPlayersInLobby().keySet().toString());

		// Usa o opcode 0x200E para notificar sobre a entrada
		final int OPCODE_PLAYER_JOINED = 0x200E;
		ByteBuf notificationPayload = Unpooled.buffer();

		// snapshot para evitar concorrência
		Collection<PlayerSession> recipients = new ArrayList<>(currenttLobby.getPlayersInLobby().values());
		for (PlayerSession existingPlayer : recipients) {
			if (!existingPlayer.equals(newPlayer)) {

				//int targetTxSum = existingPlayer.getPlayerCtx().attr(GameAttributes.PACKET_TX_SUM).get();
				notificationPayload = PacketWriter.writeJoinNotification(newPlayer);

				ByteBuf notificationPacket = PacketUtils.generatePacket(existingPlayer, OPCODE_PLAYER_JOINED,
						notificationPayload,false);
				existingPlayer.getPlayerCtxChannel().eventLoop().execute(() -> {
					existingPlayer.getPlayerCtxChannel().writeAndFlush(notificationPacket);
				});
			}

		}

		notificationPayload.release(); // Libera após o uso
	}

	/**
	 * Notifica todos os jogadores no lobby (exceto o que está a sair) que um
	 * jogador saiu.
	 * 
	 * @param leavingPlayer O jogador que está a sair.
	 */
	private void broadcastPlayerLeft(PlayerSession leavingPlayer) {

		GunBoundLobby currenttLobby = leavingPlayer.getCurrentLobby();

		// logs para depuração
		System.out.println("[DEBUG] Iniciando Lobby.broadcastPlayerLeft para: " + leavingPlayer.getNickName()
				+ " no Canal ID: " + currenttLobby.getId());
		System.out.println("[DEBUG] Tamanho atual do lobby: " + currenttLobby.getPlayersInLobby().size());
		System.out.println("[DEBUG] Jogadores no lobby: " + currenttLobby.getPlayersInLobby().keySet().toString());

		System.out.println("[DEBUG] Posição do jogador que está a sair (" + leavingPlayer.getNickName() + "): "
				+ leavingPlayer.getChannelPosition());

		// Se o lobby só tiver 1 pessoa (o que está a sair), não há ninguém para
		// notificar.
		if (currenttLobby.getPlayersInLobby().size() <= 1) {
			System.out.println(leavingPlayer.getNickName() + " Saiu e não havia ninguém para notificar.");
			return;
		}

		final int OPCODE_PLAYER_LEFT = 0x200F;
		ByteBuf notificationPayload = Unpooled.buffer();

		// snapshot para evitar concorrência
		Collection<PlayerSession> recipients = new ArrayList<>(currenttLobby.getPlayersInLobby().values());
		for (PlayerSession remainingPlayer : recipients) {
			// **VERIFICAÇÃO CRUCIAL**: Não enviar a notificação para o próprio jogador que
			// está a sair.
			if (remainingPlayer.equals(leavingPlayer)) {
				System.out.println(
						"[DEBUG] Pulando notificação para o próprio jogador: " + remainingPlayer.getNickName());
				continue; // Pula para a próxima iteração do loop
			}

			//int targetTxSum = remainingPlayer.getPlayerCtx().attr(GameAttributes.PACKET_TX_SUM).get();
			notificationPayload.writeShortLE(leavingPlayer.getChannelPosition()); //gera o buffer

			ByteBuf notificationPacket = PacketUtils.generatePacket(remainingPlayer, OPCODE_PLAYER_LEFT,
					notificationPayload,false);
			System.out.println(leavingPlayer.getNickName() + " Saiu e " + remainingPlayer.getNickName() + " ["
					+ remainingPlayer.getChannelPosition() + "] Foi notificado");
			remainingPlayer.getPlayerCtxChannel().eventLoop().execute(() -> {
				remainingPlayer.getPlayerCtxChannel().writeAndFlush(notificationPacket);
			});
		}

		System.out
				.println(leavingPlayer.getNickName() + " Saiu e gerou notificação para todos os jogadores restantes.");
		notificationPayload.release();
	}

}