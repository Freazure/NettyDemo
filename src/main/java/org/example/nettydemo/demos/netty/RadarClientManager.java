package org.example.nettydemo.demos.netty;

import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>Project: NettyDemo - RadarClientManager</p>
 * <p>Powered by szl On 2025-03-14 16:38:56</p>
 * <p>Description:
 *
 * @author szl
 * @version 1.0
 * @since 1.8
 */
@Component
@Slf4j
public class RadarClientManager {

    // 存储设备 ip:port -> RadarClient 映射
    private static final Map<String, RadarClient> CLIENTS = new ConcurrentHashMap<>();

    public static Map<String, RadarClient> getClients() {
        return CLIENTS;
    }

    public static int getRadarClientCount() {
        return CLIENTS.size();
    }

    public static RadarClient getRadarClient(String radarId) {
        return CLIENTS.get(radarId);
    }

    public static void removeRadarClient(String radarId) {
        RadarClient client = CLIENTS.get(radarId);
        if (client != null) {
            client.shutdown();
            CLIENTS.remove(radarId);
        }
    }

    public static void addRadarClient(RadarDevice radarDevice) {
        RadarClient client = new RadarClient(radarDevice.getIp(), radarDevice.getPort());
        client.start();
        CLIENTS.put(radarDevice.getRadarId(),client);
    }


    public static void startAll(List<RadarDevice> radarDeviceList) {
        // 对每个雷达地址创建并启动一个RadarClient
        for (RadarDevice radarDevice : radarDeviceList) {
            addRadarClient(radarDevice);
        }
    }

}
