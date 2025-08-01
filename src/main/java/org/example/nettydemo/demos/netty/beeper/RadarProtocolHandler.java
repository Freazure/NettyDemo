package org.example.nettydemo.demos.netty.beeper;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>Project: NettyDemo - RadarProtocolHandler</p>
 * <p>Powered by szl On 2025-08-01 15:38:12</p>
 * <p>Description: 雷达协议处理器 </p>
 *
 * @author szl
 * @version 1.0
 * @since 1.8
 */
public class RadarProtocolHandler implements ProtocolHandler{

    private static final Logger logger = LoggerFactory.getLogger(RadarProtocolHandler.class);

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
        ProtocolHandler.super.onChannelActive(ctx);
    }

    @Override
    public void onChannelInactive(ChannelHandlerContext ctx) {
        ProtocolHandler.super.onChannelInactive(ctx);
    }

    @Override
    public void onExceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        ProtocolHandler.super.onExceptionCaught(ctx, cause);
    }
}
