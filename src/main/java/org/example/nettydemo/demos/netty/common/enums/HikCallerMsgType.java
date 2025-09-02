package org.example.nettydemo.demos.netty.common.enums;

/**
 * <p>Project: NettyDemo - DevMsgType</p>
 * <p>Powered by szl On 2025-08-01 10:13:54</p>
 * <p>Description: 消息类型枚举 </p>
 *
 * @author szl
 * @version 1.0
 * @since 1.8
 */
public enum HikCallerMsgType {
    DEV_MSG_TYPE_LOGIN(0),                    // 登录请求
    DEV_MSG_TYPE_REG(1),                      // 注册请求
    DEV_MSG_GET_CAP_INFO(2),                  // 获取设备能力
    DEV_MSG_GET_NET_CFG(3),                   // 获取网络配置
    DEV_MSG_SET_NET_CFG(4),                   // 设置网络配置
    DEV_MSG_GET_ALARM_CFG(5),                 // 获取告警配置信息
    DEV_MSG_SET_ALARM_CFG(6),                 // 设置告警配置信息
    DEV_MSG_GET_FIRMWARE_VERSION(7),          // 获取固件版本信息
    DEV_MSG_TYPE_IOOUT_CTRL(8),               // IO输出状态控制
    DEV_MSG_UPLOAD_FILE(9),                   // 文件导入请求
    DEV_MSG_DOWNLOAD_FILE(10),                // 文件导出请求
    DEV_MSG_GET_FILELIST(11),                 // 获取文件列表信息
    DEV_MSG_UPGRADE(16),                      // 升级固件
    DEV_MSG_SET_MAC(17),                      // 设置设备有线网卡MAC地址
    DEV_MSG_SET_SYSTEM_TIME(18),              // 设置系统时间
    DEV_MSG_GET_SYSTEM_TIME(19),              // 获取系统时间
    DEV_MSG_WIFI_TEST_RES(20),                // 获取无线测试结果
    DEV_MSG_GET_DEV_TYPE(21),                 // 获取设备类型
    DEV_MSG_SET_DEV_TYPE(22),                 // 设置设备类型

    // 呼叫器/控制器命令
    DEV_MSG_TYPE_GET_IO_STAT(101),          // 获得输入IO状态
    DEV_MSG_TYPE_GET_BTNS_CFG(102),         // 获得按键/输入IO配置参数
    DEV_MSG_TYPE_SET_BTNS_CFG(103),         // 设置按键/输入IO配置参数
    DEV_MSG_TYPE_EVENT_NOTIFY(260),         // 输入IO检测到高电平/按钮按下通知上层平台
    DEV_MSG_TYPE_EVENT_PROC_RES(105),       // 平台对上报事件处理结果通知

    // 串口转发设置
    DEV_MSG_GET_UART_ATTR_CFG(601),         // 获取串口设置
    DEV_MSG_SET_UART_ATTR_CFG(602);         // 设置串口属性

    private final int value;

    HikCallerMsgType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static HikCallerMsgType fromValue(int value) {
        for (HikCallerMsgType type : values()) {
            if (type.value == value) {
                return type;
            }
        }
        return null;
    }
}
