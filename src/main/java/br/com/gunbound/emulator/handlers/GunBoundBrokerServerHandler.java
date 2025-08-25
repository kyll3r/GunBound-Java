package br.com.gunbound.emulator.handlers;

import java.net.InetSocketAddress;
import java.util.List;

import br.com.gunbound.emulator.Connection;
import br.com.gunbound.emulator.ConnectionManager;
import br.com.gunbound.emulator.model.entities.ServerOption;
import br.com.gunbound.emulator.utils.PacketUtils;
import br.com.gunbound.emulator.utils.Utils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.AttributeKey;

public class GunBoundBrokerServerHandler extends ChannelInboundHandlerAdapter {

	// Obtém a instância única do ConnectionManager
	private final ConnectionManager connectionManager = ConnectionManager.getInstance();

	private final List<ServerOption> serverOptions;
	private final List<Object> worldSession; // Lista de sessões ativas (para simular a ocupação)
	// Utiliza AtomicInteger para a soma do tamanho dos pacotes, pois é thread-safe.
	// (NAO TA USANDO)
	// private final AtomicInteger socketRxSum = new AtomicInteger(0);

	// Chave de atributo para armazenar a soma dos tamanhos dos pacotes enviados
	// (MUITO IMPORTANTE!).
	public static final AttributeKey<Integer> PACKET_TX_SUM = AttributeKey.valueOf("packetTxSum");
	int currentTxSum = 0; // isso aqui ta aqui porque eu preciso sempre pegar a soma dos pacotes

	public GunBoundBrokerServerHandler(List<ServerOption> serverOptions, List<Object> worldSession) {
		this.serverOptions = serverOptions;
		this.worldSession = worldSession;
	}

	/**
	 * Chamado quando a conexão com o cliente é estabelecida.
	 */
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		// Obtenha o endereço remoto do canal (do cliente)
		InetSocketAddress remoteAddress = (InetSocketAddress) ctx.channel().remoteAddress();

		// Obtenha o IP e a porta
		String clientIp = remoteAddress.getAddress().getHostAddress();
		int clientPort = remoteAddress.getPort();

		System.out.println("BS: Cliente conectado! IP: " + clientIp + ", Porta: " + clientPort);

		// Inicializa o atributo de soma de pacotes enviados para esta nova conexão.
		ctx.channel().attr(PACKET_TX_SUM).set(0);

		// Registra a nova conexão no nosso gerenciador
		Channel channel = ctx.channel();
		connectionManager.registerConnection(channel);

		System.out.println("BS: Total de conexões ativas: " + connectionManager.getActiveConnectionCount());

		// Armazena informações no contexto do canal se precisar delas mais tarde
		// ctx.channel().attr(MY_CUSTOM_ATTRIBUTE_KEY).set(clientIp);

