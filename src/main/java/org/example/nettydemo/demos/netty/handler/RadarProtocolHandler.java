package org.example.nettydemo.demos.netty.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import org.example.nettydemo.demos.netty.beeper.HikCallerClient;
import org.example.nettydemo.demos.netty.beeper.ProtocolType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * <p>Project: NettyDemo - RadarProtocolHandler</p>
 * <p>Powered by szl On 2025-08-01 15:38:12</p>
 * <p>Description: 雷达协议处理器 </p>
 *
 * @author szl
 * @version 1.0
 * @since 1.8
 */
public class RadarProtocolHandler implements ProtocolHandler {

    private static final Logger logger = LoggerFactory.getLogger(RadarProtocolHandler.class);

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Override
    public void handleMessage(ChannelHandlerContext ctx, Object message) {
        ByteBuf buf = (ByteBuf) message;
        try {
            // 将buf写入到一个字节数组中
            byte[] bytes = new byte[buf.readableBytes()];
            buf.readBytes(bytes);
            logger.info("收到设备原消息: {}", bytes);
        } finally {
            // 确保释放 ByteBuf
            buf.release();
        }
    }

    @Override
    public ProtocolType getSupportedProtocol() {
        return ProtocolType.RADAR;
    }

    @Override
    public void onChannelActive(ChannelHandlerContext ctx) {
        String s = ctx.channel().attr(HikCallerClient.DEVICE_ID_KEY).get();
        if (s != null) {
            logger.info("雷达设备[{}]连接: {}", s, ctx.channel().remoteAddress());
        }
    }

    @Override
    public void onChannelInactive(ChannelHandlerContext ctx) {
        String s = ctx.channel().attr(HikCallerClient.DEVICE_ID_KEY).get();
        if (s != null) {
            logger.info("雷达设备[{}]断开: {}", s, ctx.channel().remoteAddress());
        }
    }

    @Override
    public void onExceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        String s = ctx.channel().attr(HikCallerClient.DEVICE_ID_KEY).get();
        if (s != null) {
            logger.info("雷达设备[{}]异常断开: {}, 异常原因: ", s, ctx.channel().remoteAddress(), cause);
        }
    }
}
