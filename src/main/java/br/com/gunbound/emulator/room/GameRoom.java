package br.com.gunbound.emulator.room;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import br.com.gunbound.emulator.handlers.GameAttributes;
import br.com.gunbound.emulator.model.entities.game.PlayerGameResult;
import br.com.gunbound.emulator.model.entities.game.PlayerSession;
import br.com.gunbound.emulator.packets.writers.RoomWriter;
import br.com.gunbound.emulator.playdata.MapData;
import br.com.gunbound.emulator.playdata.MapDataLoader;
import br.com.gunbound.emulator.playdata.SpawnPoint;
import br.com.gunbound.emulator.room.model.enums.GameMode;
import br.com.gunbound.emulator.utils.PacketUtils;
import br.com.gunbound.emulator.utils.Utils;
import br.com.gunbound.emulator.utils.crypto.GunBoundCipher;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;

public class GameRoom {

	// Informações básicas da sala
	private final int roomId;
	private String title;
	private String password;

	private boolean isPrivate;
	// Estado do jogo
	private volatile boolean isGameStarted;

	private int mapId;
	private int gameMode; // Ex: 0=Solo, 1=Score, 2=Tag, 3=jewel.
	private int gameSettings; // Ex: A-SIDE, DOUBLE DEATH

	private int itemState = -1;// Items: Dual,Dual+,Teleport etc.

	// Gerenciamento de jogadores
	private int capacity;
	private PlayerSession roomMaster; // O "dono" da sala
	private final Map<Integer, PlayerSession> playersBySlot = new ConcurrentHashMap<>(); // Posição (slot) -> Jogador
	private final Map<Integer, PlayerGameResult> ResultGameBySlot = new ConcurrentHashMap<>(); // Posição (slot) ->
																								// Jogador
	private final Map<Integer, Boolean> readyStatusBySlot = new ConcurrentHashMap<>(); // Posição (slot) -> Status de
																						// Pronto

	// Quando a partida é score ajustar os placares baseados na qtd de player da sala
	int scoreTeamA = 0;
	int scoreTeamB = 0;
	
	
	//Flag para ter apenas um endGame por partida se não vira bagunça
	private final AtomicBoolean endGameTriggered = new AtomicBoolean(false);


	// Fila thread-safe que mantém os IDs disponíveis, sempre oferecendo o menor
	// primeiro.
	private final Queue<Integer> availableSlots;

	/**
	 * Construtor para uma nova sala de jogo.
	 * 
	 * @param roomId     O ID único da sala.
	 * @param title      O título da sala.
	 * @param roomMaster O jogador que criou a sala.
	 * @param capacity   A capacidade máxima de jogadores.
	 */
	public GameRoom(int roomId, String title, PlayerSession roomMaster, int capacity) {
		this.roomId = roomId;
		this.title = title;
		this.roomMaster = roomMaster;
		this.capacity = capacity;
		this.password = "";
		this.isPrivate = false;
		this.isGameStarted = false;
		this.mapId = 0; // Mapa padrão

		this.availableSlots = new PriorityBlockingQueue<>(8);
		// A fila é pré-populada com um NÚMERO FINITO de slots (0 a 7)
		for (int i = 0; i < 8; i++) {
			this.availableSlots.add(i);
		}

		// Adiciona o criador da sala no primeiro slot disponível
		addPlayer(roomMaster);
	}

	// --- Getters e Setters ---

