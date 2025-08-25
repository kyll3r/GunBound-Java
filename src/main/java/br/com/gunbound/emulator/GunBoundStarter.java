package br.com.gunbound.emulator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import br.com.gunbound.emulator.model.entities.ServerOption;

public class GunBoundStarter {
    private static final int BROKER_PORT = 8400;
    private static final int GAME_SERVER_PORT = 8360;
    private static final String SERVER_HOST = "192.168.0.111"; // Escuta em todas as interfaces de rede

	public static void main(String[] args) {
		// Usa um ExecutorService para iniciar o broker e o game server em threads
		// separadas.
		ExecutorService executor = Executors.newFixedThreadPool(2);

		// Lista que simula as sessões de jogadores no servidor de jogo.
		// O broker precisa saber o número de jogadores para reportar a ocupação.
		// É importante que essa lista seja thread-safe, por isso
		// Collections.synchronizedList.
		List<Object> gameServerSessions = Collections.synchronizedList(new ArrayList<>());

		// Lista de servidores que o broker irá mostrar aos clientes.
		List<ServerOption> serverOptions = new ArrayList<>();
		try {
			// Adiciona o servidor de jogo local à lista que o broker irá mostrar.
			serverOptions.add(new ServerOption("GunBound Legacy", "Avatar OFF", "192.168.0.111",
					GAME_SERVER_PORT, 0, 500, true));
		} catch (Exception e) {
			System.err.println("Erro ao criar ServerOption para o Game Server: " + e.getMessage());
			return; // Aborta se não conseguir configurar a opção do servidor.
		}

		// --- 1. Inicializa o Game Server ---
		// O GameServer é iniciado primeiro para garantir que ele esteja online
		// Para depois o Broker Server começar a aceitar conexões e direcionar os clientes.
		GunBoundGameServer gameServer = new GunBoundGameServer(GAME_SERVER_PORT);
		AtomicBoolean gameServerStarted = new AtomicBoolean(false); // Flag para verificar se o Game Server iniciou com
																	// sucesso.

		executor.submit(() -> {
			try {
				gameServer.start();
				gameServerStarted.set(true); // Marca como iniciado com sucesso.
			} catch (Exception e) {
				System.err.println("Falha ao iniciar o Game Server na porta " + GAME_SERVER_PORT + "!");
				e.printStackTrace();
			}
		});

		// Pequena pausa para dar tempo ao Game Server de inicializar completamente.
		// Prática comum para garantir a ordem de inicialização em sistemas distribuídos.
		try {
			Thread.sleep(2000); // Espera 2 segundos.
		} catch (InterruptedException e) {
			System.err.println("Inicialização do Game Server interrompida.");
			Thread.currentThread().interrupt(); // Restaura o status de interrupção.
		}

		// Verifica se o Game Server realmente iniciou.
		if (!gameServerStarted.get()) {
			System.err.println("Não foi possível iniciar o Game Server. Abortando inicialização do Broker Server.");
			executor.shutdownNow(); // Encerra todos os threads no executor.
			return;
		}

		// --- 2. Inicializa o GunBoundBrokerServer ---
		// O GunBoundBrokerServer agora está configurado para anunciar e potencialmente
		// direcionar
		// clientes para o Game Server que acabou de ser iniciado.
		// A lista 'gameServerSessions' é passada para o broker para que ele possa
		// reportar
		// a ocupação atual do servidor de jogo.
		GunBoundBrokerServer gunBoundBrokerServer = new GunBoundBrokerServer(SERVER_HOST, BROKER_PORT, serverOptions,
				gameServerSessions);
		executor.submit(() -> {
			try {
				gunBoundBrokerServer.start();
			} catch (Exception e) {
				System.err.println("Falha ao iniciar o Broker Server na porta " + BROKER_PORT + "!");
				e.printStackTrace();
			}
		});

		System.out.println("--- Gunbound Starter Iniciado ---");
		System.out.println("Broker Server escutando em " + SERVER_HOST + ":" + BROKER_PORT);
		System.out.println("Game Server escutando em " + SERVER_HOST + ":" + GAME_SERVER_PORT);
		System.out.println("Pressione Ctrl+C para encerrar.");

		// O thread principal agora aguarda o encerramento dos threads do executor.
		// Isso mantém a aplicação Java em execução até que os servidores sejam
		// desligados.
		try {
			executor.awaitTermination(Long.MAX_VALUE, TimeUnit.HOURS);
		} catch (InterruptedException e) {
			System.out.println("Servidores encerrados por interrupção.");
			Thread.currentThread().interrupt();
		} finally {
			// Garante que o executor será encerrado em qualquer caso de término.
			executor.shutdownNow();
			System.out.println("--- Gunbound Starter Encerrado ---");
		}
	}
}