package br.com.gunbound.emulator.packets.readers.lobby;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

import br.com.gunbound.emulator.handlers.GameAttributes;
import br.com.gunbound.emulator.lobby.GunBoundLobby;
import br.com.gunbound.emulator.lobby.GunBoundLobbyManager;
import br.com.gunbound.emulator.model.entities.game.PlayerSession;
import br.com.gunbound.emulator.packets.readers.CashUpdateReader;
import br.com.gunbound.emulator.packets.writers.model.JoinChannelSuccessPacket;
import br.com.gunbound.emulator.room.RoomManager;
import br.com.gunbound.emulator.utils.PacketUtils;
import br.com.gunbound.emulator.utils.Utils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;

public class LobbyJoin {

	// Pega instancia do LobbyManager
	private static GunBoundLobbyManager gbLobbyManager = GunBoundLobbyManager.getInstance();

	private static final int OPCODE_REQUEST = 0x2000;
	private static final int DEFAULT_LOBBY_ID = 7;
	private static final int LOBBY_ALT_ID = 2;

	public static void read(ChannelHandlerContext ctx, byte[] payload) {
		System.out.println("RECV> SVC_JOIN_CHANNEL (0x" + Integer.toHexString(OPCODE_REQUEST) + ")");

		// --- LÓGICA DE DISTRIBUIÇÃO EM AÇÃO ---
	    // 1. Pede ao LobbyManager para encontrar o melhor canal.
	    int bestChannelId = GunBoundLobbyManager.getInstance().findBestChannelForNewPlayer();
		Integer channelId = 7; // por padrao setado pro lobby 8 (1 no cliente)
		Integer channelIdEntered = -1; 

		// se o payload ta zerado é pq ele veio pelo loginWriter (World List) entao
		// precisamos
		// definir um channel
		if (payload == null) {
			//JoinGameLobby(ctx, DEFAULT_LOBBY_ID);
			JoinGameLobby(ctx, bestChannelId);
		} else {
			// pegar o canal solicitado
			channelId = (int) PacketUtils.readShortLEFromByteArray(payload);// testando

			// se for 0xFFFF (-1) seta lobby 3 (2 no array)
			// channelId = channelId == -1 ? 2 : channelId;
			// JoinGameLobby(ctx, channelId);

			if (channelId == -1) {
				PlayerSession ps = ctx.channel().attr(GameAttributes.USER_SESSION).get();

				// remove possiveis duplicidades (PORQUE ao entrar no shop nao sai do channel)
				gbLobbyManager.playerLeaveLobby(ps);

				
				//se saiu do room precisa tirar o camarada de lá
				RoomManager roomManager = RoomManager.getInstance();

				if (roomManager.isPlayerInAnyRoom(ps)) {
					roomManager.handlePlayerLeave(ps);
				}

				//JoinGameLobby(ctx, LOBBY_ALT_ID); // se o saiu do avatar shop ou de um room
				//channelIdEntered = LOBBY_ALT_ID;
				JoinGameLobby(ctx, bestChannelId); // se o saiu do avatar shop ou de um room
				channelIdEntered = bestChannelId;
			} else {
				JoinGameLobby(ctx, channelId); // se o player solicitou um lobby pelo botao do lobby
				channelIdEntered = channelId;
			}

		}

		System.out.println(
				"[DEBUG][JoinLobby.read] IdLobby Solicitado: " + channelId + "IdLobby fornecido: " + channelIdEntered);
	}

	// metodo vai ficar privado para ser chamado apenas pelo read
	private static void JoinGameLobby(ChannelHandlerContext ctx, int channelId) {
		System.out.println("RECV> SVC_JOIN_CHANNEL PT2");

		PlayerSession player = ctx.channel().attr(GameAttributes.USER_SESSION).get();
		if (player == null) {
			System.err.println("JoinChannel: PlayerSession não encontrada no contexto do canal.");
			ctx.close();
			return;
		}

		//int currentTxSum = ctx.channel().attr(GameAttributes.PACKET_TX_SUM).get();

		// adicionado em 15/07 - BUG se o player [1] ia pra um channel com o index [0]
		// no channel antigo dele deletava a posicao 0
		// ou seja deletava o player acima dele no channel antigo (Foi foda pra perceber
		// isso)
		// if (player.getCurrentLobby() != null) {
		// player.getCurrentLobby().removePlayerFromLobby(player);
		// }

		// nao apagar isso aqui...
		gbLobbyManager.playerLeaveLobby(player);

		// 1. Usa o Optional para encontrar a maior posição usando a classe
		// findHighestChannelPosition
		//int highestPosition = gbLobbyManager.getLobbyById(channelId)
				//.map(GunBoundLobbyChannel::findHighestChannelPosition).orElse(-1);

		// 2. A nova posição será a maior posição + 1
		//int newPosition = highestPosition + 1;
		
	    // Pega a instância do canal
	    Optional<GunBoundLobby> lobbyOpt = gbLobbyManager.getLobbyById(channelId);
	    if (lobbyOpt.isEmpty()) {
	        System.err.println("Erro: Canal " + channelId + " não existe.");
	        // Envie um pacote de erro para o cliente aqui
	        return;
	    }
	    GunBoundLobby lobby = lobbyOpt.get();

	    // Tenta pegar um slot vago
	    Integer newPosition = lobby.takeSlot();

	    if (newPosition == null) {
	        System.out.println("GS: O lobby está cheio. O jogador " + player.getNickName() + " não pode entrar.");
	        // Envie um pacote de "lobby cheio" para o cliente aqui
	        return;
	    }

		player.setChannelPosition(newPosition);
		System.out.println("[DEBUG] Posição do jogador está entrando (" + player.getNickName() + "): ["
				+ player.getChannelPosition() + "]");

		// Adiciona o jogador ao lobby apropriado
		gbLobbyManager.playerJoinLobby(player, channelId);


		//isso aqui foi inserido porque ao fechar a sala nao dava tempo da collection carregar
		ctx.channel().eventLoop().schedule(() -> {
			// Adiciona Delay proposital para sincronizar o result
			putPlayerAtLobby(channelId, player, newPosition);
		}, 150, java.util.concurrent.TimeUnit.MILLISECONDS);
		

		// *********************Após entrar no canal, é necessário atualizar o Cash*********************
		CashUpdateReader.read(ctx, null);
		//*********************************************************************************************
	}