	public int getRoomId() {
		return roomId;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public boolean isPrivate() {
		return isPrivate;
	}

	public void setPassword(String password) {
		this.password = password;
		this.isPrivate = (password != null && !password.isEmpty());
	}

	public boolean checkPassword(String pass) {
		return !isPrivate || this.password.equals(pass);
	}

	public int getMapId() {
		return mapId;
	}

	public void setMapId(int mapId) {
		this.mapId = mapId;
	}

	public PlayerSession getRoomMaster() {
		return roomMaster;
	}

	public Map<Integer, PlayerSession> getPlayersBySlot() {
		return playersBySlot;
	}

	public Map<Integer, PlayerGameResult> getResultGameBySlot() {
		return ResultGameBySlot;
	}

	public void setResultGameBySlot(int slot, PlayerGameResult pGameResult) {
		ResultGameBySlot.put(slot, pGameResult);
	}

	public void cleanResultGameBySlot() {
		ResultGameBySlot.clear();
	}

	public int getPlayerCount() {
		return playersBySlot.size();
	}

	public int getCapacity() {
		return capacity;
	}

	public boolean isGameStarted() {
		return isGameStarted;
	}

	public void isGameStarted(boolean state) {
		this.isGameStarted = state;
	}

	public int getGameMode() {
		return gameMode;
	}

	public void setGameMode(int gameMode) {
		this.gameMode = gameMode;
	}

	public int getItemState() {
		return itemState;
	}

	public void setItemState(int itemState) {
		this.itemState = itemState;
	}

	public int getGameSettings() {
		return gameSettings;
	}

	public void setGameSettings(int gameConfig) {
		this.gameSettings = gameConfig;
	}

	public Integer takeSlot() {
		return this.availableSlots.poll();
	}

	public void releaseSlot(int slot) {
		this.availableSlots.add(slot);
	}

	public boolean isFull() {
		return getPlayerCount() >= getCapacity();
	}

	public int getScoreTeamA() {
		return scoreTeamA;
	}

	public void setScoreTeamA(int scoreTeamA) {
		this.scoreTeamA = scoreTeamA;
	}

	public int getScoreTeamB() {
		return scoreTeamB;
	}

	public void setScoreTeamB(int scoreTeamB) {
		this.scoreTeamB = scoreTeamB;
	}
	
	public boolean isAscoreRoom() {
		return GameMode.fromId(getGameMode()).equals(GameMode.SCORE);
	}
	
	
	public void setScoreTeam(int teamId) {
	    if (teamId == 0) {
	        int newScore = Math.max(getScoreTeamA() - 1, 0);
	        setScoreTeamA(newScore);
	        System.out.println("[DEBUG] Novo Score Team A: " + getScoreTeamA());
	    } else if (teamId == 1) {
	        int newScore = Math.max(getScoreTeamB() - 1, 0);
	        setScoreTeamB(newScore);
	        System.out.println("[DEBUG] Novo Score Team B: " + getScoreTeamB());
	    }
	}

	public boolean isTeamHasScore(int teamId) {
	    int checkScoreTeam = 0;
	    if (teamId == 0) {
	        checkScoreTeam = getScoreTeamA();
	    } else if (teamId == 1) {
	        checkScoreTeam = getScoreTeamB();
	    }
	    return checkScoreTeam > 0;
	}
	

	/**
	 * Verifica se ainda há jogadores vivos em uma equipe específica.
	 * 
	 * @param teamId O ID da equipe a ser verificada (0 para Time A, 1 para Time B).
	 * @return true se pelo menos um jogador estiver vivo, false caso contrário.
	 */
	public boolean isTeamAlive(int teamId) {
		// Itera sobre todos os jogadores na sala
		for (PlayerSession player : playersBySlot.values()) {
			// Verifica se o jogador pertence à equipe e se está vivo
			if (player.getRoomTeam() == teamId && player.getIsAlive() == 1) {
				return true; // Encontrou um jogador vivo, a equipe ainda está no jogo.
			}
		}
		// Se o loop terminar, significa que nenhum jogador vivo foi encontrado nesta
		// equipe.
		return false;
	}
	
	
	
	/**
	 * Tenta ativar o flag de fim de jogo. 
	 * Garante que o endgame só será processado uma única vez por partida.
	 * ---------------
	 * O método compareAndSet(false, true) verifica se o valor atual é false: 
	 * Se for, coloca como true e retorna true (indica que você foi o primeiro a disparar).
	 * Se já estiver true, retorna false (outro fluxo já disparou/finalizou antes).
	 *----------------
	 * @return true se é a primeira vez que está sendo chamado, false se já foi disparado antes.
	 */
	public boolean tryTriggerEndGame() {

	    return endGameTriggered.compareAndSet(false, true);
	}

	/**
	 * Reseta o controle do flag de endgame, permitindo que uma nova partida processe o fim normalmente.
	 * Deve ser chamado após preparar a sala para uma nova partida.
	 */
	public void resetEndGameFlag() {
	    endGameTriggered.set(false);
	}

	/**
	 * Realiza toda a checagem se o jogo atingiu condição de fim.
	 * Se sim, retorna o time vencedor.
	 * 
	 * Regras:
	 * - Em modo SCORE: 
	 *      O time que zerar o placar perde. Retorna o ID do time vencedor.
	 * - Em outros modos:
	 *      O time que não tem mais jogadores vivos perde. Retorna ID do vencedor.
	 * 
	 * @return 0 se o time A venceu, 1 se o time B venceu, -1 se a partida não finalizou ainda.
	 */
	public int checkGameEndAndGetWinner() {
	    if (isAscoreRoom()) {
	        if (!isTeamHasScore(0)) return 1;
	        if (!isTeamHasScore(1)) return 0;
	    }
	    if (!isTeamAlive(0)) return 1;
	    if (!isTeamAlive(1)) return 0;
	    return -1;
	}

	/**
	 * Calcula e retorna o time ideal para um novo jogador entrar, priorizando a
	 * equipe com menos membros.
	 * 
	 * @return 0 para Time A, 1 para Time B.
	 */
	private int getBalancedTeamForNewPlayer() {
		if (playersBySlot.isEmpty()) {
			return 0; // Se a sala está vazia, o primeiro jogador entra no Time A.
		}

		int teamACount = 0;
		int teamBCount = 0;

		// Itera sobre os jogadores da sala para contar os membros de cada time.
		for (PlayerSession player : playersBySlot.values()) {
			if (player.getRoomTeam() == 0) {
				teamACount++;
			} else {
				teamBCount++;
			}
		}

		// Retorna o time que tiver menos jogadores. Em caso de empate, prioriza o Time
		// A.
		if (teamACount <= teamBCount) {
			return 0; // Time A
		} else {
			return 1; // Time B
		}
	}

	/**
	 * Altera o status de "pronto" de um jogador em um determinado slot.
	 * 
	 * @param slot    O slot do jogador.
	 * @param isReady O novo status (true para pronto, false para não pronto).
	 */
	public void setPlayerReady(int slot, boolean isReady) {
		if (playersBySlot.containsKey(slot)) {
			readyStatusBySlot.put(slot, isReady);
		}
	}

	/**
	 * Obtém o status de "pronto" de um jogador em um determinado slot.
	 * 
	 * @param slot O slot do jogador.
	 * @return true se o jogador estiver pronto, false caso contrário.
	 */
	public boolean isPlayerReady(int slot) {
		return readyStatusBySlot.getOrDefault(slot, false);
	}

	/**
	 * Define a nova capacidade máxima de jogadores para a sala.
	 * 
	 * @param capacity O novo número máximo de jogadores.
	 */
	public void setCapacity(int capacity) {
		// Adicionar validação para garantir que a capacidade não seja menor que a
		// contagem atual de jogadores
		if (capacity >= this.playersBySlot.size() && capacity > 0) {
			this.capacity = capacity;
		}
	}

	// --- Métodos de Gerenciamento de Jogadores ---

	public int getRoomMasterSlot() {
		if (roomMaster == null) {
			return -1; // Não há master na sala.
		}

		PlayerSession master = getRoomMaster();

		return getSlotPlayer(master);

	}

	public int addPlayer(PlayerSession player) {
		// Validação: Impede que jogadores entrem se a sala estiver cheia ou se o jogo
		// já começou.
		if (isFull() || isGameStarted) {
			System.err.println("Tentativa falhou: Sala " + roomId + " cheia ou jogo em andamento.");
			return -1;
		}

		Integer newSlot = takeSlot();

		// Define um time padrão usando a logica para balancear as equipes
		player.setRoomTeam(getBalancedTeamForNewPlayer());

		// 1. Adiciona o jogador ao mapa de slots.
		playersBySlot.put(newSlot, player);
		// 2. Define os estados iniciais para o jogador neste slot.
		readyStatusBySlot.put(newSlot, false); // Todo jogador entra como "não pronto".
		// player.setRoomTeam(newSlot % 2); // Define um time padrão (slots pares no
		// time A, ímpares no time B).

		// 3. Associa a sala à sessão do jogador para fácil referência futura.
		player.setCurrentRoom(this);

		System.out.println("Jogador " + player.getNickName() + " entrou na sala " + roomId + " no slot " + newSlot
				+ " e time " + (player.getRoomTeam() == 0 ? "A" : "B"));

		return newSlot;
	}

	public int getSlotPlayer(PlayerSession player) {
		int slotToRemove = -1;
		for (Map.Entry<Integer, PlayerSession> entry : playersBySlot.entrySet()) {
			if (entry.getValue().equals(player)) {
				slotToRemove = entry.getKey();
				break;
			}
		}
		return slotToRemove;
	}

	/**
	 * Remove um jogador da sala.
	 * 
	 * @param player O jogador a ser removido.
	 */
	public int removePlayer(PlayerSession player) {
		int slotToRemove = -1;
		for (Map.Entry<Integer, PlayerSession> entry : playersBySlot.entrySet()) {
			if (entry.getValue().equals(player)) {
				slotToRemove = entry.getKey();
				break;
			}
		}

		if (slotToRemove != -1) {
			releaseSlot(slotToRemove);
			playersBySlot.remove(slotToRemove);
			readyStatusBySlot.remove(slotToRemove);
			player.setCurrentRoom(null); // Desassocia a sala do jogador
			System.out.println("Player " + player.getNickName() + " saiu da sala " + roomId);

			// Se o jogador que saiu era o dono da sala, elege um novo.
			if (player.equals(roomMaster)) {
				electNewRoomMaster();
			}

			// TODO: Notificar os outros jogadores que o player saiu
			// broadcastPlayerLeft(slotToRemove);
		}
		return slotToRemove;
	}

	/**
	 * Elege um novo jogador a dono da sala, caso o dono saia ou repasse a key.
	 */
	private void electNewRoomMaster() {
		if (playersBySlot.isEmpty()) {
			this.roomMaster = null;
			// A sala está vazia, pode ser destruída pelo RoomManager
			System.out.println("Sala " + roomId + " está vazia e pode ser fechada.");
		} else {
			// Elegemos o jogador no menor slot ainda ocupado
			int menorSlot = playersBySlot.keySet().stream().min(Integer::compareTo).orElse(-1);

			if (menorSlot != -1) {
				this.roomMaster = playersBySlot.get(menorSlot);
				System.out.println("Novo dono da sala " + (roomId + 1) + ": " + this.roomMaster.getNickName());
			}

		}
	}

	/**
	 * Lógica principal para iniciar o jogo.
	 */
	private static final int OPCODE_START_GAME = 0x3432;
	private final Random random = new Random();

	public void startGame(byte[] payload) {
		if (isGameStarted)
			return;

		/*
		 * boolean allReady =
		 * readyStatusBySlot.values().stream().allMatch(Boolean::booleanValue); if
		 * (!allReady && playersBySlot.size() > 1) { // Lógica de "pronto" simplificada
		 * System.err.
		 * println("Tentativa de iniciar o jogo, mas nem todos estão prontos."); return;
		 * }
		 */
		
		//Caso a sala teve um jogo anterior reseta a flag de endGame
		this.resetEndGameFlag(); 

		this.isGameStarted = true;
		System.out.println("Iniciando jogo na sala " + (roomId + 1));

		// 1. Seleciona o mapa
		if (this.mapId == 0) {
			this.mapId = random.nextInt(10) + 1; // Randomiza entre mapas de 1 a 10
		}
		MapData mapData = MapDataLoader.getMapById(this.mapId);
		if (mapData == null) {
			System.err.println("Mapa com ID " + this.mapId + " não encontrado!");
			return;
		}

		// 2. Determina os spawn points
		// List<SpawnPoint> availableSpawns = ((this.gameConfig & 1) == 0) ?
		// mapData.getPositionsASide() : mapData.getPositionsBSide();
		// List<SpawnPoint> availableSpawns = mapData.getPositionsASide();
		// Collections.shuffle(availableSpawns);

		// Collections.shuffle(availableSpawns);

		// 2. Determina os spawn points disponíveis e os embaralha.
		int isASide = (this.gameSettings >> 16) & 0xFF;// pega o byte para ver se é ASide ou BSide
		// boolean isASide = true;
		List<SpawnPoint> shuffledSpawns = new ArrayList<>(
				(isASide == 0) ? mapData.getPositionsASide() : mapData.getPositionsBSide());
		Collections.shuffle(shuffledSpawns);

		Map<Integer, SpawnPoint> playerSpawns = new HashMap<>();
		int spawnIndex = 0;

		// 3. Associa um spawn point para cada jogador e seta alive para cada um deles.
		for (Map.Entry<Integer, PlayerSession> entry : playersBySlot.entrySet()) {
			int slot = entry.getKey();
			PlayerSession player = entry.getValue();

			// seta para "vivo" ao iniciar o jogo
			player.setIsAlive(1);

			System.out.println("[DEBUG] Associando slot do player: " + player.getNickName() + " [" + slot + "]");

			// Associa o spawn point ao slot do jogador.
			playerSpawns.put(slot, shuffledSpawns.get(spawnIndex % shuffledSpawns.size()));
			spawnIndex++;

			// Randomiza os tanques aqui, pois já estamos iterando sobre os jogadores.
			// if (player.getRoomTankPrimary() == 0xFF) {
			// player.setRoomTankPrimary(random.nextInt(14));
			// player.setRoomTankPrimary(Utils.randomMobile(99));
			// }
			// if (player.getRoomTankSecondary() == 0xFF) {
			// player.setRoomTankSecondary(random.nextInt(14));
			// player.setRoomTankSecondary(Utils.randomMobile(99));
			// }
		}

		// 4. Determina a ordem dos turnos
		List<Integer> turnOrder = new ArrayList<>();
		for (int i = 0; i < playersBySlot.size(); i++) {
			turnOrder.add(i);
		}
		Collections.shuffle(turnOrder);

		// 5. Baseado no estilo de jogo seta o score padrao
		if (isAscoreRoom()) {
			int size = playersBySlot.size() / 2;
			// verifica se as equipes estao desbalanceadas (Ex: Gm start 3x2)
			size = (size % 2 == 0) ? size : size + 1;

			setScoreTeamA(size + 1);
			setScoreTeamB(size + 1);
			
			System.out.println("DEBUG Entrou no if do Score: " + GameMode.fromId(gameMode).getName());
			System.out.println("ScoreTeam A" + getScoreTeamA());
			System.out.println("ScoreTeam B" + getScoreTeamA());
		}
		


		// 6. Constrói e envia o pacote de início
		ByteBuf startPayload = Unpooled
				.wrappedBuffer(RoomWriter.writeGameStartPacketTest(this, turnOrder, playerSpawns, payload));
		System.out.println("[DEBUG] Antes Start -> " + Utils.bytesToHex(startPayload.array()));

		// 6. Envia o pacote criptografado e com padding para cada jogador
		for (PlayerSession player : playersBySlot.values()) {
			try {
				// System.out.println("[DEBUG] Start -> " + Utils.bytesToHex(bytesToEncrypt));
				byte[] encryptedPayload = GunBoundCipher.gunboundDynamicEncrypt(
						startPayload.retainedDuplicate().array(), // Usa o array com
						// padding
						player.getUserNameId(), player.getPassword(),
						player.getPlayerCtxChannel().attr(GameAttributes.AUTH_TOKEN).get(), OPCODE_START_GAME);

				// Envia o pacote criptografado
				//int txSum = player.getPlayerCtx().attr(GameAttributes.PACKET_TX_SUM).get();
				ByteBuf finalPacket = PacketUtils.generatePacket(player, OPCODE_START_GAME,
						Unpooled.wrappedBuffer(encryptedPayload),false);

				// Thread.sleep(150);
				submitAction(() -> 
				player.getPlayerCtxChannel().eventLoop().execute(() -> {
					player.getPlayerCtxChannel().writeAndFlush(finalPacket);
				}),player.getPlayerCtx());

				// player.getPlayerCtx().eventLoop().schedule(() -> {
				// player.getPlayerCtx().writeAndFlush(finalPacket);
				// }, 500, java.util.concurrent.TimeUnit.MILLISECONDS);

			} catch (Exception e) {
				System.err.println("Falha ao criptografar ou enviar pacote de início para " + player.getNickName());
				e.printStackTrace();
			}
		}

		startPayload.release();
		System.out.println("Pacotes de início de jogo enfileirados para " + getPlayerCount() + " jogadores.");
	}

	/**
	 * Notifica TODOS os jogadores na sala com o comando de atualização (0x3105). A
	 * própria sala é responsável por criar e enviar o pacote para cada membro com a
	 * sequência correta.
	 */
	private static final int OPCODE_ROOM_UPDATE = 0x3105;

	public void broadcastRoomUpdate() {

		// O payload da notificação é vazio.
		ByteBuf notifyPayload = Unpooled.EMPTY_BUFFER;

		System.out.println("Iniciando broadcast de atualização (0x3105) para a sala " + this.roomId);

		// snapshot para evitar concorrência
		List<PlayerSession> recipients = new ArrayList<>(getPlayersBySlot().values());

		// for (PlayerSession playerInRoom : playersBySlot.values()) {
		for (PlayerSession playerInRoom : recipients) {
			try {
				// Gera um pacote com a sequência CORRETA para este jogador.
				ByteBuf notifyPacket = PacketUtils.generatePacket(playerInRoom, OPCODE_ROOM_UPDATE, notifyPayload, true);

				// Usamos writeAndFlush com um listener para capturar erros.
				
				submitAction(() -> 
				playerInRoom.getPlayerCtxChannel().writeAndFlush(notifyPacket).addListener((ChannelFutureListener) future -> {
					if (!future.isSuccess()) {
						System.err.println("FALHA AO ENVIAR PACOTE DE TÚNEL para: " + playerInRoom.getNickName());
						future.cause().printStackTrace();
						// Caso o jogador nao esteja impossibilitado de receber pacotes.
						playerInRoom.getPlayerCtxChannel().close();
					}
				}),playerInRoom.getPlayerCtx());

				// playerInRoom.getPlayerCtx().eventLoop().execute(() -> {
				// Envia o pacote individualmente.
				// playerInRoom.getPlayerCtx().writeAndFlush(notifyPacket);
				// });

			} catch (Exception e) {
				System.err.println("Falha ao enviar broadcast de atualização para " + playerInRoom.getNickName());
				e.printStackTrace();
			}
		}
	}

	/**
	 * Notifica os jogadores existentes na sala que um novo jogador entrou.
	 * 
	 * @param newPlayer     O jogador que acabou de entrar.
	 * @param newPlayerSlot O slot que o novo jogador ocupou.
	 */
	private static final int OPCODE_NOTIFY_JOIN = 0x3010;

	public void notifyOthersPlayerJoined(PlayerSession newPlayer) {
		// Cria o payload da notificação UMA VEZ.
		ByteBuf notifyPayload = Unpooled.buffer();

		// List<PlayerSession> recipients = new
		// ArrayList<>(getPlayersBySlot().values());

		for (PlayerSession playerInRoom : playersBySlot.values()) {
			// for (PlayerSession playerInRoom : recipients) {

			// Envia para todos, EXCETO o jogador que acabou de entrar.
			if (!playerInRoom.equals(newPlayer)) {
				notifyPayload = RoomWriter.writeNotifyPlayerJoinedRoom(newPlayer);
				//int playerTxSum = playerInRoom.getPlayerCtx().attr(GameAttributes.PACKET_TX_SUM).get();
				// A notificação de join (0x3010) não usa RTC.
				ByteBuf notifyPacket = PacketUtils.generatePacket(playerInRoom, OPCODE_NOTIFY_JOIN,
						notifyPayload.retainedDuplicate(),false);

				submitAction(() -> 
				playerInRoom.getPlayerCtxChannel().eventLoop().execute(() -> {
					playerInRoom.getPlayerCtxChannel().writeAndFlush(notifyPacket);
				}),playerInRoom.getPlayerCtx());
			}
		}

		notifyPayload.release();
		System.out.println("Notificação de entrada (0x3010) enviada para " + (getPlayerCount() - 1) + " jogador(es).");
	}

	/**
	 * Notifica os jogadores restantes que um jogador saiu de um slot específico.
	 * 
	 * @param leftPlayerSlot O slot que ficou vago.
	 */

	private static final int OPCODE_PLAYER_LEFT = 0x3020;

	public void notifyPlayerLeft(int leftPlayerSlot,boolean wasHost) {
		// O payload é um short (2 bytes) contendo o ID do slot.
		ByteBuf payload = Unpooled.buffer().writeShortLE(leftPlayerSlot);

		// snapshot para evitar concorrência
		Collection<PlayerSession> recipients = new ArrayList<>(getPlayersBySlot().values());
		for (PlayerSession playerInRoom : recipients) {
			// Pega a soma de pacotes para ESTE jogador específico.
			//int playerTxSum = playerInRoom.getPlayerCtx().attr(GameAttributes.PACKET_TX_SUM).get();

			// Gera um pacote com a sequência correta para este jogador.
			ByteBuf notifyPacket = PacketUtils.generatePacket(playerInRoom, OPCODE_PLAYER_LEFT,
					payload.retainedDuplicate(),false);

			// Envia o pacote individualmente.
			//playerInRoom.getPlayerCtxChannel().eventLoop().execute(() -> {
				//playerInRoom.getPlayerCtxChannel().writeAndFlush(notifyPacket);
			//});
			
			
			playerInRoom.getPlayerCtxChannel().writeAndFlush(notifyPacket).addListener((ChannelFutureListener) future -> {
	                if (future.isSuccess()) {
	        			// Se quem saiu era o host, notifica sobre a migração.
	        			if (wasHost) {
	        				System.out.println("[DEBUG]: Entrou no if de washost" );
	        				notifyHostMigration();
	        				//room.submitAction(() -> room.notifyHostMigration());
	        			}
	                }
	            });
			

			
		}

		payload.release();
		System.out.println("Notificação de saída do slot (0x3020) " + leftPlayerSlot + " enviada para a sala.");
	}

	/**
	 * Notifica os jogadores restantes sobre a migração do host.
	 */
	private static final int OPCODE_HOST_MIGRATION = 0x3400;

	public void notifyHostMigration() {

		this.electNewRoomMaster();

		System.out.println("[DEBUG] Slot do Novo Master:" + this.getRoomMasterSlot());

		// O RoomWriter constrói o payload complexo da migração.
		// ByteBuf payload = RoomWriter.writeHostMigrationPacket(this);
		ByteBuf buffer = Unpooled.buffer();
		// Escreve o payload conforme a referência
		buffer.writeByte(getRoomMasterSlot());

		byte[] titleBytes = getTitle().getBytes(StandardCharsets.ISO_8859_1);
		buffer.writeByte(titleBytes.length);
		buffer.writeBytes(titleBytes);

		buffer.writeByte(getMapId());
		buffer.writeIntLE(getGameSettings());
		buffer.writeIntLE(getItemState());
		buffer.writeBytes(new byte[] { (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF });
		// buffer.writeBytes(new byte[] { (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte)
		// 0xFF, (byte) 0xFF, (byte) 0xFF,
		// (byte) 0xFF, (byte) 0xFF });
		buffer.writeByte(getCapacity());

		// snapshot para evitar concorrência
		Collection<PlayerSession> recipients = new ArrayList<>(getPlayersBySlot().values());
		for (PlayerSession playerInRoom : recipients) {
			//int playerTxSum = playerInRoom.getPlayerCtx().attr(GameAttributes.PACKET_TX_SUM).get();

			// Gera um pacote com a sequência correta para este jogador.
			ByteBuf notifyPacket = PacketUtils.generatePacket(playerInRoom, OPCODE_HOST_MIGRATION,
					buffer.retainedDuplicate(),false);

			// Envia o pacote individualmente.
 
			// Envia o pacote individualmente.
			playerInRoom.getPlayerCtxChannel().eventLoop().execute(() -> {
				playerInRoom.getPlayerCtxChannel().writeAndFlush(notifyPacket);
			});
		}

		buffer.release();
		System.out.println("Notificação de migração de host (0x3400) enviada para a sala.");
	}

	// *****************************FILA PARA PROCESSAR A
	// SALA*****************************

    // Classe interna que associa a ação ao seu contexto do Netty
    private static class QueuedAction {
        final Runnable task;
        final ChannelHandlerContext ctx;

        QueuedAction(Runnable task, ChannelHandlerContext ctx) {
            this.task = task;
            this.ctx = ctx;
        }
    }

    // Fila thread-safe para as ações
    private final Queue<QueuedAction> actionQueue = new ConcurrentLinkedQueue<>();
    private final AtomicBoolean processing = new AtomicBoolean(false);
    
    private static final ExecutorService workerPool = Executors.newFixedThreadPool(16);


    // Enfileira ação + contexto
    public void submitAction(Runnable action, ChannelHandlerContext ctx) {
        actionQueue.add(new QueuedAction(action, ctx));
        processNext(); // Tenta processar (só dispara se não estiver em processamento)
    }

    // Controle thread-safe: só um processador por vez
    private void processNext() {
        if (processing.compareAndSet(false, true)) {
            nextAction();
        }
    }

    private void nextAction() {
        QueuedAction act = actionQueue.poll();
        if (act != null) {
            // Executa a ação no event loop certo do Netty (para evitar problema de concorrência no canal)
        	
        	//workerPool.submit(() -> {
            act.ctx.executor().execute(() -> {
                try {
                    act.task.run();
                } catch (Exception e) {
                    System.out.println("[ERRO] Exceção em Room Action: " + e.getMessage());
                    e.printStackTrace();
                } finally {
                    nextAction();
                }
            });
            
        	//});
        } else {
            // Ninguém na fila: libera o flag de processamento
            processing.set(false);
            // Confere se, enquanto processava, outra thread não enfileirou nova ação nesse meio tempo
            if (!actionQueue.isEmpty()) {
                processNext();
            }
        }
    }

}