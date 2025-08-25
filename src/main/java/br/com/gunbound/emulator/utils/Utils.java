package br.com.gunbound.emulator.utils;

import java.nio.ByteBuffer;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Random;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class Utils {

	/**
	 * Metodo auxiliar para converter um ByteBuf em uma string hexadecimal.
	 * 
	 * @param buffer O ByteBuf a ser convertido.
	 * @return Uma string representando o conteúdo hexadecimal do buffer.
	 */
	public static String toHexString(ByteBuf buffer) {
		// Converte o ByteBuf para uma array de bytes
		byte[] bytes = new byte[buffer.readableBytes()];
		buffer.readBytes(bytes);

		// Converte a array de bytes para uma string hexadecimal
		StringBuilder hexString = new StringBuilder();
		for (byte b : bytes) {
			// Formata cada byte como dois caracteres hexadecimais
			hexString.append(String.format("%02X ", b));
		}
		return hexString.toString().trim();
	}

	public static int randomIntNumber() {

		// Cria uma instância de Random
		Random random = new Random();

		// Gera um inteiro aleatório de 32 bits (4 bytes)
		int rndNum = random.nextInt(Integer.MAX_VALUE); // sempre positivo
		;
		System.out.println("ID de 4 bytes gerado: " + rndNum);
		return rndNum;

	}

	public static byte[] fourBytesTokenGet(int token) {
		// Esse metodo será sempre usado quando necessário converter o token de int para
		// byte

		// Cria um buffer de 4 bytes
		ByteBuffer buffer = ByteBuffer.allocate(4);

		buffer.putInt(token);

		// Obtém o array de bytes
		byte[] idBytes = buffer.array();

		System.out.println("ID de 4 bytes recebido: " + token);
		System.out.println("Representação em bytes: " + Arrays.toString(idBytes));

		return idBytes;
	}

	public static String stringDecode(ByteBuf inputBytes) {
		// Cria um StringBuilder para construir a String.
		StringBuilder result = new StringBuilder();

		// Verifica se o ByteBuf é nulo ou não tem bytes legíveis.
		if (inputBytes == null || !inputBytes.isReadable()) {
			return "";
		}

		// Itera sobre os bytes legíveis do ByteBuf.
		while (inputBytes.isReadable()) {
			// Lê o próximo byte.
			byte currentByte = inputBytes.readByte();

			// Verifica se o byte não é o terminador nulo.
			if (currentByte != 0) {
				// Adiciona o caractere correspondente ao resultado.
				result.append((char) currentByte);
			} else {
				// Se encontrar um byte nulo, para de ler e retorna o resultado.
				return result.toString();
			}
		}

		// Retorna o resultado caso não encontre um byte nulo antes do fim.
		return result.toString();
	}

	/**
	 * Converte um array de bytes em uma string hexadecimal. ( TA DUPLIADO DENTRO DO
	 * GunBounDecrypt) preciso refatorar depois
	 */
	public static String bytesToHex(byte[] bytes) {
		if (bytes == null) {
			return "null";
		}
		final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
		char[] hexChars = new char[bytes.length * 2];
		for (int j = 0; j < bytes.length; j++) {
			int v = bytes[j] & 0xFF;
			hexChars[j * 2] = HEX_ARRAY[v >>> 4];
			hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
		}
		return new String(hexChars);
	}

	/**
	 * Redimensiona um array de bytes para um tamanho específico, truncando ou
	 * preenchendo com zeros.
	 * 
	 * @param source O array de bytes de origem.
	 * @param size   O tamanho desejado.
	 * @return Um novo array de bytes com o tamanho especificado.
	 */
	public static byte[] resizeBytes(byte[] source, int size) {
		byte[] resized = new byte[size];
		// Copia os bytes da origem para o novo array.
		// Se a origem for menor, o restante será preenchido com 0s.
		// Se a origem for maior, ela será truncada.
		System.arraycopy(source, 0, resized, 0, Math.min(source.length, size));
		return resized;

	}

	/**
	 * Converte um array de bytes em um ByteBuf de forma otimizada. Esse método cria
	 * uma cópia dos bytes de entrada.
	 *
	 * @param bytes O array de bytes de entrada.
	 * @return Um novo ByteBuf que contém os bytes copiados.
	 */
	public static ByteBuf toByteBuf(byte[] bytes) {
		if (bytes == null) {
			return null;
		}
		// Utiliza Unpooled.wrappedBuffer para evitar a cópia.
		// O ByteBuf criado compartilha o array de bytes subjacente.
		// Isso é ótimo para desempenho, mas exige cuidado com a liberação do buffer.
		return Unpooled.wrappedBuffer(bytes);
	}

	/**
	 * Converte uma String em array de bytes
	 *
	 * @param String Uma sequencia de string de entrada.
	 * @return Um novo array de Byte que contém os bytes copiados.
	 */
	public static byte[] hexStringToByteArray(String s) {
		s = s.replaceAll("\\s+", ""); // remove espaços
		int len = s.length();
		byte[] data = new byte[len / 2];

		for (int i = 0; i < len; i += 2) {
			data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
		}
		return data;
	}
	
	
	public static int randomMobile(int DragonAndKnight) {
        Random random = new Random();

        // Gera um número aleatório entre 0 e 100
        int chance = random.nextInt(100);

        int resultado;

        // se numero gerado for 80 então a chance esta entre 0 e 80, há 80% de chance
        if (chance <= DragonAndKnight) {
            // Gera 17 ou 18
            resultado = 17 + random.nextInt(2); // 17 ou 18
        } else { // 20% de chance
            // Gera um número entre 0 e 13
            resultado = random.nextInt(14); // 0 a 13
        }

        System.out.println("Resultado: " + resultado);
        return resultado;
    }



	public static String msgByHour() {
		LocalTime agora = LocalTime.now();

		if (agora.isBefore(LocalTime.NOON)) {
			return "$Bom dia ";
		} else if (agora.isBefore(LocalTime.of(18, 0))) {
			return "$Boa tarde ";
		} else {
			return "$Boa noite ";
		}
	}

}
