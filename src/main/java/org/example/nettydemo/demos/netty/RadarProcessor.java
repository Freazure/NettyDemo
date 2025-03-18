package org.example.nettydemo.demos.netty;

import lombok.extern.slf4j.Slf4j;
import org.example.nettydemo.demos.netty.unuse.TestObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Objects;

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

    public void process(String key, byte[] bytes) {
        try {
            TestObject testObject = new TestObject();
            testObject.setId(key);
            testObject.setData(new String(bytes));
            testObject.setCount(count++);
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

    private void log(String key, byte[] bytes, String message) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("0x%02X ", b));
        }
        radarLogger.info("收到雷达[{}]上报数据[{}]: {}", key, sb, message);
    }
}
