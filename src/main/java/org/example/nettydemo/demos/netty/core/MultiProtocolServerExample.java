package org.example.nettydemo.demos.netty.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * <p>Project: NettyDemo - MultiProtocolServerExample</p>
 * <p>Powered by szl On 2025-08-01 13:44:29</p>
 * <p>Description:
 *
 * @author szl
 * @version 1.0
 * @since 1.8
 */
public class MultiProtocolServerExample {
    private static final Logger logger = LoggerFactory.getLogger(MultiProtocolServerExample.class);

    public static void main(String[] args) {
        MultiProtocolServer server = new MultiProtocolServer(8000);

        try {
            // 启动服务器
            server.start().get();
            logger.info("多协议服务器启动成功");

            // 保持服务器运行
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                logger.info("正在关闭服务器...");
                try {
                    server.stop().get(5, TimeUnit.SECONDS);
                    logger.info("服务器已关闭");
                } catch (Exception e) {
                    logger.error("关闭服务器失败", e);
                }
            }));

            // 等待终止信号
            Thread.currentThread().join();

        } catch (Exception e) {
            logger.error("服务器运行失败", e);
        }
    }
}
