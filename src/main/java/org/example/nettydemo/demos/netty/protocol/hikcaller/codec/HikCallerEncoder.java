package org.example.nettydemo.demos.netty.protocol.hikcaller.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.example.nettydemo.demos.netty.protocol.hikcaller.message.HikCallerMessage;
import org.example.nettydemo.demos.netty.protocol.hikcaller.message.HikCallerMsgHead;

import java.nio.charset.StandardCharsets;

/**
 * <p>Project: NettyDemo - DevMsgEncoder</p>
 * <p>Powered by szl On 2025-08-01 10:16:32</p>
 * <p>Description: 协议消息编解码器 </p>
 *
 * @author szl
 * @version 1.0
 * @since 1.8
 */
public class HikCallerEncoder extends MessageToByteEncoder<HikCallerMessage> {
    @Override
    protected void encode(ChannelHandlerContext ctx, HikCallerMessage msg, ByteBuf out) throws Exception {
        // 编码消息头
        HikCallerMsgHead header = msg.getHeader();

        // 写入魔法字符
        out.writeBytes(header.getMagic());

        // 写入消息长度
        out.writeInt(header.getMsgLen());

        // 写入消息类型
        out.writeInt(header.getMsgType());

        // 写入消息序号
        out.writeInt(header.getMsgSeq());

        // 写入CRC32
        out.writeInt(header.getCrc32());

        // 写入加密ID
        out.writeInt(header.getEncryptId());

        // 写入版本号
        out.writeByte(header.getVersion());

        // 写入加密标志
        out.writeByte(header.getEnc());

        // 写入保留字段
        out.writeBytes(header.getRes());

        // 写入消息体（XML数据）
        if (msg.getBody() != null && !msg.getBody().isEmpty()) {
            byte[] bodyBytes = msg.getBody().getBytes(StandardCharsets.UTF_8);
            out.writeBytes(bodyBytes);
            // 添加结束符
            out.writeByte(0x00);
        }
    }
}
