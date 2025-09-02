package org.example.nettydemo.demos.netty.protocol.base;

import io.netty.channel.ChannelHandlerContext;
import org.example.nettydemo.demos.netty.common.enums.ProtocolType;

/**
 * <p>Project: NettyDemo - ProtocolHandler</p>
 * <p>Powered by szl On 2025-08-01 11:16:35</p>
 * <p>Description: 协议路由处理器接口 </p>
 *
 * @author szl
 * @version 1.0
 * @since 1.8
 */
public interface ProtocolHandler {
    /**
     * 处理协议消息
     */
    void handleMessage(ChannelHandlerContext ctx, Object message);

    /**
     * 获取支持的协议类型
     */
    ProtocolType getSupportedProtocol();

    /**
     * 连接建立时调用
     */
    default void onChannelActive(ChannelHandlerContext ctx) {}

    /**
     * 连接断开时调用
     */
    default void onChannelInactive(ChannelHandlerContext ctx) {}

    /**
     * 异常处理
     */
    default void onExceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
