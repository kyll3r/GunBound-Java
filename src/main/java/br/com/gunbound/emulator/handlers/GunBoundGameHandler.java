package br.com.gunbound.emulator.handlers;

import java.net.InetSocketAddress;
import java.util.function.BiConsumer;

import br.com.gunbound.emulator.ConnectionManager;
import br.com.gunbound.emulator.lobby.GunBoundLobbyManager;
import br.com.gunbound.emulator.model.entities.game.PlayerSession;
import br.com.gunbound.emulator.model.entities.game.PlayerSessionManager;
import br.com.gunbound.emulator.packets.OpcodeReaderFactory;
import br.com.gunbound.emulator.room.RoomManager;
import br.com.gunbound.emulator.utils.PacketUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter; // Importação alterada

public class GunBoundGameHandler extends ChannelInboundHandlerAdapter {

	// Obtém a instância única do ConnectionManager
	private final ConnectionManager connectionManager = ConnectionManager.getInstance();

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		// Obtenha o endereço remoto do canal (do cliente)
		InetSocketAddress remoteAddress = (InetSocketAddress) ctx.channel().remoteAddress();

		// Obtenha o IP e a porta
		String clientIp = remoteAddress.getAddress().getHostAddress();
		int clientPort = remoteAddress.getPort();

		System.out.println("GS: Cliente conectado! IP: " + clientIp + ", Porta: " + clientPort);

		// Inicializa o atributo de soma de pacotes enviados para esta nova conexão.
		ctx.channel().attr(GameAttributes.PACKET_TX_SUM).set(0);

		// Inicializa o atributo de AUTH TOKEN
		ctx.channel().attr(GameAttributes.AUTH_TOKEN).set(new byte[4]);

		// Inicializa o atributo que guarda a versao do cliente do player
		ctx.channel().attr(GameAttributes.CLIENT_VERSION).set(0);

		// Inicializa o atributo USER
		// ctx.channel().attr(GameAttributes.USER_SESSION).set(new PlayerSession());

		// Registra a nova conexão no nosso gerenciador
		Channel channel = ctx.channel();
		connectionManager.registerConnection(channel);

		System.out.println("GS: Total de conexões ativas: " + connectionManager.getActiveConnectionCount());

		// Você pode armazenar essas informações no contexto do canal se precisar delas
		// mais tarde
		// ctx.channel().attr(MY_CUSTOM_ATTRIBUTE_KEY).set(clientIp);

