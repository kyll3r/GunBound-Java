package br.com.gunbound.emulator.packets.readers.room;

import java.util.concurrent.TimeUnit;

import br.com.gunbound.emulator.handlers.GameAttributes;
import br.com.gunbound.emulator.model.entities.game.PlayerSession;
import br.com.gunbound.emulator.room.GameRoom;
import br.com.gunbound.emulator.utils.PacketUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;

public class RoomTunnelReader {

    private static final int OPCODE_REQUEST = 0x4500;
    private static final int OPCODE_FORWARD = 0x4501;
    private static final int MAX_RETRIES = 3; // Número máximo de tentativas de reenvio
    private static final long RETRY_DELAY = 100; // Tempo de espera entre tentativas (em milissegundos)

    public static void read(ChannelHandlerContext ctx, byte[] payload) {
        System.out.println("RECV> SVC_ROOM_TUNNEL (0x" + Integer.toHexString(OPCODE_REQUEST) + ")");

        PlayerSession senderPlayer = ctx.channel().attr(GameAttributes.USER_SESSION).get();

        if (senderPlayer == null || senderPlayer.getCurrentRoom() == null)
            return;
        GameRoom room = senderPlayer.getCurrentRoom();

        // Empacota toda a lógica em um Runnable e submete para a fila da sala!
       // room.submitAction(() -> {
            //synchronized (room) {
        	ctx.channel().eventLoop().schedule(() -> {
                processTunnel(ctx, payload, senderPlayer, room);
        	}, 100, java.util.concurrent.TimeUnit.MILLISECONDS);
            //}
        //}, ctx);
    }

    private static void processTunnel(ChannelHandlerContext ctx, byte[] payload, PlayerSession senderPlayer,
                                      GameRoom room) {
        ByteBuf request = Unpooled.wrappedBuffer(payload);
        try {
            // Skip opcode bytes
            request.skipBytes(2);

            if (request.readableBytes() < 1)
                return;

            int destinationSlot = request.readUnsignedByte();

            if (request.readableBytes() <= 0) {
                System.err.println("TUNNEL: Pacote recebido sem dados de jogo após o slot de destino.");
                return;
            }

            // Cria um novo byte[] apenas com os dados do jogo para o forward
            byte[] dataBytes = new byte[request.readableBytes()];
            request.readBytes(dataBytes);

            PlayerSession destinationPlayer = room.getPlayersBySlot().get(destinationSlot);
            if (destinationPlayer != null) {
                System.out.println("TUNNEL: enviando pacote de: " + senderPlayer.getNickName() +
                                   " SLOT [" + senderPlayer.getCurrentRoom().getSlotPlayer(senderPlayer) + "]" +
                                   " para: " + destinationPlayer.getNickName() + " SLOT [" + destinationSlot + "]");

                // Sempre passe o array puro, nunca o ByteBuf
                
                
                // Empacota toda a lógica em um Runnable e submete para a fila da sala!
                 room.submitAction(() -> {
                     synchronized (room) {
                forwardPacketToPlayer(room, senderPlayer, destinationPlayer, dataBytes, 0);
                     }
                 }, destinationPlayer.getPlayerCtx());

            } else {
                System.err.println("TUNNEL: slot de destino inválido: " + destinationSlot);
            }
        } catch (Exception e) {
            System.err.println("Erro ao processar pacote de túnel (0x4500):");
            e.printStackTrace();
        } finally {
            request.release();
        }
    }

    // Retry seguro: a cada chamada um novo ByteBuf é criado a partir de byte[]
    private static void forwardPacketToPlayer(GameRoom room, PlayerSession sender, PlayerSession recipient,
                                              byte[] data, int retryCount) {

        // Cria o payload do forward
        ByteBuf forwardPayload = recipient.getPlayerCtxChannel().alloc().buffer();
        try {
            forwardPayload.writeByte(room.getSlotPlayer(sender)); // Origem
            forwardPayload.writeBytes(data);                      // Dados do jogo

            ByteBuf forwardPacket = PacketUtils.generatePacket(recipient, OPCODE_FORWARD, forwardPayload, false);

            // Não libere forwardPacket -- Netty se encarrega via writeAndFlush
            recipient.getPlayerCtxChannel().writeAndFlush(forwardPacket).addListener((ChannelFutureListener) future -> {
                if (!future.isSuccess()) {
                    System.err.println("FALHA AO ENVIAR PACOTE DE TÚNEL para: " + recipient.getNickName());
                    future.cause().printStackTrace();

                    // Verifica se ainda há tentativas disponíveis
                    if (retryCount < MAX_RETRIES) {
                        System.out.println("Tentativa de reenvio: " + (retryCount + 1) + " para " + recipient.getNickName());
                        recipient.getPlayerCtxChannel().eventLoop().schedule(
                                () -> forwardPacketToPlayer(room, sender, recipient, data, retryCount + 1),
                                RETRY_DELAY, TimeUnit.MILLISECONDS);
                    } else {
                        System.out.println("Tentativas esgotadas, fechando conexão com: " + recipient.getNickName());
                        recipient.getPlayerCtxChannel().close();
                    }
                }
            });
        } finally {
            forwardPayload.release();
        }
    }
}
