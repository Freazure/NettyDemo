package org.example.nettydemo.demos.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.example.nettydemo.demos.netty.unuse.TestObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.data.redis.core.RedisTemplate;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * <p>Project: NettyDemo - RadarProcessor</p>
 * <p>Powered by szl On 2025-03-17 14:05:07</p>
 * <p>Description:
 *
 * @author szl
 * @version 1.0
 * @since 1.8
 */
@Slf4j
@Component
public class RadarProcessor {

    private static final Logger radarLogger = LoggerFactory.getLogger("RadarLogger");

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private int count = 0;

    @PostConstruct
    public void afterPropertiesSet() {
        try {
            List<RadarDevice> radarDeviceList = new ArrayList<>();
            int basePort = 9000; // 基础端口
            int deviceCount = 2; // 需要模拟的设备数量

            for (int i = 0; i < deviceCount; i++) {
                int port = basePort + i;
                radarDeviceList.add(new RadarDevice(port+"","127.0.0.1", port));
            }
            RadarClientManager.startAll(radarDeviceList);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        for (RadarClient client : RadarClientManager.getClients().values()) {
            client.getChannel().eventLoop().scheduleAtFixedRate(() -> {sendSwitchCommand(client);}, 0, 1000, TimeUnit.MILLISECONDS);
        }
    }

    private void sendSwitchCommand(RadarClient client) {

        byte[] a10 = new byte[]{(byte) 0x35, (byte) 0x3A , (byte) 0x0A,(byte) 0xFF};
        byte[] a15 = new byte[]{(byte) 0x35, (byte) 0x3A , (byte) 0x0F,(byte) 0xFF};
        byte[] command = client.getToggle().getAndSet(!client.getToggle().get()) ? a10 : a15;
        ByteBuf buffer10 = null;
        buffer10 = Unpooled.buffer(command.length);
        buffer10.writeBytes(command);
        client.getChannel().writeAndFlush(buffer10);
    }

    @PreDestroy
    public void destroy() {
        for (RadarClient client : RadarClientManager.getClients().values()) {
            client.shutdown();
        }
    }

    public void process(String key, byte[] bytes) {
        try {
            TestObject testObject = new TestObject();
            testObject.setId(key);
            testObject.setData(new String(bytes));
            testObject.setCount(count++);
            testObject.setLastUpdateTime(System.currentTimeMillis());
            TestObject o = (TestObject) redisTemplate.opsForValue().get(key);
            if (Objects.nonNull(o)) {
                if (bytes[3] == 0x01) {
                    if (!"1".equals(o.getStatus())) {
                        log(key, bytes, "无货变有货");
                    }
                    testObject.setStatus("1");
                }
                else {
                    if (!"0".equals(o.getStatus())) {
                        log(key, bytes, "有货变无货");
                    }
                    testObject.setStatus("0");
                }
            }
            redisTemplate.opsForValue().set(key, testObject);
        } catch (Exception e) {
            radarLogger.error("雷达[{}]处理数据[{}]失败: ", key, bytes, e);
        }
    }

    // 定时检测离线设备
    public void checkHeartbeat(String key) {
        log.info("检测雷达[{}]心跳", key);
        long now = System.currentTimeMillis();
        TestObject o = (TestObject) redisTemplate.opsForValue().get(key);
        if (Objects.nonNull(o)) {
            if (now - o.getLastUpdateTime() > 5000) {
                radarLogger.info("雷达[{}]离线", key);
                redisTemplate.delete(key);
            }
        }
        else {
            radarLogger.info("雷达[{}]离线", key);
        }
    }

    private void log(String key, byte[] bytes, String message) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("0x%02X ", b));
        }
        radarLogger.info("收到雷达[{}]上报数据[{}]: {}", key, sb, message);
    }
}
