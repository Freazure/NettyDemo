package org.example.nettydemo.demos.netty.message;

import lombok.Data;
import org.example.nettydemo.demos.netty.beeper.ProtocolType;

/**
 * <p>Project: NettyDemo - DeviceInfo</p>
 * <p>Powered by szl On 2025-08-01 17:27:28</p>
 * <p>Description: 设备信息类 </p>
 *
 * @author szl
 * @version 1.0
 * @since 1.8
 */
@Data
public class DeviceInfo {
    private String deviceId;
    private ProtocolType protocolType;
    private boolean connected;
    private String host;
    private int port;
    private long lastActiveTime;

    public DeviceInfo(String deviceId, String host, int port) {
        this.deviceId = deviceId;
        this.host = host;
        this.port = port;
    }

    @Override
    public String toString() {
        return String.format("Device[ID=%s, Protocol=%s, Connected=%s, Address=%s:%d]",
                deviceId, protocolType, connected, host, port);
    }
}