		super.channelActive(ctx);
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) {
		ByteBuf in = (ByteBuf) msg;// faz casting para ByteBuf
		try {

			// Logar o pacote recebido ANTES de ser lido
			// Para isso, precisamos garantir que o ByteBuf não seja esgotado pela leitura
			// Usamos `duplicate()` para criar uma cópia que podemos ler sem afetar o buffer
			// original.
			ByteBuf packetData = in.duplicate();

			// Converte os bytes do pacote para uma string hexadecimal para exibição
			String hexDump = Utils.toHexString(packetData);

			// Se precisar do UUID, você pode obtê-lo aqui
			// Obtém as informações da conexão para o log
			Connection clientConnection = connectionManager.getConnection(ctx.channel());
			String connectionInfo = (clientConnection != null)
					? clientConnection.getIp() + " (ID: " + clientConnection.getId() + ")"
					: "Desconhecido";

			System.out.println("\n--- Pacote Recebido de " + connectionInfo + " ---");
			System.out.println("Tamanho do pacote: " + in.readableBytes() + " bytes");
			System.out.println("Conteúdo do pacote (Hex): " + hexDump);
			System.out.println("----------------------------------------------\n");

			// O decoder já removeu os 2 bytes de tamanho do início do pacote.
			if (in.readableBytes() < 4) {
				System.err.println("Pacote inválido (tamanho < 4 após decodificação)");
				return;
			}

			// Lê a sequência e o comando do pacote (2 bytes cada, little-endian).
			int packetSequence = in.readUnsignedShortLE();
			int clientCommand = in.readUnsignedShortLE();

			System.out.println(""); // Linha em branco para realizar formatação no console.

			// Pega a soma dos pacotes já enviados para este canal.
			// O valor inicial é 0, definido em channelActive.
			currentTxSum = ctx.channel().attr(PACKET_TX_SUM).get();

			// Atualiza a soma do tamanho dos pacotes recebidos (para o cálculo da sequência
			// de resposta).
			// socketRxSum.addAndGet(in.readableBytes() + 6); // +6 para o cabeçalho
			// original.

			// Responde ao cliente se o comando for reconhecido.
			if (clientCommand == 0x1013) {
				System.out.println("BS: Requisição de Autenticação (0x1013)");
				// O payload de resposta é apenas 2 bytes (0x0000), big-endian.
				ByteBuf loginPayload = Unpooled.buffer(2).writeShort(0x0000);

				// O primeiro pacote tem uma sequência especial, então usamos -1 como flag.
				ByteBuf loginPacket = PacketUtils.generatePacket(-1, 0x1312, loginPayload);

				// Envia a resposta e libera o buffer.
				ctx.writeAndFlush(loginPacket);
				loginPayload.release();
			} else if (clientCommand == 0x1100) {
				System.out.println("BS: Requisição de Diretório de Servidores (0x1100)");
				handleServerListRequest(ctx, in);
			} else {
				System.out.println("BS: Comando desconhecido: 0x" + Integer.toHexString(clientCommand));
				// Pode-se optar por fechar a conexão se um comando desconhecido for recebido.
				// ctx.close();
			}
		} finally

		{
			in.release(); // Importante liberar o ByteBuf!
		}
	}

	// Lógica para o comando de login ISSO AQUI NA TA SENDO USADO!
	private void handleLoginRequest(ChannelHandlerContext ctx, ByteBuf in) {
		// A lógica de autenticação é mínima no broker server
		// Você pode apenas ler o resto do pacote se quiser, mas a resposta é fixa
		System.out.println("Solicitação de login recebida. Respondendo...");

		// Crie o pacote de resposta (0x1312)
		ByteBuf response = ctx.alloc().buffer();
		response.writeShortLE(0x1312); // Comando de resposta de login
		// ... adicione outros dados necessários para a resposta de sucesso

		ctx.writeAndFlush(response); // Envie a resposta de volta ao cliente
	}

	// Lógica para o comando de lista de servidores
	private void handleServerListRequest(ChannelHandlerContext ctx, ByteBuf in) {
		System.out.println("Solicitação de lista de servidores recebida. Respondendo...");

		ByteBuf directoryPayload = Unpooled.buffer();
		directoryPayload.writeBytes(new byte[] { 0x00, 0x00, 0x01 }); // Bytes desconhecidos
		directoryPayload.writeByte(serverOptions.size()); // Número de servidores na lista

		// Usamos a lista thread-safe para obter a ocupação atual.
		int currentUtilization = worldSession.size();

		// Constrói o payload do diretório, um servidor por vez.
		for (int i = 0; i < serverOptions.size(); i++) {
			ServerOption option = serverOptions.get(i);
			// Atualiza a ocupação do servidor antes de enviá-lo.
			option.setServerUtilization(currentUtilization);
			directoryPayload.writeBytes(PacketUtils.getIndividualServer(option, i));
		}

		// Gera o pacote completo e o envia.
		System.out.println("Pacote enviado size: " + currentTxSum);
		ByteBuf directoryPacket = PacketUtils.generatePacket(currentTxSum, 0x1102, directoryPayload);

		System.out.println("SEND >> 0x1102");

		ctx.writeAndFlush(directoryPacket).addListener(future -> {
			// A operação de envio (writeAndFlush) está completa.
			if (future.isSuccess()) {
				// A mensagem foi enviada com sucesso.
				System.out.println("Pacote 0x1102 enviado com sucesso para o cliente.");
			} else {
				// Houve uma falha no envio.
				System.err.println("Falha ao enviar o pacote 0x1102: " + future.cause().getMessage());
				future.cause().printStackTrace();
				// Aqui você pode decidir o que fazer, como fechar a conexão, por exemplo.
				ctx.close();
			}
			directoryPayload.release();
		});
	}

	/**
	 * Chamado quando a conexão com o cliente é encerrada.
	 */
	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		// Remove a conexão do gerenciador quando o cliente se desconecta
		connectionManager.removeConnection(ctx.channel());
		System.out.println("BS: Total de conexões ativas: " + connectionManager.getActiveConnectionCount());
		connectionManager.removeConnection(ctx.channel());
		super.channelInactive(ctx);
		ctx.close();
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		cause.printStackTrace();
		ctx.close();
	}
	
	//fechando conexao apos inatividade
	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
	    if (evt instanceof IdleStateEvent) {
	        IdleStateEvent event = (IdleStateEvent) evt;

	        //quando nenhum dado foi recebido
	        if (event.state() == IdleState.READER_IDLE || event.state() == IdleState.WRITER_IDLE) {
	            System.out.println("BS: Conexão inativa detectada: " + ctx.channel().remoteAddress());
	            System.out.println("BS: Total de conexões ativas: " + connectionManager.getActiveConnectionCount());
				connectionManager.removeConnection(ctx.channel());
	            super.channelInactive(ctx);
	            ctx.close(); // Fecha a conexão por inatividade
	        }
	    } else {
	        super.userEventTriggered(ctx, evt);
	    }
	}

}