		super.channelActive(ctx);
	}

	/**
	 * Processa um pacote de entrada do cliente de jogo. Recebe um Object e precisa
	 * ser gerenciado manualmente .
	 * 
	 * @param ctx O contexto do canal.
	 * @param msg O objeto da mensagem (que esperamos ser um ByteBuf).
	 */
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) { // Método alterado
		ByteBuf in = null; // Declarado fora do try para garantir acesso no finally (IMPORTANTE!)

		try {
			if (!(msg instanceof ByteBuf)) {
				System.err.println("GS: Mensagem recebida não é um ByteBuf. Tipo: " + msg.getClass().getName());
				// Encaminha a mensagem para o próximo handler ou fecha a conexão se for
				// inesperado.
				ctx.fireChannelRead(msg);
				return;
			}

			in = (ByteBuf) msg; // Faz o cast para ByteBuf

			// Verifica se o pacote tem tamanho suficiente para o cabeçalho mínimo
			// (sequência + comando)
			if (in.readableBytes() < 4) {
				System.err.println("GS: Pacote de jogo inválido (tamanho < 4 após decodificação)");
				return; // Não libera o buffer aqui (PELO AMOR!), pois espera mais dados se for
						// incompleto.
			}

			// 1. Lê a sequência do pacote (2 bytes, little-endian).
			// Agora o ponteiro de leitura do ByteBuf avança.
			int sequence = in.readUnsignedShortLE();

			// 2. Lê o comando (2 bytes, little-endian).
			int command = in.readUnsignedShortLE();

			// 3. O restante dos bytes no ByteBuf é o payload real do pacote.
			byte[] payloadData = new byte[in.readableBytes()];
			in.readBytes(payloadData);

			System.out.println("Pacote recebido no GameHandler: Comando=0x" + Integer.toHexString(command)
					+ ", Sequência=" + sequence + ", Payload size=" + payloadData.length);

			System.out.println(""); // Linha em branco para formatação.
			System.out.println("GS: Comando recebido: 0x" + Integer.toHexString(command) + " (Seq: 0x"
					+ Integer.toHexString(sequence) + ")");

			// Obtém a soma atual dos pacotes enviados para este canal.
			// int currentTxSum = ctx.channel().attr(GameAttributes.PACKET_TX_SUM).get();

			BiConsumer<ChannelHandlerContext, byte[]> reader = OpcodeReaderFactory.getReader(command);
			if (reader != null) {
				System.err.println("GS: Comando Recebido 0x" + Integer.toHexString(command));
				reader.accept(ctx, payloadData);
				// cashUpdate(ctx); //manda att de cash
				// JoinChannel(ctx); //Entra no channel
			} else {
				System.err.println("GS: Comando 0x" + Integer.toHexString(command) + " desconhecido no Game Server.");
			}
			

		} finally {
			// MUITO IMPORTANTE: Liberar o ByteBuf manualmente!
			if (in != null) {
				in.release();
			}
		}
	}

	/**
	 * POR HORA NAO ESTA SENDO USADO... (HA BASTANTE COISAS PRA SE FAZER) Envia uma
	 * resposta de falha para a requisição de verificação de usuário/senha. O opcode
	 * de resposta de falha pode variar.
	 * 
	 * @param ctx          O contexto do canal.
	 * @param currentTxSum A soma atual dos tamanhos dos pacotes enviados.
	 */
	private void sendAuthenticationFailureResponse(ChannelHandlerContext ctx, int currentTxSum) {
		ByteBuf responsePayload = Unpooled.buffer();
		responsePayload.writeByte(0x01); // Código de status (falha)

		ByteBuf failurePacket = PacketUtils.generatePacket(currentTxSum, 0x1011, responsePayload);
		ctx.writeAndFlush(failurePacket);
		responsePayload.release(); // Libere o payload após o uso
		System.out.println("GS: Resposta de falha 0x1011 enviada.");
	}

	/**
	 * Chamado quando a conexão com o cliente de jogo é inativa.
	 * 
	 * @param ctx O contexto do canal.
	 * @throws Exception
	 */
	@Override
	public void channelInactive(ChannelHandlerContext ctx) {
		connectionManager.removeConnection(ctx.channel());
		System.err.println("GS: Cliente de jogo desconectado de " + ctx.channel().remoteAddress());

		PlayerSession ps = PlayerSessionManager.getInstance().getPlayer(ctx.channel());

		PlayerSessionManager.getInstance().removePlayer(ctx.channel());

		// O canal é removido automaticamente -> abaixo pode ser removido explicitamente

		// super.channelInactive(ctx);
		ctx.close(); // Fecha a conexão em caso de erro.

		if (ps != null) {
			GunBoundLobbyManager.getInstance().playerLeaveLobby(ps);
			RoomManager.getInstance().handlePlayerLeave(ps);
			
			if (ps.getNickName() == null) {
				System.err.println("GS: Player desconhecido desconectado");
			} else {
				System.err.println("GS: Player " + ps.getNickName() + " desconectado");
			}
		}
	}

	/**
	 * Chamado quando uma exceção ocorre no pipeline.
	 * 
	 * @param ctx   O contexto do canal.
	 * @param cause A causa da exceção.
	 */
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		System.err.println("GS: Exceção no Game Server para " + ctx.channel().remoteAddress() + ":");
		PlayerSession ps = PlayerSessionManager.getInstance().getPlayer(ctx.channel());
		
		GunBoundLobbyManager.getInstance().playerLeaveLobby(ps);
		RoomManager.getInstance().handlePlayerLeave(ps);
		connectionManager.removeConnection(ctx.channel());

		PlayerSessionManager.getInstance().removePlayer(ctx.channel());

		if (ps.getNickName() == null) {
			System.err.println("GS: Exceção no Game Server para o Player desconhecido");
		} else {
			System.err.println("GS: Exceção no Game Server para o Player " + ps.getNickName());
		}
		cause.printStackTrace();
	
		ctx.close(); // Fecha a conexão em caso de erro.
	}
}