package br.com.gunbound.emulator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import br.com.gunbound.emulator.handlers.GunBoundBrokerServerHandler;
import br.com.gunbound.emulator.model.entities.ServerOption;
import br.com.gunbound.emulator.utils.PacketDecoder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
// Importa o novo EventLoopGroup e o handler de I/O para NIO
import io.netty.channel.MultiThreadIoEventLoopGroup;
import io.netty.channel.nio.NioIoHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;

/**
 * A classe principal que configura e inicia o servidor broker do GunBound
 * usando Netty. Esta versão utiliza MultiThreadIoEventLoopGroup, conforme a
 * recomendação mais recente do Netty.
 */
public class GunBoundBrokerServer {

	private String host = "";
	private int port = 0;
	private List<ServerOption> serverOptions = null;
	// Lista para rastrear sessões ativas, usada para simular a ocupação dos
	// servidores.
	private final List<Object> worldSession;

	public GunBoundBrokerServer(String host, int port, List<ServerOption> serverOptions, List<Object> worldSession) {
		this.host = host;
		this.port = port;
		this.serverOptions = serverOptions;
		// Torna a lista sincronizada para ser thread-safe, já que múltiplos clientes
		// podem acessá-la.
		this.worldSession = Collections.synchronizedList(worldSession);
	}

	public void start() throws Exception {
		// EventLoopGroup para aceitar novas conexões (boss).
		// Usamos MultiThreadIoEventLoopGroup com o handler de I/O específico para NIO.
		EventLoopGroup bossGroup = new MultiThreadIoEventLoopGroup(1, NioIoHandler.newFactory());
		// EventLoopGroup para lidar com o tráfego das conexões aceitas (worker).
		EventLoopGroup workerGroup = new MultiThreadIoEventLoopGroup(0, NioIoHandler.newFactory()); // 0 threads = usa o
																									// número de threads
																									// padrão (2 *
																									// número de
																									// núcleos).

		try {
			// ServerBootstrap é uma classe de conveniência para configurar um servidor.
			ServerBootstrap b = new ServerBootstrap();
			b.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class) // Continua usando
																					// NioServerSocketChannel, pois ele
																					// é compatível com o handler de
																					// I/O.
					.childHandler(new ChannelInitializer<SocketChannel>() {
						@Override
						public void initChannel(SocketChannel ch) {
							
							/* Verifica se a propriedade do sistema 'debug.netty.log' está definida como 'true'
					        boolean enableNettyLog = Boolean.parseBoolean(System.getProperty("debug.netty.log", "false"));

					        if (enableNettyLog) {
					            // Adiciona o LoggingHandler somente caso o log de depuração estiver ativado
					            ch.pipeline().addLast(new LoggingHandler("BrokerLogger", LogLevel.DEBUG));
					        }*/
							
							
							  // Adiciona os handlers ao pipeline de cada nova conexão.
                            ch.pipeline().addLast(
                       	
                            		 // Detecta 60 segundos sem nenhum dado do cliente
                                    new IdleStateHandler(180, 0, 0, TimeUnit.SECONDS),
                                    // 1. LoggingHandler para depuração do tráfego de entrada e saída.
                                    // Se no início do pipeline veremos os bytes brutos da rede.
                                    new LoggingHandler("BrokerLogger", LogLevel.DEBUG),
                                    
                                    // 2. Decoder para separar os pacotes.
                                    new PacketDecoder(),
                                    
                                    // 3. Handler para rastrear o tamanho dos pacotes de saída
                                    new PacketSizeTracker(), 
                                    
                                    // 4. Handler com a lógica de negócio.
                                    new GunBoundBrokerServerHandler(serverOptions, worldSession)
                            );
						}
					}).option(ChannelOption.SO_BACKLOG, 128) // Configurações de socket.
					.childOption(ChannelOption.SO_KEEPALIVE, true);

			System.out.println("Broker TCP Bound");
			System.out.println("GunBound Broker - Diretório de Servidores:");
			for (ServerOption serverOption : serverOptions) {
				System.out.println("Servidor: " + serverOption.getServerName() + " - "
						+ serverOption.getServerDescription() + " na porta " + serverOption.getServerPort());
			}

			// Vincula a porta e começa a aceitar conexões.
			ChannelFuture f = b.bind(host, port).sync();
			System.out.println("BS: Escutando em " + host + ":" + port);

			// Espera até que o socket do servidor seja fechado.
			f.channel().closeFuture().sync();
		} finally {
			// Encerra os EventLoopGroups elegantemente.
			workerGroup.shutdownGracefully();
			bossGroup.shutdownGracefully();
		}
	}

	
	//esse metodo main serve apenas para startar o broker sem o game server (Correto é no GunBoundStarter.java)
	public static void main(String[] args) throws Exception {
		// Define o endereço padrão (0.0.0.0 para todas as interfaces)
		String host = "0.0.0.0";
		int brokerPort = 8400; // A porta padrão do Broker Server

		// Verifica se o usuário forneceu um IP e/ou porta como argumento
		if (args.length > 0) {
			host = args[0]; // O primeiro argumento é o IP
		}
		if (args.length > 1) {
			try {
				brokerPort = Integer.parseInt(args[1]); // O segundo argumento é a porta
			} catch (NumberFormatException e) {
				System.err.println("Porta inválida! Usando a porta padrão: " + brokerPort);
			}
		}

		// --- Configure a lista de servidores aqui ---
		List<ServerOption> serverOptions = new ArrayList<>();
		serverOptions.add(new ServerOption("GunBound Legacy", "AVATAR OFF", "127.0.0.1", 8360, 0, 500, true));
		serverOptions.add(new ServerOption("GunBound Legacy", "AVATAR ON", "127.0.0.1", 8361, 0, 100, true));

		// Esta lista simula as sessões de jogadores para calcular a ocupação.
		// Em uma aplicação real, você a gerencia de forma mais robusta.
		List<Object> worldSession = new ArrayList<>();

		// Cria e inicia o servidor.
		new GunBoundBrokerServer(host, brokerPort, serverOptions, worldSession).start();
	}
}