	//Metodo criado para criar um delay para carregar a lista de player
	private static void putPlayerAtLobby(int channelId, PlayerSession player,
			Integer newPosition) {
		// Busca a lista COMPLETA de jogadores que estão no mesmo lobby.
		// O jogador que acabou de entrar já estará incluído nesta lista.

		Collection<PlayerSession> playersInLobby = gbLobbyManager.getPlayersInLobby(channelId);
		// O construtor do pacote espera uma ArrayList, então fazemos a conversão.
		ArrayList<PlayerSession> usersInLobby = new ArrayList<>(playersInLobby);

		String clientVersion = String.valueOf(player.getPlayerCtxChannel().attr(GameAttributes.CLIENT_VERSION).get());
		String motd = "#GunBound Legacy Thor's Hammer"; // Mensagem do dia (pode ser configurável)

		// Cria o objeto do pacote com os dados
		JoinChannelSuccessPacket joinPacketData = new JoinChannelSuccessPacket(channelId, newPosition, 
				usersInLobby, motd, clientVersion, player.getNickName());

		// Usa o PacketWriter para construir o payload
		ByteBuf joinChannelPayload = writeJoinChannelSuccess(joinPacketData);

		// Gera o pacote final com o cabeçalho correto
		ByteBuf finalPacket = PacketUtils.generatePacket(player, JoinChannelSuccessPacket.getOpcode(),
				joinChannelPayload,false);

		// Envia o pacote para o cliente
		player.getPlayerCtxChannel().writeAndFlush(finalPacket);

		System.out.println("GS: Pacote de entrada no canal (0x2001) enviado para " + player.getNickName());
		//return joinChannelPayload;
	}

	/**
	 * Constrói o payload do pacote de sucesso ao entrar no canal (opcode 0x2001).
	 * **Versão corrigida para corresponder à implementação de referência (jglim).**
	 * 
	 * @param packetData O objeto contendo todos os dados necessários para o pacote.
	 * @return um ByteBuf com o payload pronto para ser enviado.
	 */
	private static ByteBuf writeJoinChannelSuccess(JoinChannelSuccessPacket packetData) {
		ByteBuf buffer = Unpooled.buffer();

		// 1. Cabeçalho do Payload
		// Conforme a referência, começa com 2 bytes nulos, seguidos pelo ID do canal.
		buffer.writeBytes(new byte[] { 0x00, 0x00 });

		buffer.writeShortLE(packetData.getDesiredChannelId()); // tem que ser -1 aqui pq aqui o lobby 1 é 0 no array do
																// cliente
		// 2. Informações de Ocupação do Canal (1 byte cada)
		buffer.writeByte(packetData.getHighestChannelPosition());
		System.out.println("DEBUG: size.channel " + packetData.getActiveChannelUsers().size());
		buffer.writeByte(packetData.getActiveChannelUsers().size());

		// 3. Escreve o bloco de dados resumido para cada jogador
		Collection<PlayerSession> recipients = new ArrayList<>(packetData.getActiveChannelUsers());
		for (PlayerSession player : recipients) {
			// Posição do jogador no canal (slot) - 1 BYTE
			buffer.writeByte(player.getChannelPosition());

			buffer.writeBytes(Utils.resizeBytes(player.getNickName().getBytes(StandardCharsets.ISO_8859_1), 12));
			buffer.writeByte(player.getGender());
			//buffer.writeBytes(new byte[] { 0x00 });

			// Guild (8 bytes, com padding)
			buffer.writeBytes(Utils.resizeBytes(player.getGuild().getBytes(StandardCharsets.ISO_8859_1), 8));

			// Rank Atual (2 bytes) e Rank da Temporada (2 bytes)
			buffer.writeShortLE(player.getRankCurrent());
			buffer.writeShortLE(player.getRankSeason());
		}

		// 4. MOTD (Message of the Day) do Canal
		String motd = packetData.getExtendedChannelMotd();
		byte[] motdBytes = motd.getBytes(StandardCharsets.ISO_8859_1);

		// Escreve os bytes da MOTD DIRETAMENTE, SEM um prefixo de tamanho.
		buffer.writeBytes(motdBytes);

		return buffer;
	}
}
