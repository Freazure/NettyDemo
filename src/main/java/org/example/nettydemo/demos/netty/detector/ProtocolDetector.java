package org.example.nettydemo.demos.netty.detector;

import io.netty.buffer.ByteBuf;
import org.example.nettydemo.demos.netty.beeper.ProtocolType;

/**
 * <p>Project: NettyDemo - ProtocolDetector</p>
 * <p>Powered by szl On 2025-08-01 11:07:21</p>
 * <p>Description: 协议类型解析接口 </p>
 *
 * @author szl
 * @version 1.0
 * @since 1.8
 */
public interface ProtocolDetector {
    /**
     * 检测协议类型
     * @param buffer 数据缓冲区
     * @return 协议类型，如果无法确定返回null
     */
    ProtocolType detectProtocol(ByteBuf buffer);

    /**
     * 获取检测所需的最小字节数
     * @return 最小字节数
     */
    int getMinBytesRequired();

    /**
     * 获取支持的协议类型
     * @return 协议类型
     */
    ProtocolType getSupportedProtocol();
}
