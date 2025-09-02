package org.example.nettydemo.demos.netty.client;

import org.example.nettydemo.demos.netty.protocol.hikcaller.client.HikCallerClient;
import org.example.nettydemo.demos.netty.common.enums.ProtocolType;
import org.example.nettydemo.demos.netty.message.DeviceInfo;
import org.example.nettydemo.demos.netty.protocol.radar.client.RadarClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>Project: NettyDemo - ClientFactory</p>
 * <p>Powered by szl On 2025-08-04 11:20:09</p>
 * <p>Description:
 *
 * @author szl
 * @version 1.0
 * @since 1.8
 */
public class ClientFactory {
    private static final Logger logger = LoggerFactory.getLogger(ClientFactory.class);

    public static IDeviceClient createClient(DeviceInfo deviceInfo) {
        switch (deviceInfo.getProtocolType()) {
            case HIKVISION_CALLER:
                return new HikCallerClient(deviceInfo);
            case RADAR:
                return new RadarClient(deviceInfo);
            default:
                throw new IllegalArgumentException("不支持的协议类型: " + deviceInfo.getProtocolType());
        }
    }

    public static IDeviceClient createClient(String deviceId, String host, int port,
                                             ProtocolType protocolType) {
        DeviceInfo deviceInfo = new DeviceInfo(deviceId, host, port);
        deviceInfo.setProtocolType(protocolType);
        return createClient(deviceInfo);
    }
}
