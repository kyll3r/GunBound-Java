package br.com.gunbound.emulator;

import br.com.gunbound.emulator.handlers.GunBoundGameHandler;
import br.com.gunbound.emulator.utils.PacketDecoder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.MultiThreadIoEventLoopGroup;
import io.netty.channel.nio.NioIoHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

public class GunBoundGameServer {

    private final int port;

    public GunBoundGameServer(int port) {
        this.port = port;
    }

    public void start() throws Exception {
        // Crie os EventLoopGroups para o servidor de jogo
        EventLoopGroup bossGroup = new MultiThreadIoEventLoopGroup(1, NioIoHandler.newFactory());
        EventLoopGroup workerGroup = new MultiThreadIoEventLoopGroup(0, NioIoHandler.newFactory());
        


        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) {
                            ch.pipeline().addLast(
                                    // Adicione um LoggingHandler para o servidor de jogo também
                                    new LoggingHandler("GameServerLogger", LogLevel.DEBUG),
                                    // - Decoder de pacotes de jogo                
                                    new PacketDecoder(),
                                    // 3. Handler para rastrear o tamanho dos pacotes de saída
                                    new PacketSizeTracker(), 
                                    // - Handler de lógica de jogo (chat, salas etc.)
                                    new GunBoundGameHandler() // Uma nova classe de handler de jogo
                            );
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            System.out.println("Game Server bound on port " + port);

            // Inicie o servidor de jogo em um novo Thread
            ChannelFuture f = b.bind(port).sync();
            
            // Não bloqueie o thread principal aqui, apenas retorne o futuro do canal.
            // A classe Master irá gerenciar o encerramento.
            f.channel().closeFuture().addListener(future -> {
                workerGroup.shutdownGracefully();
                bossGroup.shutdownGracefully();
            });
            
        } catch (Exception e) {
            System.err.println("Erro ao iniciar o servidor de jogo: " + e.getMessage());
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
            throw e;
        }
    }
}