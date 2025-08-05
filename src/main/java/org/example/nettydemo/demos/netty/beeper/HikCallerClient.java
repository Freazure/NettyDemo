package org.example.nettydemo.demos.netty.beeper;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.AttributeKey;
import org.example.nettydemo.demos.netty.client.IDeviceClient;
import org.example.nettydemo.demos.netty.handler.HikCallerProtocolHandler;
import org.example.nettydemo.demos.netty.handler.ProtocolDispatchHandler;
import org.example.nettydemo.demos.netty.handler.ProtocolRouter;
import org.example.nettydemo.demos.netty.message.DeviceInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * <p>Project: NettyDemo - HikvisionCallerClient</p>
 * <p>Powered by szl On 2025-08-04 10:18:38</p>
 * <p>Description: 海康呼叫器客户端</p>
 *
 * @author szl
 * @version 1.0
 * @since 1.8
 */
public class HikCallerClient implements IDeviceClient {

    private final DeviceInfo deviceInfo;

    private static final Logger logger = LoggerFactory.getLogger(HikCallerClient.class);

    private EventLoopGroup group;

    private Channel channel;

    public static final AttributeKey<String> DEVICE_ID_KEY = AttributeKey.valueOf("DEVICE_ID_KEY");

    public HikCallerClient(DeviceInfo deviceInfo) {
        this.deviceInfo = deviceInfo;
    }


    @Override
    public CompletableFuture<Void> connect() {
        CompletableFuture<Void> future = new CompletableFuture<>();

        group = new NioEventLoopGroup();

        ProtocolRouter protocolRouter = new ProtocolRouter();
        protocolRouter.registerHandler(new HikCallerProtocolHandler());

        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            // 为每个连接生成唯一的设备ID（临时）
                            String tempDeviceId = "DEVICE_" + deviceInfo.getDeviceId();
                            ch.attr(DEVICE_ID_KEY).set(tempDeviceId);

                            ChannelPipeline pipeline = ch.pipeline();

                            // 添加编解码器
                            pipeline.addLast("decoder", new HikCallerDecoder());
                            pipeline.addLast("encoder", new HikCallerEncoder());

                            // 添加业务处理器
                            pipeline.addLast("handler", new ProtocolDispatchHandler(protocolRouter, deviceInfo.getProtocolType()));
                        }
                    });

            ChannelFuture channelFuture = bootstrap.connect(deviceInfo.getHost(), deviceInfo.getPort());
            channelFuture.addListener((ChannelFutureListener) connectFuture -> {
                if (connectFuture.isSuccess()) {
                    channel = connectFuture.channel();
                    logger.info("成功连接到设备 {}:{}", deviceInfo.getHost(), deviceInfo.getPort());
                    future.complete(null);
                } else {
                    logger.error("连接设备失败 {}:{}", deviceInfo.getHost(), deviceInfo.getPort(), connectFuture.cause());
                    future.completeExceptionally(connectFuture.cause());
                    group.shutdownGracefully();
                }
            });

        } catch (Exception e) {
            logger.error("启动客户端失败", e);
            future.completeExceptionally(e);
            if (group != null) {
                group.shutdownGracefully();
            }
        }

        return future;
    }

    @Override
    public CompletableFuture<Void> disconnect() {
        CompletableFuture<Void> future = new CompletableFuture<>();

        if (channel != null && channel.isActive()) {
            channel.close().addListener((ChannelFutureListener) closeFuture -> {
                if (group != null) {
                    group.shutdownGracefully(0, 5, TimeUnit.SECONDS).addListener(shutdownFuture -> {
                        logger.info("客户端已关闭");
                        future.complete(null);
                    });
                } else {
                    future.complete(null);
                }
            });
        } else {
            if (group != null) {
                group.shutdownGracefully(0, 5, TimeUnit.SECONDS).addListener(shutdownFuture -> {
                    logger.info("客户端已关闭");
                    future.complete(null);
                });
            } else {
                future.complete(null);
            }
        }

        return future;
    }

    @Override
    public CompletableFuture<Void> sendMessage(Object message) {
        if (channel == null || !channel.isActive()) {
            CompletableFuture<Void> future = new CompletableFuture<>();
            future.completeExceptionally(new IllegalStateException("连接未建立或已断开"));
            return future;
        }

        CompletableFuture<Void> future = new CompletableFuture<>();

        ChannelFuture channelFuture = channel.writeAndFlush(message);
        channelFuture.addListener((ChannelFutureListener) writeFuture -> {
            if (writeFuture.isSuccess()) {
                logger.debug("消息发送成功");
                future.complete(null);
            } else {
                logger.error("消息发送失败", writeFuture.cause());
                future.completeExceptionally(writeFuture.cause());
            }
        });

        return future;
    }

    @Override
    public boolean isConnected() {
        return channel != null && channel.isActive();
    }

    @Override
    public DeviceInfo getDeviceInfo() {
        return deviceInfo;
    }

    @Override
    public ProtocolType getProtocolType() {
        return ProtocolType.HIKVISION_CALLER;
    }
}
