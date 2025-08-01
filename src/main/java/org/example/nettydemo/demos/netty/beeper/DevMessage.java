package org.example.nettydemo.demos.netty.beeper;

import java.nio.charset.StandardCharsets;

/**
 * <p>Project: NettyDemo - DevMessage</p>
 * <p>Powered by szl On 2025-08-01 10:14:52</p>
 * <p>Description: 消息类 </p>
 *
 * @author szl
 * @version 1.0
 * @since 1.8
 */
public class DevMessage {
    private DevSdkMsgHead header;
    private String body;

    public DevMessage(DevSdkMsgHead header, String body) {
        this.header = header;
        this.body = body;

        // 更新消息长度
        if (body != null) {
            int bodyLen = body.getBytes(StandardCharsets.UTF_8).length + 1; // +1 for null terminator
            header.setMsgLen(DevSdkMsgHead.HEADER_SIZE + bodyLen);
        } else {
            header.setMsgLen(DevSdkMsgHead.HEADER_SIZE);
        }
    }

    public DevSdkMsgHead getHeader() { return header; }
    public String getBody() { return body; }
    public void setBody(String body) {
        this.body = body;
        // 更新消息长度
        if (body != null) {
            int bodyLen = body.getBytes(StandardCharsets.UTF_8).length + 1;
            header.setMsgLen(DevSdkMsgHead.HEADER_SIZE + bodyLen);
        } else {
            header.setMsgLen(DevSdkMsgHead.HEADER_SIZE);
        }
    }
}
