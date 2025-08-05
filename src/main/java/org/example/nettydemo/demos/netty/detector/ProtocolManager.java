package org.example.nettydemo.demos.netty.detector;

import io.netty.buffer.ByteBuf;
import org.example.nettydemo.demos.netty.beeper.ProtocolType;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * <p>Project: NettyDemo - ProtocolManager</p>
 * <p>Powered by szl On 2025-08-01 11:14:32</p>
 * <p>Description: 协议管理器 </p>
 *
 * @author szl
 * @version 1.0
 * @since 1.8
 */
public class ProtocolManager {
    private final List<ProtocolDetector> detectors = new CopyOnWriteArrayList<>();

    public ProtocolManager() {
        // 注册默认的协议检测器
        registerDetector(new HikCallerProtocolDetector());
        registerDetector(new RadarProtocolDetector());
    }

    /**
     * 注册协议检测器
     */
    public void registerDetector(ProtocolDetector detector) {
        detectors.add(detector);
    }

    /**
     * 移除协议检测器
     */
    public void unregisterDetector(ProtocolType protocolType) {
        detectors.removeIf(detector -> detector.getSupportedProtocol() == protocolType);
    }

    /**
     * 检测协议类型
     */
    public ProtocolType detectProtocol(ByteBuf buffer) {
        // 计算所需的最小字节数
        int maxMinBytes = detectors.stream()
                .mapToInt(ProtocolDetector::getMinBytesRequired)
                .max()
                .orElse(0);

        if (buffer.readableBytes() < maxMinBytes) {
            // 数据不足，无法检测
            return null;
        }

        // 逐个尝试检测器
        for (ProtocolDetector detector : detectors) {
            ProtocolType type = detector.detectProtocol(buffer);
            if (type != null) {
                return type;
            }
        }

        return ProtocolType.UNKNOWN;
    }

    /**
     * 获取最大检测字节数需求
     */
    public int getMaxDetectionBytes() {
        // 默认8字节
        return detectors.stream()
                .mapToInt(ProtocolDetector::getMinBytesRequired)
                .max()
                .orElse(8);
    }
}
