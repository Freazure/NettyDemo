package org.example.nettydemo.demos.netty;

import lombok.Data;

import java.util.List;

@Data
public class SendRequest {

    private String deviceId;

    private String command;

    private String ip;

    private Integer port;
    // 区域
    private Byte region;
}
