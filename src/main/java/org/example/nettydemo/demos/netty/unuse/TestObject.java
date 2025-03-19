package org.example.nettydemo.demos.netty.unuse;

import lombok.Data;

import java.util.concurrent.atomic.AtomicLong;

/**
 * <p>Project: NettyDemo - TestObject</p>
 * <p>Powered by szl On 2025-03-17 14:42:26</p>
 * <p>Description:
 *
 * @author szl
 * @version 1.0
 * @since 1.8
 */
@Data
public class TestObject {
    private String id;

    private String status;

    private String message;

    private String data;

    private Integer count;

    private Long lastUpdateTime;
}
