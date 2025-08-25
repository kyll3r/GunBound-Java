package br.com.gunbound.emulator.room.model.enums;

import java.util.HashMap;
import java.util.Map;

/**
 * Enumeração para os Modos de Jogo do GunBound.
 */
public enum GameError {
    SOLO(0x00, "Solo"),
    SCORE(0x44, "Score"),
    TAG(0x08, "Tag"),
    JEWEL(0x0C, "Jewel"),
    UNKNOWN(-1, "Unknown"); // Valor padrão para modos não reconhecidos

    private final int id;
    private final String nameError;

    // Mapa para busca rápida do modo de jogo pelo ID
    private static final Map<Integer, GameError> BY_ID = new HashMap<>();

    // Bloco estático para preencher o mapa quando a classe for carregada
    static {
        for (GameError mode : values()) {
            if (mode != UNKNOWN) {
                BY_ID.put(mode.id, mode);
            }
        }
    }

    GameError(int id, String nameError) {
        this.id = id;
        this.nameError = nameError;
    }

    public int getId() {
        return id;
    }

    public String getNameError() {
        return nameError;
    }

    /**
     * Obtém um GameMode a partir de seu ID numérico.
     *
     * @param id O ID do modo de jogo.
     * @return O GameMode correspondente, ou UNKNOWN se o ID não for encontrado.
     */
    public static GameError fromId(int id) {
        return BY_ID.getOrDefault(id, UNKNOWN);
    }
}