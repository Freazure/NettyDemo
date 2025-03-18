package org.example.nettydemo.demos.netty.unuse;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

public class MyDecoder extends ByteToMessageDecoder {
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        if (in.readableBytes() < 5) { // 检查是否有足够的数据
            return;
        }
//        HexUtils.toHexString()
        // 读取数据
        byte messageType = in.readByte(); // 0x75
        byte messageFlag = in.readByte(); // 0x7B
        byte dataLength = in.readByte(); // 0x06
        byte dataContent = in.readByte(); // 0x01
        byte checksum = in.readByte(); // 0xEE

        // 创建消息对象
        String message = String.valueOf(messageType + messageFlag + dataLength + dataContent + checksum);

        // 将消息传递给下一个处理器
        out.add(message);
    }
}
