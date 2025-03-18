package org.example.nettydemo.demos.netty;

import lombok.Data;

/**
 * <p>Project: NettyDemo - RadarDevice</p>
 * <p>Powered by szl On 2025-03-14 16:43:14</p>
 * <p>Description:
 *
 * @author szl
 * @version 1.0
 * @since 1.8
 */
@Data
public class RadarDevice {

    private String radarId;

    private String ip;
    private int port;

    public RadarDevice(String id, String s, int i) {
        this.ip = s;
        this.port = i;
        this.radarId = id;
    }
}
