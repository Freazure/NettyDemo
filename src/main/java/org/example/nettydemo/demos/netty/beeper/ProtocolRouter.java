package org.example.nettydemo.demos.netty.beeper;

import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>Project: NettyDemo - ProtocolRouter</p>
 * <p>Powered by szl On 2025-08-01 11:29:36</p>
 * <p>Description: 协议路由器 </p>
 *
 * @author szl
 * @version 1.0
 * @since 1.8
 */
@Component
@Slf4j
public class ProtocolRouter {
    private final Map<ProtocolType, ProtocolHandler> handlers = new ConcurrentHashMap<>();

    /**
     * 注册协议处理器
     */
    public void registerHandler(ProtocolHandler handler) {
        handlers.put(handler.getSupportedProtocol(), handler);
    }

    /**
     * 移除协议处理器
     */
    public void unregisterHandler(ProtocolType protocolType) {
        handlers.remove(protocolType);
    }

    /**
     * 获取协议处理器
     */
    public ProtocolHandler getHandler(ProtocolType protocolType) {
        return handlers.get(protocolType);
    }

    /**
     * 路由消息到对应的处理器
     */
    public void routeMessage(ChannelHandlerContext ctx, ProtocolType protocolType, Object message) {
        ProtocolHandler handler = handlers.get(protocolType);
        if (handler != null) {
            handler.handleMessage(ctx, message);
        } else {
            log.warn("未找到协议处理器: {}", protocolType);
        }
    }

    /**
     * 通知连接建立
     */
    public void notifyChannelActive(ChannelHandlerContext ctx, ProtocolType protocolType) {
        ProtocolHandler handler = handlers.get(protocolType);
        if (handler != null) {
            handler.onChannelActive(ctx);
        }
    }

    /**
     * 通知连接断开
     */
    public void notifyChannelInactive(ChannelHandlerContext ctx, ProtocolType protocolType) {
        ProtocolHandler handler = handlers.get(protocolType);
        if (handler != null) {
            handler.onChannelInactive(ctx);
        }
    }

    /**
     * 通知异常
     */
    public void notifyExceptionCaught(ChannelHandlerContext ctx, ProtocolType protocolType, Throwable cause) {
        ProtocolHandler handler = handlers.get(protocolType);
        if (handler != null) {
            handler.onExceptionCaught(ctx, cause);
        }
    }
}
