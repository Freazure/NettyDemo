package org.example.nettydemo.demos.netty.unuse;

import io.netty.channel.Channel;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

public class DeviceChannelManager {
    // 存储设备 ID -> Channel 映射
    private static final Map<String, Channel> DEVICE_CHANNEL_MAP = new ConcurrentHashMap<>();

    // 添加设备连接
    public static void addDevice(String deviceId, Channel channel) {
        DEVICE_CHANNEL_MAP.put(deviceId, channel);
    }

    // 移除设备连接
    public static void removeDevice(String deviceId) {
        DEVICE_CHANNEL_MAP.remove(deviceId);
    }

    // 获取设备连接
    public static Channel getDeviceChannel(String deviceId) {
        return DEVICE_CHANNEL_MAP.get(deviceId);
    }

    // 获取设备连接数量
    public static int getDeviceChannelCount() {
        return DEVICE_CHANNEL_MAP.size();
    }
}
