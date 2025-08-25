package br.com.gunbound.emulator.model.entities.game;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.netty.channel.Channel;
import io.netty.channel.ChannelId;

public final class PlayerSessionManager {

    private static final PlayerSessionManager INSTANCE = new PlayerSessionManager();

    // Índice por ChannelId para acessos rápidos por conexão
    private final Map<ChannelId, PlayerSession> byNettyChannelId = new ConcurrentHashMap<>();

    // Índice por nickname (lowercase) para buscas diretas
    private final Map<String, PlayerSession> byNickname = new ConcurrentHashMap<>();

    private PlayerSessionManager() {}

    public static PlayerSessionManager getInstance() {
        return INSTANCE;
    }

    public void addPlayer(PlayerSession playerSession) {
        Channel channel = playerSession.getPlayerCtx(); // nome do campo ctx mantido
        String nick = playerSession.getNickName().toLowerCase();

        byNettyChannelId.put(channel.id(), playerSession);
        byNickname.put(nick, playerSession);

        System.out.println("Session Manager: Jogador " + playerSession.getNickName() +
                " conectado. Total de jogadores online: " + byNettyChannelId.size());
    }

    public void removePlayer(Channel channel) {
        PlayerSession removed = byNettyChannelId.remove(channel.id());

        if (removed != null) {
            byNickname.remove(removed.getNickName().toLowerCase());
            System.out.println("Session Manager: Jogador removido. Total de jogadores online: " + byNettyChannelId.size());
        }
    }

    public PlayerSession getPlayer(Channel channel) {
        return byNettyChannelId.get(channel.id());
    }

    public PlayerSession getSessionPlayerByNickname(String nickName) {
        return byNickname.get(nickName.toLowerCase());
    }

    public Collection<PlayerSession> getAllPlayers() {
        return byNettyChannelId.values();
    }

    public int getActivePlayerCount() {
        return byNettyChannelId.size();
    }

    public boolean isPlayerOnline(String nickName) {
        return byNickname.containsKey(nickName.toLowerCase());
    }
}
