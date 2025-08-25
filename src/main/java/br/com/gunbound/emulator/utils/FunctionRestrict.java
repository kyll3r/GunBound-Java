package br.com.gunbound.emulator.utils;

/**
 * Classe utilitária para gerenciar as funcionalidades do servidor usando bitmasking.
 * Permite que múltiplas opções (como eventos e efeitos) sejam armazenadas em um único inteiro.
 */
public final class FunctionRestrict {

    // Construtor privado para impedir a instanciação da classe utilitária.
    private FunctionRestrict() {}

    // --- Definição das Flags ---
    // Cada funcionalidade é um bit diferente em um inteiro de 32 bits.
    
    public static final int AVATAR_ENABLED   = 1 << 4;  // 16
    public static final int EFFECT_FORCE     = 1 << 13; // 8192
    public static final int EFFECT_TORNADO   = 1 << 14; // 16384
    public static final int EFFECT_LIGHTNING = 1 << 15; // 32768
    public static final int EFFECT_WIND      = 1 << 16; // 65536
    public static final int EFFECT_THOR      = 1 << 17; // 131072
    public static final int EFFECT_MOON      = 1 << 18; // 262144
    public static final int EFFECT_ECLIPSE   = 1 << 19; // 524288
    public static final int EVENT1_ENABLE    = 1 << 20; // 1048576
    public static final int EVENT2_ENABLE    = 1 << 21; // 2097152
    public static final int EVENT3_ENABLE    = 1 << 22; // 4194304
    public static final int EVENT4_ENABLE    = 1 << 23; // 8388608

    /**
     * Combina múltiplas flags de funcionalidades em um único valor inteiro.
     * * @param effectFlags Um array ou uma sequência de flags a serem ativadas (varargs).
     * @return Um único inteiro representando a combinação de todas as flags ativadas.
     */
    public static int getFunctionValue(int... effectFlags) {
        int resultFunctionOut = 0;
        // O loop 'for-each' itera sobre cada flag passada para o método.
        for (int effectFlag : effectFlags) {
            // A operação OR bit-a-bit (|=) "liga" o bit correspondente na variável de resultado.
            resultFunctionOut |= effectFlag;
        }
        return resultFunctionOut;
    }
}


/*
/ Exemplo de como usar a classe:

//ativar apenas o efeito da Lua
int enabledFunctions = FunctionRestrict.getFunctionValue(FunctionRestrict.EFFECT_MOON);
//O resultado será 262144

//Para ativar Avatares, Thor e um Evento (COMO NO CODIGO javascript)
int enabledFunctionsMultiple = FunctionRestrict.getFunctionValue(
 FunctionRestrict.AVATAR_ENABLED,
 FunctionRestrict.EFFECT_THOR,
 FunctionRestrict.EVENT1_ENABLE
);
//O resultado será a soma dos valores: 16 + 131072 + 1048576 = 1179664
*/