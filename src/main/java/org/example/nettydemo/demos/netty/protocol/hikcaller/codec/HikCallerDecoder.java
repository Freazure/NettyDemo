package org.example.nettydemo.demos.netty.protocol.hikcaller.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.example.nettydemo.demos.netty.protocol.hikcaller.message.HikCallerMessage;
import org.example.nettydemo.demos.netty.protocol.hikcaller.message.HikCallerMsgHead;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * <p>Project: NettyDemo - DevMsgDecoder</p>
 * <p>Powered by szl On 2025-08-01 10:17:28</p>
 * <p>Description: 消息解码器 </p>
 *
 * @author szl
 * @version 1.0
 * @since 1.8
 */
public class HikCallerDecoder extends ByteToMessageDecoder {


    private static final Logger logger = LoggerFactory.getLogger(HikCallerDecoder.class);

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        // 检查是否有足够的字节来读取头部
        if (in.readableBytes() < HikCallerMsgHead.HEADER_SIZE) {
            return;
        }

        // 标记读取位置
        in.markReaderIndex();

        // 读取魔法字符
        byte[] magic = new byte[4];
        in.readBytes(magic);

        // 验证魔法字符
        if (!java.util.Arrays.equals(magic, HikCallerMsgHead.MAGIC)) {
            in.resetReaderIndex();
            in.skipBytes(1); // 跳过一个字节继续寻找
            return;
        }

        // 读取消息长度
        int msgLen = in.readInt();

        // 检查消息是否完整
        if (in.readableBytes() < msgLen - HikCallerMsgHead.HEADER_SIZE) {
            in.resetReaderIndex();
            return;
        }

        // 读取消息头剩余部分
        int msgType = in.readInt();
        int msgSeq = in.readInt();
        int crc32 = in.readInt();
        int encryptId = in.readInt();
        byte version = in.readByte();
        byte enc = in.readByte();
        byte[] res = new byte[2];
        in.readBytes(res);

        // 创建消息头
        HikCallerMsgHead header = new HikCallerMsgHead(msgType, msgLen);
        header.setMsgSeq(msgSeq);
        header.setCrc32(crc32);
        header.setEncryptId(encryptId);

        // 读取消息体
        String body = null;
        int bodyLen = msgLen - HikCallerMsgHead.HEADER_SIZE;
        if (bodyLen > 0) {
            byte[] bodyBytes = new byte[bodyLen];
            in.readBytes(bodyBytes);

            // 移除结尾的0x00字节
            int actualLen = bodyLen;
            for (int i = bodyBytes.length - 1; i >= 0; i--) {
                if (bodyBytes[i] == 0) {
                    actualLen = i;
                } else {
                    break;
                }
            }

            if (actualLen > 0) {
                body = new String(bodyBytes, 0, actualLen, StandardCharsets.UTF_8);
            }
        }

        // 创建消息对象
        HikCallerMessage message = new HikCallerMessage(header, body);
        out.add(message);
    }
}
