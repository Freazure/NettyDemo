package org.example.nettydemo.demos.netty.client;

import org.example.nettydemo.demos.netty.common.enums.ProtocolType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>Project: NettyDemo - ClientExample</p>
 * <p>Powered by szl On 2025-08-04 11:35:38</p>
 * <p>Description:
 *
 * @author szl
 * @version 1.0
 * @since 1.8
 */
public class ClientExample {

    private static final Logger log = LoggerFactory.getLogger(ClientExample.class);

    public static void main(String[] args) {

        ClientManager.startHealthCheck();
        ClientManager.addClient("device1", "127.0.0.1", 9001, ProtocolType.HIKVISION_CALLER);
        ClientManager.addClient("device2", "127.0.0.1", 9002, ProtocolType.HIKVISION_CALLER);
        ClientManager.addClient("device3", "127.0.0.1", 9003, ProtocolType.RADAR);
        ClientManager.addClient("device4", "127.0.0.1", 9004, ProtocolType.RADAR);

    }
}
