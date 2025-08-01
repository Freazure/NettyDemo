package org.example.nettydemo.demos.netty.beeper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.net.www.http.HttpClient;

import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>Project: NettyDemo - MultiProtocolClientManager</p>
 * <p>Powered by szl On 2025-08-01 17:15:26</p>
 * <p>Description: 客户端管理 </p>
 *
 * @author szl
 * @version 1.0
 * @since 1.8
 */
public class MultiProtocolClientManager {
    private static final Logger logger = LoggerFactory.getLogger(MultiProtocolClientManager.class);

    private final Map<String, ProtocolClientWrapper> clients = new ConcurrentHashMap<>();

    /**
     * 连接海康呼叫器设备
     */
    public CompletableFuture<DeviceClient> connectHaikangCaller(String deviceId, String host, int port,
                                                                DevMessageCallback callback) {
        DeviceClient client = new DeviceClient(host, port, callback);
        ProtocolClientWrapper wrapper = new ProtocolClientWrapper(ProtocolType.HAIKANG_CALLER, client);
        clients.put(deviceId, wrapper);

        return client.connect().thenApply(v -> client);
    }

    /**
     * 连接Modbus TCP设备
     */
    public CompletableFuture<ModbusTcpClient> connectModbusTcp(String deviceId, String host, int port) {
        ModbusTcpClient client = new ModbusTcpClient(host, port);
        ProtocolClientWrapper wrapper = new ProtocolClientWrapper(ProtocolType.MODBUS_TCP, client);
        clients.put(deviceId, wrapper);

        return client.connect().thenApply(v -> client);
    }

    /**
     * 连接HTTP设备
     */
    public CompletableFuture<HttpClient> connectHttp(String deviceId, String host, int port) {
        HttpClient client = new HttpClient(host, port);
        ProtocolClientWrapper wrapper = new ProtocolClientWrapper(ProtocolType.CUSTOM_HTTP, client);
        clients.put(deviceId, wrapper);

        return client.connect().thenApply(v -> client);
    }

    /**
     * 获取设备客户端
     */
    @SuppressWarnings("unchecked")
    public <T> T getClient(String deviceId, Class<T> clientType) {
        ProtocolClientWrapper wrapper = clients.get(deviceId);
        if (wrapper != null && clientType.isInstance(wrapper.getClient())) {
            return (T) wrapper.getClient();
        }
        return null;
    }

    /**
     * 获取设备协议类型
     */
    public ProtocolType getProtocolType(String deviceId) {
        ProtocolClientWrapper wrapper = clients.get(deviceId);
        return wrapper != null ? wrapper.getProtocolType() : null;
    }

    /**
     * 断开指定设备连接
     */
    public CompletableFuture<Void> disconnect(String deviceId) {
        ProtocolClientWrapper wrapper = clients.remove(deviceId);
        if (wrapper != null) {
            return wrapper.disconnect();
        }
        return CompletableFuture.completedFuture(null);
    }

    /**
     * 断开所有连接
     */
    public CompletableFuture<Void> disconnectAll() {
        CompletableFuture<?>[] futures = clients.values().stream()
                .map(ProtocolClientWrapper::disconnect)
                .toArray(CompletableFuture[]::new);

        clients.clear();
        return CompletableFuture.allOf(futures);
    }

    /**
     * 获取所有连接的设备信息
     */
    public Map<String, DeviceInfo> getAllDevices() {
        Map<String, DeviceInfo> deviceInfos = new HashMap<>();
        clients.forEach((deviceId, wrapper) -> {
            DeviceInfo info = new DeviceInfo();
            info.setDeviceId(deviceId);
            info.setProtocolType(wrapper.getProtocolType());
            info.setConnected(wrapper.isConnected());
            deviceInfos.put(deviceId, info);
        });
        return deviceInfos;
    }
}

