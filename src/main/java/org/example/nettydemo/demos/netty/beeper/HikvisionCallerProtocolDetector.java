package org.example.nettydemo.demos.netty.beeper;

import io.netty.buffer.ByteBuf;

/**
 * <p>Project: NettyDemo - HikvisionCallerProtocolDetector</p>
 * <p>Powered by szl On 2025-08-01 11:09:00</p>
 * <p>Description: 海康呼叫器协议类型解析 </p>
 *
 * @author szl
 * @version 1.0
 * @since 1.8
 */
public class HikvisionCallerProtocolDetector implements ProtocolDetector{

    private static final byte[] MAGIC_BYTES = DevSdkMsgHead.MAGIC;

    @Override
    public ProtocolType detectProtocol(ByteBuf buffer) {
        if (buffer.readableBytes() < getMinBytesRequired()) {
            return null;
        }

        // 检查魔法字符
        for (int i = 0; i < MAGIC_BYTES.length; i++) {
            if (buffer.getByte(buffer.readerIndex() + i) != MAGIC_BYTES[i]) {
                return null;
            }
        }

        return ProtocolType.HIKVISION_CALLER;
    }

    @Override
    public int getMinBytesRequired() {
        return 4;
    }

    @Override
    public ProtocolType getSupportedProtocol() {
        return ProtocolType.HIKVISION_CALLER;
    }
}
