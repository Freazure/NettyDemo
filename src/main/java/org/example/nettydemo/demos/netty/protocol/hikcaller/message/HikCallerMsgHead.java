package org.example.nettydemo.demos.netty.protocol.hikcaller.message;

import lombok.Data;

/**
 * <p>Project: NettyDemo - DevSdkMsgHead</p>
 * <p>Powered by Freazure On 2025-07-31 23:31:33</p>
 * <p>Description: 呼叫器/控制器协议消息头结构 </p>
 *
 * @author Freazure [freazure@163.com]
 * @version 1.0
 * @since 1.8
 */
@Data
public class HikCallerMsgHead {
    public static final int HEADER_SIZE = 28; // 协议头长度
    public static final byte[] MAGIC = {0x48,0x4b,0x4d,0x56}; // 固定魔术字

    private byte[] magic = MAGIC;           // 固定为HKMV
    private int msgLen;                     // 协议头与消息体的长度
    private int msgType;                    // 消息类型
    private int msgSeq;                     // 消息序号
    private int crc32;                      // 数据校验
    private int encryptId;                  // 加密ID
    private byte version = 1;               // 协议版本号
    private byte enc = 0;                   // 是否加密
    private byte[] res = new byte[2];       // 保留字段

    public HikCallerMsgHead(int msgType, int msgLen) {
        this.msgType = msgType;
        this.msgLen = msgLen;
        this.msgSeq = 0;
        this.crc32 = 0;
        this.encryptId = 0;
    }

}
