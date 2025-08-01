package org.example.nettydemo.demos.netty.beeper;

import sun.net.www.http.HttpClient;

import java.util.concurrent.CompletableFuture;

/**
 * <p>Project: NettyDemo - ProtocolClientWrapper</p>
 * <p>Powered by szl On 2025-08-01 17:16:27</p>
 * <p>Description: 协议客户端包装器 </p>
 *
 * @author szl
 * @version 1.0
 * @since 1.8
 */
public class ProtocolClientWrapper {
    private final ProtocolType protocolType;
    private final Object client;

    public ProtocolClientWrapper(ProtocolType protocolType, Object client) {
        this.protocolType = protocolType;
        this.client = client;
    }

    public ProtocolType getProtocolType() {
        return protocolType;
    }

    public Object getClient() {
        return client;
    }

    public boolean isConnected() {
        if (client instanceof DeviceClient) {
            return ((DeviceClient) client).isConnected();
        } else if (client instanceof ModbusTcpClient) {
            return ((ModbusTcpClient) client).isConnected();
        } else if (client instanceof HttpClient) {
            return ((HttpClient) client).isConnected();
        }
        return false;
    }

    public CompletableFuture<Void> disconnect() {
        if (client instanceof DeviceClient) {
            return ((DeviceClient) client).disconnect();
        } else if (client instanceof ModbusTcpClient) {
            return ((ModbusTcpClient) client).disconnect();
        } else if (client instanceof HttpClient) {
            return ((HttpClient) client).disconnect();
        }
        return CompletableFuture.completedFuture(null);
    }
}
