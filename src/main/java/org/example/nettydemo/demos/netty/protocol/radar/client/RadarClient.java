package org.example.nettydemo.demos.netty.protocol.radar.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.FixedLengthFrameDecoder;
import io.netty.util.AttributeKey;
import lombok.Getter;
import org.example.nettydemo.demos.netty.client.IDeviceClient;
import org.example.nettydemo.demos.netty.common.enums.ProtocolType;
import org.example.nettydemo.demos.netty.protocol.base.ProtocolDispatchHandler;
import org.example.nettydemo.demos.netty.protocol.base.ProtocolRouter;
import org.example.nettydemo.demos.netty.protocol.radar.handler.RadarProtocolHandler;
import org.example.nettydemo.demos.netty.message.DeviceInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * <p>Project: NettyDemo - RadarClient</p>
 * <p>Powered by szl On 2025-08-05 10:13:17</p>
 * <p>Description:
 *
 * @author szl
 * @version 1.0
 * @since 1.8
 */
public class RadarClient implements IDeviceClient {

    private final DeviceInfo deviceInfo;

    private static final Logger logger = LoggerFactory.getLogger(RadarClient.class);
    @Getter
    private Channel channel;
    @Getter
    private Bootstrap bootstrap = new Bootstrap();
    // 客户端的NIO线程组
    @Getter
    private EventLoopGroup group = new NioEventLoopGroup();

    public static final AttributeKey<String> DEVICE_ID_KEY = AttributeKey.valueOf("DEVICE_ID_KEY");

    public RadarClient(DeviceInfo deviceInfo) {
        this.deviceInfo = deviceInfo;
        ProtocolRouter protocolRouter = new ProtocolRouter();
        protocolRouter.registerHandler(new RadarProtocolHandler());
        // 设置group, Bootstrap 是一个启动NIO服务的辅助启动类 客户端的
        bootstrap.group(group)
                // 关联客户端通道
                .channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                // 设置 I/O处理类,主要用于网络I/O事件，记录日志，编码、解码消息
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        // 为每个连接生成唯一的设备ID（临时）
                        String tempDeviceId = "DEVICE_" + deviceInfo.getDeviceId();
                        ch.attr(DEVICE_ID_KEY).set(tempDeviceId);

                        ChannelPipeline pipeline = ch.pipeline();

                        // 添加编解码器
                        pipeline.addLast("decoder", new FixedLengthFrameDecoder(5));

                        // 添加业务处理器
                        pipeline.addLast("handler", new ProtocolDispatchHandler(protocolRouter, deviceInfo.getProtocolType()));
                    }
                });
    }
    @Override
    public CompletableFuture<Void> connect() {
        CompletableFuture<Void> future = new CompletableFuture<>();
        try {
            // 连接服务端
            ChannelFuture f = bootstrap.connect(deviceInfo.getHost(), deviceInfo.getPort());
            channel = f.channel();
            // 为每个连接生成唯一的设备ID（临时）
            String tempDeviceId = "DEVICE_" + deviceInfo.getDeviceId();
            channel.attr(DEVICE_ID_KEY).set(tempDeviceId);
            //通常需要写不断重连服务端
            f.addListener((ChannelFutureListener) channelFuture -> {
                if (!channelFuture.isSuccess()) {
                    //重连交给后端线程执行
                    channelFuture.channel().eventLoop().schedule(() -> {
                        logger.info("设备 [{}] 重新连接...", deviceInfo.getDeviceId());
                        connect();
                    }, 3000, TimeUnit.MILLISECONDS);
                } else {
                    // 监听连接关闭
                    channel.closeFuture().addListener(closeFuture -> {
                        logger.warn("设备 [{}] 连接关闭。", deviceInfo.getDeviceId());
                        connect();
                    });
                    logger.info("设备 [{}] 连接成功!", deviceInfo.getDeviceId());
                }
            });
        }
        catch (Exception e) {
            future.completeExceptionally(e);
            logger.error("设备 [{}] 连接异常!", deviceInfo.getDeviceId(),e);
        }
        return future;
    }

    @Override
    public CompletableFuture<Void> disconnect() {
        CompletableFuture<Void> future = new CompletableFuture<>();
        try {
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
        }
        catch (Exception e) {
            logger.error("客户端关闭异常", e);
            future.completeExceptionally(e);
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
        return ProtocolType.RADAR;
    }
}
