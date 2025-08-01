package org.example.nettydemo.demos.netty.message;

import lombok.Data;

/**
 * <p>Project: NettyDemo - EventNotification</p>
 * <p>Powered by szl On 2025-08-01 16:40:56</p>
 * <p>Description: 按钮事件通知 </p>
 *
 * @author szl
 * @version 1.0
 * @since 1.8
 */
@Data
public class EventNotification {

    private String devType;

    private String devId;

    private int eventId;

    private String para0;

    private String para1;

    private String para2;

    @Override
    public String toString() {
        return String.format("事件通知[设备ID:%s, 事件ID:%d, 参数:%s,%s,%s]",
                devId, eventId, para0, para1, para2);
    }
}
