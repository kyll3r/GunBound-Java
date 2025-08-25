package br.com.gunbound.emulator.room.model.enums;

import java.util.HashMap;
import java.util.Map;

/**
 * Enumeração para os Modos de Jogo do GunBound.
 */
public enum GameMode {
    SOLO(0x00, "Solo"),
    SCORE(0x44, "Score"),
    TAG(0x08, "Tag"),
    JEWEL(0x0C, "Jewel"),
    UNKNOWN(-1, "Unknown"); // Valor padrão para modos não reconhecidos

    private final int id;
    private final String name;

    // Mapa para busca rápida do modo de jogo pelo ID
    private static final Map<Integer, GameMode> BY_ID = new HashMap<>();

    // Bloco estático para preencher o mapa quando a classe for carregada
    static {
        for (GameMode mode : values()) {
            if (mode != UNKNOWN) {
                BY_ID.put(mode.id, mode);
            }
        }
    }

    GameMode(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    /**
     * Obtém um GameMode a partir de seu ID numérico.
     *
     * @param id O ID do modo de jogo.
     * @return O GameMode correspondente, ou UNKNOWN se o ID não for encontrado.
     */
    public static GameMode fromId(int id) {
        return BY_ID.getOrDefault(id, UNKNOWN);
    }
}