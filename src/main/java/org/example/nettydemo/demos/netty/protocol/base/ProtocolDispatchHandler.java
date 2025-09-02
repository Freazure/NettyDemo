package org.example.nettydemo.demos.netty.protocol.base;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.example.nettydemo.demos.netty.common.enums.ProtocolType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>Project: NettyDemo - ProtocolDispatchHandler</p>
 * <p>Powered by szl On 2025-08-01 11:34:34</p>
 * <p>Description: 协议分发处理器 </p>
 *
 * @author szl
 * @version 1.0
 * @since 1.8
 */
public class ProtocolDispatchHandler extends ChannelInboundHandlerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(ProtocolDispatchHandler.class);

    private final ProtocolRouter protocolRouter;
    private final ProtocolType protocolType;

    public ProtocolDispatchHandler(ProtocolRouter protocolRouter, ProtocolType protocolType) {
        this.protocolRouter = protocolRouter;
        this.protocolType = protocolType;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        protocolRouter.routeMessage(ctx, protocolType, msg);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        protocolRouter.notifyChannelInactive(ctx, protocolType);
        super.channelInactive(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        protocolRouter.notifyExceptionCaught(ctx, protocolType, cause);
    }
}
