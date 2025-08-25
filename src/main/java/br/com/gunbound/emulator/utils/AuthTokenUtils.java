package br.com.gunbound.emulator.utils;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

/**
 * Classe utilitária para gerar e manipular tokens de autenticação.
 * Otimizada para o contexto do Netty.
 */
public final class AuthTokenUtils {

    /**
     * Gera um token de 4 bytes a partir de um inteiro positivo.
     *
     * @param allocator O alocador de buffers do canal.
     * @param positiveToken O token (inteiro positivo de 4 bytes) a ser convertido.
     * @return Um ByteBuf contendo o token de 4 bytes.
     */
    public static ByteBuf generateAuthToken(ByteBufAllocator allocator, int positiveToken) {
        // Aloca um ByteBuf de 4 bytes.
        ByteBuf tokenBuf = allocator.buffer(4);

        // Escreve o inteiro de 4 bytes no buffer (little-endian).
        tokenBuf.writeIntLE(positiveToken);

        return tokenBuf;
    }

    // Construtor privado para evitar a criação de instâncias da classe utilitária.
    private AuthTokenUtils() {}
}