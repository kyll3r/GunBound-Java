package br.com.gunbound.emulator.handlers;

import br.com.gunbound.emulator.model.entities.game.PlayerSession;
import io.netty.util.AttributeKey;

public class GameAttributes {
	//SOMA DOS PACOTES DE CADA SESSAO (SEM ISSO NAO SOMOS NADA)
	public static final AttributeKey<Integer> PACKET_TX_SUM = AttributeKey.valueOf("packetTxSum");
	
	//Atributos do Usuario
	//USUARIO DA SESSAO
	public static final AttributeKey<PlayerSession> USER_SESSION = AttributeKey.valueOf("userSession");
	//AUTHTOKEN DWORD (NO CLIENTE) ESSENCIAL PARA CRIPTOGRAFIA
	public static final AttributeKey<byte[]> AUTH_TOKEN = AttributeKey.valueOf("authToken");
	//gera um valor unico pra sessao
	public static final AttributeKey<byte[]> SESSION_UNIQUE = AttributeKey.valueOf("sessionUnique");
	//guarda a versao do Cliente que o player ta usando
	public static final AttributeKey<Integer> CLIENT_VERSION = AttributeKey.valueOf("clientVersion");
	
	
	
	
	
	//Game atributes
	
	public static AttributeKey<Integer> DRAGON_N_KINGHT_RATIO = AttributeKey.valueOf("DRAGON_N_KINGHT_RATIO");
	
	
	//Gerenciador dos lobbys (canais no gunbound)
	//public static final AttributeKey<GunBoundLobbyManager> LOBBY_MANAGER = AttributeKey.valueOf("LobbyManager");
}
