package org.example.nettydemo.demos.netty.common.enums;

/**
 * <p>Project: NettyDemo - ProtocolType</p>
 * <p>Powered by szl On 2025-08-01 11:04:38</p>
 * <p>Description: 协议类型枚举 </p>
 *
 * @author szl
 * @version 1.0
 * @since 1.8
 */
public enum ProtocolType {
    HIKVISION_CALLER("HKMV", "海康呼叫器协议"),
    RADAR("RADAR", "雷达协议"),
    UNKNOWN("UNKNOWN", "未知协议");

    private final String identifier;
    private final String description;

    ProtocolType(String identifier, String description) {
        this.identifier = identifier;
        this.description = description;
    }

    public String getIdentifier() { return identifier; }
    public String getDescription() { return description; }
}
