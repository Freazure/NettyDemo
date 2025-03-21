package org.example.nettydemo.demos.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.ScheduledFuture;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * <p>Project: NettyDemo - RadarClient</p>
 * <p>Powered by szl On 2025-03-14 16:32:40</p>
 * <p>Description:
 *
 * @author szl
 * @version 1.0
 * @since 1.8
 */
@Slf4j
public class RadarClient {

    private static final Logger radarLogger = LoggerFactory.getLogger("RadarLogger");

    private final String host;
    private final int port;
    @Getter
    private Channel channel;
    @Getter
    private Bootstrap bootstrap = new Bootstrap();
    // 客户端的NIO线程组
    @Getter
    private EventLoopGroup group = new NioEventLoopGroup();

    private ScheduledFuture<?> switchTaskFuture;

    @Getter
    private AtomicBoolean toggle = new AtomicBoolean(true);

    public RadarClient(String host, int port) {
        this.host = host;
        this.port = port;
        // 设置group, Bootstrap 是一个启动NIO服务的辅助启动类 客户端的
        bootstrap.group(group)
                // 关联客户端通道
                .channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                // 设置 I/O处理类,主要用于网络I/O事件，记录日志，编码、解码消息
                .handler(new RadarClientInitializer());
    }

    public void start() {
        try {
            // 连接服务端
            ChannelFuture f = bootstrap.connect(host, port);
            channel = f.channel();
            //通常需要写不断重连服务端
            f.addListener((ChannelFutureListener) future -> {
                if (!future.isSuccess()) {
                    //重连交给后端线程执行
                    future.channel().eventLoop().schedule(() -> {
                        radarLogger.info("Radar [{}:{}]] restart connection...", host, port);
                        log.info("Radar [{}:{}]] restart connection...", host, port);
                        try {
                            start();
                        } catch (Exception e) {
                            radarLogger.error("Radar [{}:{}]] connection error!", host, port,e);
                            log.error("Radar [{}:{}]] connection error!", host, port,e);
                        }
                    }, 3000, TimeUnit.MILLISECONDS);
                } else {
                    // 启动定时任务（切换大区）
//                    scheduleSwitchTask();
                    // 监听连接关闭
                    channel.closeFuture().addListener(closeFuture -> {
                        radarLogger.warn("Radar [{}:{}] connection closed.", host, port);
                        log.warn("Radar [{}:{}] connection closed.", host, port);
                        start();
                    });
                    radarLogger.info("Radar [{}:{}]] connection success!", host, port);
                    log.info("Radar [{}:{}]] connection success!", host, port);
                }
            });
        }
        catch (Exception e) {
            radarLogger.error("Radar [{}:{}]] connection error!", host, port,e);
            log.error("Radar [{}:{}]] connection error!", host, port,e);
        }
    }

    private void scheduleSwitchTask() {
        // 使用channel所在的EventLoop安排定时任务
        // 切换当前区域
        // 发送切换指令
        if (switchTaskFuture != null) {
            switchTaskFuture.cancel(false);
            switchTaskFuture = null;
        }
        switchTaskFuture = channel.eventLoop().scheduleAtFixedRate(this::sendSwitchCommand, 0, 100, TimeUnit.MILLISECONDS);
    }

    private void sendSwitchCommand() {
        byte[] a10 = new byte[]{(byte) 0x35, (byte) 0x3A , (byte) 0x0A,(byte) 0xFF};
        ByteBuf buffer10 = null;
        buffer10 = Unpooled.buffer(a10.length);
        buffer10.writeBytes(a10);
        channel.writeAndFlush(buffer10);
    }



    public void shutdown() {
        try {
            if (channel != null) {
                // 关闭 Channel
                channel.close().sync();
            }
            // 关闭 EventLoopGroup
            group.shutdownGracefully().sync();
        } catch (InterruptedException e) {
            radarLogger.error("关闭客户端时出错", e);
            log.error("关闭客户端时出错", e);
            Thread.currentThread().interrupt();
        }
    }
}
