package br.com.gunbound.emulator.utils.crypto;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import br.com.gunbound.emulator.utils.Utils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

/**
 * Classe utilitária para a descriptografia AES e conversão de dados.
 * Thanks Jglim .
 */
public final class GunBoundCipher {

	// --- Chaves e Ciphers Estáticos ---
	private static final byte[] FIXED_KEY = hexStringToBytes("FFB3B3BEAE97AD83B9610E23A43C2EB0");
	private static final SecretKeySpec SECRET_KEY_SPEC = new SecretKeySpec(FIXED_KEY, "AES");
	private static final String ALGORITHM_MODE_PADDING = "AES/ECB/NoPadding";
	private static Cipher staticCipher;

	static {
		try {
			staticCipher = Cipher.getInstance(ALGORITHM_MODE_PADDING);
			staticCipher.init(Cipher.DECRYPT_MODE, SECRET_KEY_SPEC);
		} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException e) {
			throw new ExceptionInInitializerError("Falha ao inicializar o Cipher AES estático: " + e.getMessage());
		}
	}

	// --- Métodos de Encriptação e Descriptografia AES (Base) ---

	/**
	 * Descriptografa um bloco de dados usando uma chave AES fornecida.
	 */
	public static byte[] aesDecryptBlock(byte[] block, byte[] key) throws NoSuchAlgorithmException,
			NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		Cipher cipher = Cipher.getInstance(ALGORITHM_MODE_PADDING);
		cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, "AES"));
		return cipher.doFinal(block);
	}

	/**
	 * Encripta um bloco de dados usando uma chave AES fornecida.
	 */
	public static byte[] aesEncryptBlock(byte[] block, byte[] key) throws NoSuchAlgorithmException,
			NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		Cipher cipher = Cipher.getInstance(ALGORITHM_MODE_PADDING);
		cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, "AES"));
		return cipher.doFinal(block);
	}

	// --- Métodos de Criptografia e Descriptografia do Protocolo ---

	public static byte[] gunboundStaticDecrypt(byte[] block) throws IllegalBlockSizeException, BadPaddingException {
		if (block.length % 16 != 0) {
			throw new IllegalBlockSizeException("O bloco de entrada deve ter um tamanho múltiplo de 16 bytes.");
		}
		return staticCipher.doFinal(block);
	}

	public static byte[] gunboundDynamicDecryptRaw(byte[] blocks, String username, String password, byte[] authToken)
			throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException,
			BadPaddingException, IOException {
		byte[] key = getDynamicKey(username, password, authToken);
		return aesDecryptBlock(blocks, key);
	}

	public static byte[] gunboundDynamicEncryptRaw(byte[] plainBytes, String username, String password,
			byte[] authToken) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException,
			IllegalBlockSizeException, BadPaddingException, IOException {
		byte[] key = getDynamicKey(username, password, authToken);
		return aesEncryptBlock(plainBytes, key);
	}

	public static byte[] gunboundDynamicDecrypt(byte[] blocks, String username, String password, byte[] authToken,
			int command) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException,
			IllegalBlockSizeException, BadPaddingException, IOException {
		int packetCommand = 0x8631607E + command;
		byte[] raw = gunboundDynamicDecryptRaw(blocks, username, password, authToken);
		ByteArrayOutputStream processedStream = new ByteArrayOutputStream();

		int currentBlockCommand = 0;
		for (int i = 0; i < raw.length; i++) {
			int internal128BitIndex = i % 16;
			if (internal128BitIndex < 4) {
				// Constrói o DWORD little-endian a partir de bytes.
				currentBlockCommand |= (raw[i] & 0xFF) << (internal128BitIndex * 8);
				if (internal128BitIndex == 3) {
					if (currentBlockCommand != packetCommand) {
						System.err.println("gunbound_dynamic_decrypt: command checksum mismatch (Expected: "
								+ String.format("0x%08X", packetCommand) + ", Got: "
								+ String.format("0x%08X", currentBlockCommand) + ")");
						
						System.err.println("User:"+ username + " | " + "pw:" + password + " | " + "authToken:" + Utils.bytesToHex(authToken) + " | ");
						// Não retorna aqui para processar o resto do pacote, mas você pode querer
						// desconectar o cliente.
					}
					currentBlockCommand = 0; // Reseta para o próximo bloco.
				}
			} else {
				processedStream.write(raw[i]);
			}
		}
		return processedStream.toByteArray();
	}

	public static byte[] gunboundDynamicEncrypt(byte[] plainBytes, String username, String password, byte[] authToken,
			int command) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException,
			IllegalBlockSizeException, BadPaddingException, IOException {

		if (plainBytes.length % 12 != 0) {
			System.err.println("gunbound_dynamic_encrypt: bytes are not aligned to 12-byte boundary");
			return hexStringToBytes("DEADBEEF");
		}

		int packetCommand = 0x8631607E + command;

		// Converte o comando para bytes little-endian.
		byte[] packetCommandBytes = intToBytesLE(packetCommand, 4);

		ByteArrayOutputStream processedStream = new ByteArrayOutputStream();
		for (int i = 0; i < plainBytes.length; i++) {
			if (i % 12 == 0) {
				processedStream.write(packetCommandBytes);
			}
			processedStream.write(plainBytes[i]);
		}

		byte[] processedBytes = processedStream.toByteArray();
		return gunboundDynamicEncryptRaw(processedBytes, username, password, authToken);
	}

	// --- Lógica de Geração de Chave Dinâmica ---
	private static byte[] getDynamicKey(String username, String password, byte[] authToken) throws IOException {
		byte[] usernameBytes = username.getBytes(StandardCharsets.UTF_8);
		byte[] passwordBytes = password.getBytes(StandardCharsets.UTF_8);

		ByteArrayOutputStream shaStateStream = new ByteArrayOutputStream();
		try {
			shaStateStream.write(usernameBytes);
			shaStateStream.write(passwordBytes);
			shaStateStream.write(authToken);
		} catch (IOException e) {
			throw new RuntimeException("Erro ao concatenar bytes para a chave dinâmica.", e);
		}

		byte[] shaState = shaStateStream.toByteArray();
		int messageBitLength = shaState.length * 8;

		ByteArrayOutputStream paddedBlockStream = new ByteArrayOutputStream();
		paddedBlockStream.write(shaState);
		paddedBlockStream.write(0x80);

		int currentLength = paddedBlockStream.size();
		int numZerosToPad = 62 - currentLength;
		for (int i = 0; i < numZerosToPad; i++) {
			paddedBlockStream.write(0x00);
		}

		paddedBlockStream.write(messageBitLength >> 8);
		paddedBlockStream.write(messageBitLength & 0xFF);

		byte[] paddedBlock = paddedBlockStream.toByteArray();

		byte[] sha0Hash = sha0ProcessBlock(paddedBlock);

		byte[] truncatedKey = Arrays.copyOf(sha0Hash, 16);

		for (int i = 0; i < truncatedKey.length; i += 4) {
			byte b1 = truncatedKey[i];
			byte b2 = truncatedKey[i + 1];
			byte b3 = truncatedKey[i + 2];
			byte b4 = truncatedKey[i + 3];
			truncatedKey[i] = b4;
			truncatedKey[i + 1] = b3;
			truncatedKey[i + 2] = b2;
			truncatedKey[i + 3] = b1;
		}

		return truncatedKey;
	}

	/**
	 * Processa um bloco de 64 bytes usando variante do algoritmo SHA-0.
	 *
	 * @param chunk O bloco de 64 bytes a ser processado.
	 * @return O hash de 20 bytes (160 bits).
	 */
	private static byte[] sha0ProcessBlock(byte[] chunk) {
		// 1. Quebra o chunk de 64 bytes em 16 DWORDs big-endian (W[0] a W[15]).
		int[] w = new int[80];
		ByteBuffer buffer = ByteBuffer.wrap(chunk);
		buffer.order(ByteOrder.BIG_ENDIAN); // O chunk é big-endian, lemos como big-endian
		for (int i = 0; i < 16; i++) {
			w[i] = buffer.getInt();
		}

		// 2. Expande os 16 DWORDs para 80 DWORDs (W[16] a W[79]).
		for (int i = 16; i < 80; i++) {
			// left rotate 0 for SHA-0
			w[i] = leftRotate(w[i - 3] ^ w[i - 8] ^ w[i - 14] ^ w[i - 16], 0);
		}

		// 3. Inicializa o estado do hash.
		int a = 0x67452301;
		int b = 0xEFCDAB89;
		int c = 0x98BADCFE;
		int d = 0x10325476;
		int e = 0xC3D2E1F0;

		// 4. Executa o loop principal de 80 rodadas.
		for (int i = 0; i < 80; i++) {
			int f;
			int k;

			if (0 <= i && i <= 19) {
				f = d ^ (b & (c ^ d)); // Not a standard SHA-1 function, but matches your code.
				k = 0x5A827999;
			} else if (20 <= i && i <= 39) {
				f = b ^ c ^ d;
				k = 0x6ED9EBA1;
			} else if (40 <= i && i <= 59) {
				f = (b & c) | (b & d) | (c & d);
				k = 0x8F1BBCDC;
			} else { // 60 <= i <= 79
				f = b ^ c ^ d;
				k = 0xCA62C1D6;
			}

			int temp = leftRotate(a, 5) + f + e + k + w[i];
			e = d;
			d = c;
			c = leftRotate(b, 30);
			b = a;
			a = temp; // No mask needed here, as Java's int will wrap around implicitly.
		}

		// 5. Atualiza o estado do hash com o resultado do loop.
		a = (a + 0x67452301);
		b = (b + 0xEFCDAB89);
		c = (c + 0x98BADCFE);
		d = (d + 0x10325476);
		e = (e + 0xC3D2E1F0);

		// 6. Formata o resultado como um array de bytes little-endian e truncado para 16 bytes.
		// O código Python retorna 16 bytes. O estado do hash tem 5 DWORDs (20 bytes).
		// Ele pega os primeiros 4 DWORDs (a, b, c, d) e os converte para bytes
		// little-endian.

		byte[] result = new byte[20]; // SHA-0 standard output is 20 bytes
		ByteBuffer resultBuffer = ByteBuffer.wrap(result);

		resultBuffer.putInt(a);
		resultBuffer.putInt(b);
		resultBuffer.putInt(c);
		resultBuffer.putInt(d);
		resultBuffer.putInt(e);

		return result; // The logic after this method will truncate to 16 bytes.
	}

	/**
	 * Realiza uma rotação bit a bit para a esquerda em um inteiro de 32 bits
	 * (DWORD).
	 *
	 * @param n O número a ser rotacionado.
	 * @param b O número de bits para rotacionar.
	 * @return O resultado da rotação.
	 */
	private static int leftRotate(int n, int b) {
		return (n << b) | (n >>> (32 - b));
	}

	public static byte[] gunboundDynamicDecrypt(byte[] blocks, String username, String password, byte[] authToken)
			throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException,
			BadPaddingException, IOException {

		byte[] dynamicKey = getDynamicKey(username, password, authToken);
		SecretKeySpec dynamicKeySpec = new SecretKeySpec(dynamicKey, "AES");

		Cipher dynamicCipher = Cipher.getInstance(ALGORITHM_MODE_PADDING);
		dynamicCipher.init(Cipher.DECRYPT_MODE, dynamicKeySpec);

		return dynamicCipher.doFinal(blocks);
	}

	// --- Métodos de Conversão Utilitários ---
	private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

	public static String bytesToHex(byte[] bytes) {
		if (bytes == null) {
			return "null";
		}
		char[] hexChars = new char[bytes.length * 2];
		for (int j = 0; j < bytes.length; j++) {
			int v = bytes[j] & 0xFF;
			hexChars[j * 2] = HEX_ARRAY[v >>> 4];
			hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
		}
		return new String(hexChars);
	}

	public static byte[] intToBytesLE(int inputInteger, int size) {
		if (size <= 0 || size > 4) {
			throw new IllegalArgumentException("O tamanho deve ser entre 1 e 4 bytes para um int.");
		}
		byte[] outputBytes = new byte[size];
		int tempInteger = inputInteger;
		for (int i = 0; i < size; i++) {
			outputBytes[i] = (byte) (tempInteger & 0xFF);
			tempInteger = tempInteger >> 8;
		}
		return outputBytes;
	}
	
    public static ByteBuf intToByteBufLE(int inputInteger, int size) {
        if (size <= 0 || size > 4) {
            throw new IllegalArgumentException("O tamanho deve ser entre 1 e 4 bytes para um int.");
        }
        
        // Usa o alocador global de buffers para criar um novo ByteBuf.
        ByteBuf buf = Unpooled.buffer(size); 
        
        for (int i = 0; i < size; i++) {
            buf.writeByte((byte) (inputInteger & 0xFF));
            inputInteger = inputInteger >> 8;
        }
        return buf;
    }

	private static byte[] hexStringToBytes(String s) {
		int len = s.length();
		byte[] data = new byte[len / 2];
		for (int i = 0; i < len; i += 2) {
			data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
		}
		return data;
	}

	private GunBoundCipher() {//construtor privado (boa pratica)
	}
}