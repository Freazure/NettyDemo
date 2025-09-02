package org.example.nettydemo.demos.netty.protocol.radar.detect;

import io.netty.buffer.ByteBuf;
import org.example.nettydemo.demos.netty.common.enums.ProtocolType;
import org.example.nettydemo.demos.netty.protocol.base.ProtocolDetector;

/**
 * <p>Project: NettyDemo - RadarProtocolDetector</p>
 * <p>Powered by szl On 2025-08-01 15:19:16</p>
 * <p>Description: 雷达协议解析</p>
 *
 * @author szl
 * @version 1.0
 * @since 1.8
 */
public class RadarProtocolDetector implements ProtocolDetector {
    @Override
    public ProtocolType detectProtocol(ByteBuf buffer) {
        if (buffer.readableBytes() < getMinBytesRequired()) {
            return null;
        }

        if (buffer.getByte(buffer.readerIndex()) == (byte) 0x75 && buffer.getByte(buffer.readerIndex() + 1) == (byte) 0x7B &&
            buffer.getByte(buffer.readerIndex() + 4) == (byte) 0xEE) {
            return ProtocolType.RADAR;
        }
        return null;
    }

    @Override
    public int getMinBytesRequired() {
        return 5;
    }

    @Override
    public ProtocolType getSupportedProtocol() {
        return ProtocolType.RADAR;
    }
}
