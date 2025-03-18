package org.example.nettydemo.demos.netty.unuse;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.util.concurrent.TimeUnit;

public class MockRadarServer {
    public static void main(String[] args) throws InterruptedException {
        int basePort = 9000; // 基础端口
        int deviceCount = 55; // 需要模拟的设备数量

        for (int i = 0; i < deviceCount; i++) {
            int port = basePort + i;
            new Thread(() -> startServer(port)).start();
        }
    }

    private static void startServer(int port) {
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ch.pipeline().addLast(new MockRadarHandler());
                        }
                    });

            ChannelFuture future = bootstrap.bind(port).sync();
            System.out.println("Mock Radar started on port: " + port);
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}

class MockRadarHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        System.out.println("Client connected: " + ctx.channel().remoteAddress());
        ctx.executor().scheduleAtFixedRate(() -> {
            byte[] radarData = new byte[] {
                0x75, 0x7B, 0x06, 0x03, (byte) 0xEE
            };
            ctx.writeAndFlush(Unpooled.wrappedBuffer(radarData));
        }, 0, 40, TimeUnit.MILLISECONDS);
    }
}

