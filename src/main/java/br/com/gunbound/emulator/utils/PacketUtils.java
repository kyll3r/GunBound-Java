package br.com.gunbound.emulator.utils;

import java.nio.charset.StandardCharsets;

import br.com.gunbound.emulator.model.entities.ServerOption;
import br.com.gunbound.emulator.model.entities.game.PlayerSession;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

/**
 * Utilitários para a criação e manipulação de pacotes do GunBound.
 */
public class PacketUtils {

	public static short readShortLEFromByteArray(byte[] bytes) {
		// Cria um ByteBuf a partir do array de bytes
		ByteBuf buffer = Unpooled.wrappedBuffer(bytes);

		// Lê o short no formato Little Endian
		short value = buffer.readShortLE();

		// Retorna o valor lido
		return value;
	}

	/**
	 * Converter um número inteiro em uma sequência de bytes little-endian ou
	 * big-endian.
	 * 
	 * @param inputInteger O inteiro a ser convertido.
	 * @param size         O número de bytes.
	 * @param bigEndian    true para big-endian, false para little-endian (padrão do
	 *                     protocolo).
	 * @return Um ByteBuf contendo os bytes convertidos.
	 */
	public static ByteBuf intToBytes(int inputInteger, int size, boolean bigEndian) {
		ByteBuf buf = Unpooled.buffer(size);
		if (bigEndian) {
			for (int i = size - 1; i >= 0; i--) {
				buf.writeByte((inputInteger >> (i * 8)) & 0xff);
			}
		} else {
			for (int i = 0; i < size; i++) {
				buf.writeByte((inputInteger >> (i * 8)) & 0xff);
			}
		}
		return buf;
	}

	/**
	 * Metodo que Calcula a sequência do pacote do GunBound com base no tamanho
	 * total dos pacotes.
	 * 
	 * @param sumPacketLength A soma dos tamanhos dos pacotes enviados.
	 * @return O valor da sequência calculado.
	 */
	public static int getSequence(int sumPacketLength) {
		// Implementa a lógica de cálculo de sequência do cliente GunBound.
		return (((sumPacketLength * 0x43FD) & 0xFFFF) - 0x53FD) & 0xFFFF;
	}

	/**
	 * Sobrecarga do metodo Gera um pacote incluindo o cabeçalho.
	 * 
	 * @param player O PlayerSession para quem esta pacote está sendo escrito/enviado.
	 * @param command          O comando do pacote.
	 * @param dataBytes        O payload do pacote.
	 * @param rtc              Se o pacote inicia com bytes zerados (RTC)
	 * @return Um ByteBuf representando o pacote completo.
	 */
	public static ByteBuf generatePacket(PlayerSession player, int command, ByteBuf dataBytes, boolean rtc) {
		//Somatória atual do player do contexto
		int currentTxSum = player.getCurrentTxSum();

		if (rtc) {
			// Cria um novo payload que junta o RTC com os dados originais.
			ByteBuf rtcPayload = Unpooled.buffer(2 + dataBytes.readableBytes());
			rtcPayload.writeShortLE(0);// 2 bytes (0)
			rtcPayload.writeBytes(dataBytes);

			// Chama a função original com este novo payload combinado.
			// O método original irá calcular o tamanho e a sequência corretamente.
			return generatePacket(currentTxSum, command, rtcPayload);
		} else {
			//pacote padrao sem RTC
			return generatePacket(currentTxSum, command, dataBytes);
		}
	}

	/**
	 * Gera um pacote incluindo o cabeçalho.
	 * 
	 * @param sentPacketLength A soma do tamanho dos pacotes já enviados na sessão.
	 *                         Use -1 para o primeiro pacote.
	 * @param command          O comando do pacote.
	 * @param dataBytes        O payload do pacote.
	 * @return Um ByteBuf representando o pacote completo.
	 */
	public static ByteBuf generatePacket(int sentPacketLength, int command, ByteBuf dataBytes) {
		int packetExpectedLength = dataBytes.readableBytes() + 6; // 6 bytes para o cabeçalho
		// System.out.println("Tamanho do pacote: " + packetExpectedLength);
		int packetSequence = getSequence(sentPacketLength + packetExpectedLength);

		// O primeiro pacote da conexão tem uma sequência "mágica" fixa.
		if (sentPacketLength == -1) {
			packetSequence = 0xCBEB;
		}

		ByteBuf response = Unpooled.buffer();
		response.writeBytes(intToBytes(packetExpectedLength, 2, false)); // Tamanho (Little-endian)
		response.writeBytes(intToBytes(packetSequence, 2, false)); // Sequência (Little-endian)
		response.writeBytes(intToBytes(command, 2, false)); // Comando (Little-endian)
		response.writeBytes(dataBytes); // Payload
		return response;
	}

	/**
	 * SOBRECARGA do MÉTODO: Gera um pacote COM RTC.
	 * 
	 * @param sentPacketLength A soma do tamanho dos pacotes já enviados na sessão.
	 * @param command          O comando do pacote.
	 * @param dataBytes        O payload que virá DEPOIS do RTC.
	 * @param rtc              O Request Time Code (geralmente 0).
	 * @return Um ByteBuf representando o pacote completo com RTC.
	 */
	/*private static ByteBuf generatePacket(int sentPacketLength, int command, ByteBuf dataBytes, int rtc) {
		// Cria um novo payload que junta o RTC com os dados originais.
		ByteBuf rtcPayload = Unpooled.buffer(2 + dataBytes.readableBytes());
		rtcPayload.writeShortLE(rtc);
		rtcPayload.writeBytes(dataBytes);

		// Chama a função original com este novo payload combinado.
		// O método original irá calcular o tamanho e a sequência corretamente.
		return generatePacket(sentPacketLength, command, rtcPayload);
	}*/

	/**
	 * Util para construir os dados de um servidor individual para a resposta de
	 * diretório.
	 * 
	 * @param entry    O objeto ServerOption.
	 * @param position A posição do servidor na lista.
	 * @return Um ByteBuf contendo os dados serializados do servidor.
	 */
	public static ByteBuf getIndividualServer(ServerOption entry, int position) {
		String extendedDescription = entry.getServerDescription() + "\r\n[" + entry.getServerUtilization() + "/"
				+ entry.getServerCapacity() + "] players online";

		ByteBuf response = Unpooled.buffer();
		response.writeByte(position);
		response.writeByte(0x00); // Bytes desconhecidos
		response.writeByte(0x00);

		byte[] nameBytes = entry.getServerName().getBytes(StandardCharsets.UTF_8);
		response.writeByte(nameBytes.length);
		response.writeBytes(nameBytes);

		byte[] descriptionBytes = extendedDescription.getBytes(StandardCharsets.UTF_8);
		response.writeByte(descriptionBytes.length);
		response.writeBytes(descriptionBytes);

		// Escreve os bytes do endereço IP
		byte[] addressBytes = entry.getServerAddress().getAddress();
		response.writeBytes(addressBytes);

		response.writeBytes(intToBytes(entry.getServerPort(), 2, true)); // Porta (Big-endian)
		response.writeBytes(intToBytes(entry.getServerUtilization(), 2, true)); // Ocupação (Big-endian)
		response.writeBytes(intToBytes(entry.getServerUtilization(), 2, true)); // Ocupação (duplicado)
		response.writeBytes(intToBytes(entry.getServerCapacity(), 2, true)); // Capacidade (Big-endian)
		response.writeByte(entry.isServerEnabled() ? 1 : 0); // Habilitado (1 ou 0)

		return response;
	}
}