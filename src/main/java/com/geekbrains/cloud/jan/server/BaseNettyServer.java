package com.geekbrains.cloud.jan.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

public class BaseNettyServer {

    public BaseNettyServer(ChannelHandler ... handlers) {
        EventLoopGroup auth = new NioEventLoopGroup(1);
        EventLoopGroup worker = new NioEventLoopGroup();
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(auth, worker)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline().addLast(new ObjectDecoder(ClassResolvers.cacheDisabled(null)),
                                    new ObjectEncoder(),
                                    new com.geekbrains.cloud.jan.server.CloudServerHandler()
                            );
                        }
                    });
            ;
            ChannelFuture future = bootstrap.bind(8189).sync();
            // server started!
            future.channel().closeFuture().sync(); // block
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            System.out.println("i am here");
            auth.shutdownGracefully();
            worker.shutdownGracefully();
        }
    }
}
