package br.com.gunbound.emulator.utils;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * Um decoder do Netty que lê o tamanho do pacote do cabeçalho e garante que o
 * pacote inteiro seja recebido antes de passá-lo para o próximo handler no
 * pipeline.
 */
public class PacketDecoder extends ByteToMessageDecoder {

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
		// Precisamos de pelo menos 6 bytes para ler o cabeçalho (tamanho, sequência,
		// comando).
		if (in.readableBytes() < 6) {
			return; // Não há bytes suficientes, espere por mais.
		}

		// Marca a posição atual do cursor de leitura para poder "voltar" se não
		// tivermos dados suficientes.
		in.markReaderIndex();

		// Lê o tamanho total do pacote (os primeiros 2 bytes, little-endian).
		int packetSize = in.readUnsignedShortLE();

		// Verifica se temos o pacote completo no buffer.
		if (in.readableBytes() < packetSize - 2) { // O tamanho já foi lido, então subtraímos 2.
			in.resetReaderIndex(); // Volta para a posição marcada e espera por mais dados.
			return;
		}

		// Se chegamos aqui, temos o pacote completo. Lê o restante do payload.
		ByteBuf packet = in.readBytes(packetSize - 2); // Lê o restante dos bytes.
		out.add(packet); // Passa o pacote completo para o próximo handler no pipeline.
	}
}