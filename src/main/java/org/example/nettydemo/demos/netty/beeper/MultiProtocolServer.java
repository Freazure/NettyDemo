package org.example.nettydemo.demos.netty.beeper;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

/**
 * <p>Project: NettyDemo - MultiProtocolServer</p>
 * <p>Powered by szl On 2025-08-01 13:37:18</p>
 * <p>Description: 多协议服务端</p>
 *
 * @author szl
 * @version 1.0
 * @since 1.8
 */
public class MultiProtocolServer {
    private static final Logger logger = LoggerFactory.getLogger(MultiProtocolServer.class);

    private final int port;

    /**
     * -- GETTER --
     *  获取协议管理器
     */
    @Getter
    private final ProtocolManager protocolManager;

    /**
     * -- GETTER --
     *  获取协议路由器
     */
    @Getter
    private final ProtocolRouter protocolRouter;

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private Channel serverChannel;

    public MultiProtocolServer(int port) {
        this.port = port;
        this.protocolManager = new ProtocolManager();
        this.protocolRouter = new ProtocolRouter();

        // 注册默认的协议处理器
        initializeDefaultHandlers();
    }

    /**
     * 初始化默认的协议处理器
     */
    private void initializeDefaultHandlers() {
        // 注册海康呼叫器处理器
        protocolRouter.registerHandler(new HikvisionCallerProtocolHandler());
        protocolRouter.registerHandler(new RadarProtocolHandler());
//        protocolRouter.registerHandler(new HikvisionCallerProtocolHandler(new DevMessageCallback() {
//            @Override
//            public void onConnected(Channel channel) {
//                logger.info("海康呼叫器设备连接成功: {}", channel.remoteAddress());
//            }
//
//            @Override
//            public void onMessageReceived(DevMessage message) {
//                logger.info("收到海康呼叫器消息: {}", message.getHeader().getMsgType());
//                // 这里可以添加具体的业务处理逻辑
//            }
//
//            @Override
//            public void onDisconnected() {
//                logger.info("海康呼叫器设备断开连接");
//            }
//
//            @Override
//            public void onError(Throwable cause) {
//                logger.error("海康呼叫器连接异常", cause);
//            }
//        }));

        // 注册其他处理器
    }

    /**
     * 启动服务器
     */
    public CompletableFuture<Void> start() {
        CompletableFuture<Void> future = new CompletableFuture<>();

        bossGroup = new NioEventLoopGroup(1);
        workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();

                            // 添加协议检测解码器
                            pipeline.addLast("protocolDetection",
                                    new ProtocolDetectionDecoder(protocolManager, protocolRouter));
                        }
                    });

            ChannelFuture channelFuture = bootstrap.bind(port);
            channelFuture.addListener((ChannelFutureListener) bindFuture -> {
                if (bindFuture.isSuccess()) {
                    serverChannel = bindFuture.channel();
                    logger.info("多协议服务器启动成功，端口: {}", port);
                    future.complete(null);
                } else {
                    logger.error("服务器启动失败，端口: {}", port, bindFuture.cause());
                    future.completeExceptionally(bindFuture.cause());
                    shutdown();
                }
            });

        } catch (Exception e) {
            logger.error("启动服务器失败", e);
            future.completeExceptionally(e);
            shutdown();
        }

        return future;
    }

    /**
     * 停止服务器
     */
    public CompletableFuture<Void> stop() {
        CompletableFuture<Void> future = new CompletableFuture<>();

        if (serverChannel != null) {
            serverChannel.close().addListener((ChannelFutureListener) closeFuture -> {
                shutdown();
                future.complete(null);
            });
        } else {
            shutdown();
            future.complete(null);
        }

        return future;
    }

    private void shutdown() {
        if (bossGroup != null) {
            bossGroup.shutdownGracefully();
        }
        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
        }
    }

    /**
     * 注册协议检测器
     */
    public void registerProtocolDetector(ProtocolDetector detector) {
        protocolManager.registerDetector(detector);
    }

    /**
     * 注册协议处理器
     */
    public void registerProtocolHandler(ProtocolHandler handler) {
        protocolRouter.registerHandler(handler);
    }

}
