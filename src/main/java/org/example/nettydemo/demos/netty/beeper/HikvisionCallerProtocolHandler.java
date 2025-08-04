package org.example.nettydemo.demos.netty.beeper;

import io.netty.channel.ChannelHandlerContext;
import org.example.nettydemo.demos.netty.client.HikvisionCallerClient;
import org.example.nettydemo.demos.netty.message.EventNotification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>Project: NettyDemo - HikvisionCallerProtocolHandler</p>
 * <p>Powered by szl On 2025-08-01 11:23:40</p>
 * <p>Description: 海康呼叫器协议处理器
 *
 * @author szl
 * @version 1.0
 * @since 1.8
 */
public class HikvisionCallerProtocolHandler implements ProtocolHandler{
    private static final Logger logger = LoggerFactory.getLogger(HikvisionCallerProtocolHandler.class);
//    private final DevMessageCallback callback;
//
//    public HikvisionCallerProtocolHandler(DevMessageCallback callback) {
//        this.callback = callback;
//    }

    @Override
    public void handleMessage(ChannelHandlerContext ctx, Object message) {
        if (message instanceof DevMessage) {
            DevMessage devMsg = (DevMessage) message;
            DevMsgType msgType = DevMsgType.fromValue(devMsg.getHeader().getMsgType());
            logger.info("处理海康呼叫器消息: 类型={}, 长度={}, 消息体={}",
                    devMsg.getHeader().getMsgType(),
                    devMsg.getHeader().getMsgLen(),
                    devMsg.getBody());
            switch (msgType) {
                case DEV_MSG_TYPE_EVENT_NOTIFY:
                    EventNotification eventNotification = XmlMessageParser.parseEventNotification(devMsg.getBody());
                    logger.info("收到事件通知: {}", eventNotification);
                    break;
            }

//            if (callback != null) {
//                callback.onMessageReceived(devMsg);
//            }
        }
    }

    @Override
    public ProtocolType getSupportedProtocol() {
        return ProtocolType.HIKVISION_CALLER;
    }

    @Override
    public void onChannelActive(ChannelHandlerContext ctx) {
        logger.info("海康呼叫器设备连接: {}", ctx.channel().remoteAddress());
//        if (callback != null) {
//            callback.onConnected(ctx.channel());
//        }
    }

    @Override
    public void onChannelInactive(ChannelHandlerContext ctx) {
        logger.info("海康呼叫器设备断开: {}", ctx.channel().remoteAddress());
        String s = ctx.channel().attr(HikvisionCallerClient.DEVICE_ID_KEY).get();
        if (s != null) {
            logger.info("设备ID: {}", s);
        }
//        if (callback != null) {
//            callback.onDisconnected();
//        }
    }
}
