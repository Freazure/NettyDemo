package org.example.nettydemo.demos.netty.client;

import org.example.nettydemo.demos.netty.beeper.HikCallerClient;
import org.example.nettydemo.demos.netty.beeper.ProtocolType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * <p>Project: NettyDemo - MultiClientManager</p>
 * <p>Powered by szl On 2025-08-04 11:18:06</p>
 * <p>Description:
 *
 * @author szl
 * @version 1.0
 * @since 1.8
 */
public class ClientManager {
    private static final Logger logger = LoggerFactory.getLogger(ClientManager.class);

    private static final Map<String, IDeviceClient> clients = new ConcurrentHashMap<>();
    private static final Map<String, CompletableFuture<Void>> connectingFutures = new ConcurrentHashMap<>();
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(4);

    /**
     * 添加设备客户端
     */
    public static CompletableFuture<Void> addClient(String deviceId, String host, int port,
                                             ProtocolType protocolType) {
        if (clients.containsKey(deviceId)) {
            CompletableFuture<Void> future = new CompletableFuture<>();
            future.completeExceptionally(new IllegalArgumentException("设备已存在: " + deviceId));
            return future;
        }

        // 检查是否正在连接
        CompletableFuture<Void> existingFuture = connectingFutures.get(deviceId);
        if (existingFuture != null) {
            return existingFuture;
        }

        try {
            IDeviceClient client = ClientFactory.createClient(deviceId, host, port, protocolType);

            CompletableFuture<Void> future = client.connect()
                    .thenRun(() -> {
                        clients.put(deviceId, client);
                        connectingFutures.remove(deviceId);
                        logger.info("设备客户端添加成功: {} [{}]", deviceId, protocolType);
                    })
                    .exceptionally(ex -> {
                        connectingFutures.remove(deviceId);
                        logger.error("设备客户端添加失败: {} [{}]", deviceId, protocolType, ex);
                        throw new RuntimeException(ex);
                    });

            connectingFutures.put(deviceId, future);
            return future;

        } catch (Exception e) {
            CompletableFuture<Void> future = new CompletableFuture<>();
            future.completeExceptionally(e);
            return future;
        }
    }

    /**
     * 移除设备客户端
     */
    public static CompletableFuture<Void> removeClient(String deviceId) {
        IDeviceClient client = clients.remove(deviceId);
        if (client != null) {
            return client.disconnect().thenRun(() -> {
                logger.info("设备客户端移除成功: {}", deviceId);
            });
        }
        return CompletableFuture.completedFuture(null);
    }

    /**
     * 获取客户端
     */
    public static IDeviceClient getClient(String deviceId) {
        return clients.get(deviceId);
    }

    /**
     * 获取海康客户端
     */
    public static HikCallerClient getHikvisionCallerClient(String deviceId) {
        IDeviceClient client = clients.get(deviceId);
        if (client instanceof HikCallerClient) {
            return (HikCallerClient) client;
        }
        return null;
    }

    /**
     * 发送消息到指定设备
     */
    public static CompletableFuture<Void> sendMessage(String deviceId, Object message) {
        IDeviceClient client = clients.get(deviceId);
        if (client == null) {
            CompletableFuture<Void> future = new CompletableFuture<>();
            future.completeExceptionally(new IllegalArgumentException("设备不存在: " + deviceId));
            return future;
        }

        if (!client.isConnected()) {
            CompletableFuture<Void> future = new CompletableFuture<>();
            future.completeExceptionally(new IllegalStateException("设备未连接: " + deviceId));
            return future;
        }

        return client.sendMessage(message);
    }

    /**
     * 批量发送消息
     */
    public static CompletableFuture<Map<String, Boolean>> broadcastMessage(Object message,
                                                                    ProtocolType protocolType) {
        Map<String, CompletableFuture<Void>> futures = new HashMap<>();

        clients.entrySet().stream()
                .filter(entry -> entry.getValue().getProtocolType() == protocolType)
                .forEach(entry -> {
                    String deviceId = entry.getKey();
                    IDeviceClient client = entry.getValue();
                    if (client.isConnected()) {
                        futures.put(deviceId, client.sendMessage(message));
                    }
                });

        return CompletableFuture.allOf(futures.values().toArray(new CompletableFuture[0]))
                .thenApply(v -> {
                    Map<String, Boolean> results = new HashMap<>();
                    futures.forEach((deviceId, future) -> {
                        try {
                            future.get();
                            results.put(deviceId, true);
                        } catch (Exception e) {
                            results.put(deviceId, false);
                            logger.error("设备消息发送失败: {}", deviceId, e);
                        }
                    });
                    return results;
                });
    }

    /**
     * 按协议类型获取客户端
     */
    public static List<IDeviceClient> getClientsByProtocol(ProtocolType protocolType) {
        return clients.values().stream()
                .filter(client -> client.getProtocolType() == protocolType)
                .collect(Collectors.toList());
    }

    /**
     * 重连所有断开的客户端
     */
    public static CompletableFuture<Void> reconnectAll() {
        List<CompletableFuture<Void>> reconnectFutures = new ArrayList<>();

        clients.entrySet().forEach(entry -> {
            String deviceId = entry.getKey();
            IDeviceClient client = entry.getValue();

            if (!client.isConnected()) {
                logger.info("重连设备: {}", deviceId);
                CompletableFuture<Void> reconnectFuture = client.connect()
                        .whenComplete((result, ex) -> {
                            if (ex != null) {
                                logger.error("设备重连失败: {}", deviceId, ex);
                            } else {
                                logger.info("设备重连成功: {}", deviceId);
                            }
                        });
                reconnectFutures.add(reconnectFuture);
            }
        });

        return CompletableFuture.allOf(reconnectFutures.toArray(new CompletableFuture[0]));
    }

    /**
     * 启动健康检查
     */
    public static void startHealthCheck() {
        scheduler.scheduleWithFixedDelay(() -> {
            try {
                logger.debug("执行健康检查, 客户端数量: {}", clients.size());

                clients.entrySet().forEach(entry -> {
                    String deviceId = entry.getKey();
                    IDeviceClient client = entry.getValue();

                    if (!client.isConnected()) {
                        logger.warn("检测到设备离线: {}", deviceId);
                        // TODO 自动重连逻辑
                    }
                });

            } catch (Exception e) {
                logger.error("健康检查异常", e);
            }
        }, 30, 30, TimeUnit.SECONDS);
    }

    /**
     * 关闭管理器
     */
    public static CompletableFuture<Void> shutdown() {
        scheduler.shutdown();

        List<CompletableFuture<Void>> disconnectFutures = clients.values().stream()
                .map(IDeviceClient::disconnect)
                .collect(Collectors.toList());

        return CompletableFuture.allOf(disconnectFutures.toArray(new CompletableFuture[0]))
                .thenRun(() -> {
                    clients.clear();
                    logger.info("多客户端管理器已关闭");
                });
    }

    /**
     * 获取客户端数量
     */
    public static int getClientCount() {
        return clients.size();
    }

    /**
     * 获取已连接客户端数量
     */
    public static long getConnectedClientCount() {
        return clients.values().stream()
                .mapToLong(client -> client.isConnected() ? 1 : 0)
                .sum();
    }
}
