package org.example.nettydemo.demos.netty.client;

import org.example.nettydemo.demos.netty.common.enums.ProtocolType;
import org.example.nettydemo.demos.netty.message.DeviceInfo;

import java.util.concurrent.CompletableFuture;

/**
 * <p>Project: NettyDemo - BaseClient</p>
 * <p>Powered by szl On 2025-08-01 17:35:04</p>
 * <p>Description:
 *
 * @author szl
 * @version 1.0
 * @since 1.8
 */
public interface IDeviceClient {
    /**
     * 连接设备
     */
    CompletableFuture<Void> connect();

    /**
     * 断开连接
     */
    CompletableFuture<Void> disconnect();

    /**
     * 发送消息
     */
    CompletableFuture<Void> sendMessage(Object message);

    /**
     * 检查连接状态
     */
    boolean isConnected();

    /**
     * 获取设备信息
     */
    DeviceInfo getDeviceInfo();

    /**
     * 获取协议类型
     */
    ProtocolType getProtocolType();
}
