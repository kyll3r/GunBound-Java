package br.com.gunbound.emulator;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;

/**
 * Handler usado para rastrear a soma dos tamanhos dos pacotes enviados por um canal.
 * Isso é essencial para o cálculo da sequência de pacotes no GunBound.
 */
public class PacketSizeTracker extends ChannelOutboundHandlerAdapter {

    // A mesma chave de atributo que será usada no GameHandler e BrokerHandler
    public static final AttributeKey<Integer> PACKET_TX_SUM = AttributeKey.valueOf("packetTxSum");

    /**
     * Intercepta a operação de escrita de um pacote.
     * @param ctx O contexto do handler.
     * @param msg A mensagem (pacote) a ser enviada.
     * @param promise A promessa de conclusão da operação de escrita.
     */
    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
        if (msg instanceof ByteBuf) {
            ByteBuf packet = (ByteBuf) msg;
            int packetSize = packet.readableBytes();

            Attribute<Integer> sumAttribute = ctx.channel().attr(PACKET_TX_SUM);
            
            // Loop atômico para garantir a atualização correta
            while (true) {
                Integer currentSum = sumAttribute.get();
                if (currentSum == null) {
                    currentSum = 0;
                }
                int newSum = currentSum + packetSize;
                if (sumAttribute.compareAndSet(currentSum, newSum)) {
                    // Sucesso, a atualização foi atômica.
                    break;
                }
                // Se falhou, outro thread modificou o valor. O loop tenta novamente.
            }
        }

        // Passa a mensagem para o próximo handler no pipeline de saída.
        ctx.write(msg, promise);
    }
}