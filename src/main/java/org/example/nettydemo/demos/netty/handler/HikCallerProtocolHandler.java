package org.example.nettydemo.demos.netty.handler;

import io.netty.channel.ChannelHandlerContext;
import org.example.nettydemo.demos.netty.beeper.*;
import org.example.nettydemo.demos.netty.message.EventNotification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * <p>Project: NettyDemo - HikvisionCallerProtocolHandler</p>
 * <p>Powered by szl On 2025-08-01 11:23:40</p>
 * <p>Description: 海康呼叫器协议处理器
 *
 * @author szl
 * @version 1.0
 * @since 1.8
 */
public class HikCallerProtocolHandler implements ProtocolHandler {
    private static final Logger logger = LoggerFactory.getLogger(HikCallerProtocolHandler.class);

    @Override
    public void handleMessage(ChannelHandlerContext ctx, Object message) {
        if (message instanceof HikCallerMessage) {
            HikCallerMessage devMsg = (HikCallerMessage) message;
            HikCallerMsgType msgType = HikCallerMsgType.fromValue(devMsg.getHeader().getMsgType());
            logger.info("处理海康呼叫器消息: 类型={}, 长度={}, 消息体={}",
                    devMsg.getHeader().getMsgType(),
                    devMsg.getHeader().getMsgLen(),
                    devMsg.getBody());
            if (Objects.isNull(msgType)) {
                logger.warn("未知的消息类型: {}", devMsg.getHeader().getMsgType());
                return;
            }
            switch (msgType) {
                case DEV_MSG_TYPE_EVENT_NOTIFY:
                    EventNotification eventNotification = XmlMessageParser.parseEventNotification(devMsg.getBody());
                    logger.info("收到事件通知: {}", eventNotification);
                    break;
            }
        }
    }

    @Override
    public ProtocolType getSupportedProtocol() {
        return ProtocolType.HIKVISION_CALLER;
    }

    @Override
    public void onChannelActive(ChannelHandlerContext ctx) {
        logger.info("海康呼叫器设备连接: {}", ctx.channel().remoteAddress());
    }

    @Override
    public void onChannelInactive(ChannelHandlerContext ctx) {
        logger.info("海康呼叫器设备断开: {}", ctx.channel().remoteAddress());
        String s = ctx.channel().attr(HikCallerClient.DEVICE_ID_KEY).get();
        if (s != null) {
            logger.info("设备ID: {}", s);
        }
    }
}
