package org.example.nettydemo.demos.netty.detector;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.FixedLengthFrameDecoder;
import io.netty.handler.codec.MessageToByteEncoder;
import org.example.nettydemo.demos.netty.beeper.HikCallerDecoder;
import org.example.nettydemo.demos.netty.beeper.HikCallerEncoder;
import org.example.nettydemo.demos.netty.handler.ProtocolDispatchHandler;
import org.example.nettydemo.demos.netty.beeper.ProtocolType;
import org.example.nettydemo.demos.netty.handler.ProtocolRouter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * <p>Project: NettyDemo - ProtocolDetectionDecoder</p>
 * <p>Powered by szl On 2025-08-01 11:31:24</p>
 * <p>Description: 协议检测解码器
 *
 * @author szl
 * @version 1.0
 * @since 1.8
 */
public class ProtocolDetectionDecoder extends ByteToMessageDecoder {
    private static final Logger logger = LoggerFactory.getLogger(ProtocolDetectionDecoder.class);

    private final ProtocolManager protocolManager;
    private final ProtocolRouter protocolRouter;
    private ProtocolType detectedProtocol = null;
    private ByteToMessageDecoder protocolDecoder = null;

    public ProtocolDetectionDecoder(ProtocolManager protocolManager, ProtocolRouter protocolRouter) {
        this.protocolManager = protocolManager;
        this.protocolRouter = protocolRouter;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        // 如果协议已经检测出来，直接使用对应的解码器
        if (detectedProtocol != null && protocolDecoder != null) {
//            protocolDecoder.decode(ctx, in, out);
            ctx.fireChannelRead(in.readRetainedSlice(in.readableBytes()));
            return;
        }

        // 检查是否有足够的数据进行协议检测
        if (in.readableBytes() < protocolManager.getMaxDetectionBytes()) {
            return; // 等待更多数据
        }

        // 检测协议类型
        ProtocolType protocolType = protocolManager.detectProtocol(in);

        if (protocolType == null) {
            return; // 数据不足，继续等待
        }

        if (protocolType == ProtocolType.UNKNOWN) {
            // 未知协议，丢弃一些字节后重试
            logger.warn("检测到未知协议，丢弃数据: {}", in.readableBytes());
            in.skipBytes(Math.min(in.readableBytes(), 1));
            return;
        }

        // 协议检测成功
        detectedProtocol = protocolType;
        logger.info("检测到协议类型: {}", protocolType);

        // 创建对应的协议解码器
        protocolDecoder = createProtocolDecoder(protocolType);

        if (protocolDecoder != null) {
            // 通知协议路由器连接建立
            protocolRouter.notifyChannelActive(ctx, protocolType);

            // 使用新的解码器处理当前数据
//            protocolDecoder.decode(ctx, in, out);

            // 替换管道中的解码器
            ctx.pipeline().replace(this, "protocolDecoder", protocolDecoder);

            // 添加对应的编码器
            MessageToByteEncoder<?> encoder = createProtocolEncoder(protocolType);
            if (encoder != null) {
                ctx.pipeline().addBefore("protocolDecoder", "protocolEncoder", encoder);
            }

            // 添加协议处理器
            ctx.pipeline().addAfter("protocolDecoder", "protocolHandler",
                    new ProtocolDispatchHandler(protocolRouter, protocolType));

            // 重新触发 pipeline，交由新的 decoder 处理剩余数据
            ctx.fireChannelRead(in.readRetainedSlice(in.readableBytes()));
        } else {
            logger.error("无法创建协议解码器: {}", protocolType);
            ctx.close();
        }
    }

    /**
     * 创建协议解码器
     */
    private ByteToMessageDecoder createProtocolDecoder(ProtocolType protocolType) {
        switch (protocolType) {
            case HIKVISION_CALLER:
                return new HikCallerDecoder();
            case RADAR:
                return new FixedLengthFrameDecoder(5);
            default:
                return null;
        }
    }

    /**
     * 创建协议编码器
     */
    private MessageToByteEncoder<?> createProtocolEncoder(ProtocolType protocolType) {
        switch (protocolType) {
            case HIKVISION_CALLER:
                return new HikCallerEncoder();
            case RADAR:
                // 默认编码
                return null;
            default:
                return null;
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        if (detectedProtocol != null) {
            protocolRouter.notifyChannelInactive(ctx, detectedProtocol);
        }
        super.channelInactive(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (detectedProtocol != null) {
            protocolRouter.notifyExceptionCaught(ctx, detectedProtocol, cause);
        } else {
            logger.error("协议检测阶段发生异常", cause);
            ctx.close();
        }
    }
}
