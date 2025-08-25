package br.com.gunbound.emulator.utils;


import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class TimestampRemaining {

    public static int calcTimestampRemaining(String dataXStr) {
        // Definindo o formato da data fornecida
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        
        // Convertendo a data fornecida para LocalDateTime
        LocalDateTime dataX = LocalDateTime.parse(dataXStr, formatter);
        
        // Obtendo a data e hora atuais do sistema
        LocalDateTime dataAtual = LocalDateTime.now();
        
        // Calculando a diferença entre a data atual e a data de expiração em segundos
        Duration duracao = Duration.between(dataAtual, dataX);
        
        // Retorna o número de segundos restantes (em 4 bytes)
        return (int) duracao.getSeconds();  // Garantindo que caiba em um inteiro de 4 bytes (int)
    }
    
    public static int calcTimestampRemaining(Timestamp expireAt) {
        if (expireAt == null) return 0;
        LocalDateTime dataX = expireAt.toLocalDateTime();
        LocalDateTime dataAtual = LocalDateTime.now();
        Duration duracao = Duration.between(dataAtual, dataX);
        return Math.max(0, (int) duracao.getSeconds()); // negativo se expirado!
    }

    // Função que escreve o timestamp (em segundos) no ByteBuf
    public static ByteBuf rightTimeOnByteBuf(int timestampRestante) {
        // Cria um ByteBuf de 4 bytes (pois int ocupa 4 bytes)
        ByteBuf byteBuf = Unpooled.buffer(4);  // 4 bytes para armazenar o inteiro (timestamp em segundos)
        
        // Escreve o timestamp restante no ByteBuf (em formato de int)
        byteBuf.writeInt(timestampRestante);
        
        return byteBuf;
    }

    // Função para imprimir os bytes do ByteBuf em formato hexadecimal
    public static void printTimeHexadecimal(ByteBuf byteBuf, boolean isLittleEndian) {
        // Reinicia o índice de leitura do ByteBuf para que possamos ler os dados novamente
        byteBuf.resetReaderIndex();
        
        byte[] array = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(array);

        if (isLittleEndian) {
            // Inverte os bytes para imprimir em Little Endian
            for (int i = array.length - 1; i >= 0; i--) {
                System.out.printf("%02X ", array[i]);
            }
        } else {
            // Imprime os bytes em Big Endian (sem alterações)
            for (byte b : array) {
                System.out.printf("%02X ", b);
            }
        }
        System.out.println();
    }

    public static void main(String[] args) {
        // Data de expiração fornecida (exemplo)
        String dataXStr = "2025-07-30 20:35:51";  // Altere conforme necessário
        
        // Calcula o timestamp restante em segundos
        int timestampRestante = calcTimestampRemaining(dataXStr);
        
        // Se a data já expirou
        if (timestampRestante <= 0) {
            System.out.println("A data já expirou.");
            return;
        }

        // Escreve o timestamp restante (em segundos) no ByteBuf
        ByteBuf byteBuf = rightTimeOnByteBuf(timestampRestante);

        // Imprime o timestamp em Big Endian
        System.out.println("Hexadecimal do ByteBuf (Big Endian):");
        printTimeHexadecimal(byteBuf, false);

        // Imprime o timestamp em Little Endian
        System.out.println("Hexadecimal do ByteBuf (Little Endian):");
        printTimeHexadecimal(byteBuf, true);
    }
}