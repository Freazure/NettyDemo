package org.example.nettydemo.demos.netty.common.util;

/**
 * <p>Project: NettyDemo - XmlMessageBuilder</p>
 * <p>Powered by szl On 2025-08-01 10:18:13</p>
 * <p>Description: XML消息构建工具类 </p>
 *
 * @author szl
 * @version 1.0
 * @since 1.8
 */
public class XmlMessageBuilder {

    /**
     * 构建IO输出控制消息
     * @param pinIdx 引脚编号（软件从0开始）
     * @param mode 输出模式（0-常态，1-闪烁）
     * @param levCtrl 输出电平（0-低电平，1-高电平）
     * @param time 输出时间（ms）
     * @param interval 周期时间（ms，闪烁时使用）
     * @return XML字符串
     */
    public static String buildIoControlMessage(int pinIdx, int mode, int levCtrl, int time, int interval) {
        return String.format(
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                        "<Message>" +
                        "<dev_type>0</dev_type>" +
                        "<io_ctrl>" +
                        "<cmd pin_idx=\"%d\" mode=\"%d\" lev_ctrl=\"%d\" time=\"%d\" interval=\"%d\"/>" +
                        "</io_ctrl>" +
                        "</Message>",
                pinIdx, mode, levCtrl, time, interval
        );
    }

    /**
     * 构建按钮配置消息
     */
    public static String buildButtonConfigMessage(int vIdx, String serverIp, int serverPort, String para0, String para1, String para2) {
        return String.format(
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                        "<Message>" +
                        "<btns_conf>" +
                        "<btn_cfg btn_type=\"0\" v_idx=\"%d\" pin_idx=\"%d\" trigger=\"1\" filter_time=\"5000\" notify_proto=\"1\">" +
                        "<http_cfg ip=\"\" port=\"\" resent_cnt=\"\" time_out=\"\" para_0=\"\" para_1=\"\" para_2=\"\"/>" +
                        "<sdk_cfg ip=\"%s\" port=\"%d\" para_0=\"%s\" para_1=\"%s\" para_2=\"%s\"/>" +
                        "<light_cfg out_idx=\"0\" mode=\"0\" lev_ctrl=\"0\" interval=\"0\" time=\"0\"/>" +
                        "<feedback_cfg>" +
                        "<io_out_cfg out_idx=\"0\" mode=\"0\" lev_ctrl=\"0\" interval=\"0\" time=\"0\" trigger_res=\"0\"/>" +
                        "<io_out_cfg out_idx=\"0\" mode=\"0\" lev_ctrl=\"0\" interval=\"0\" time=\"0\" trigger_res=\"1\"/>" +
                        "</feedback_cfg>" +
                        "</btn_cfg>" +
                        "</btns_conf>" +
                        "</Message>",
                vIdx, vIdx, serverIp, serverPort, para0, para1, para2
        );
    }

    /**
     * 构建设置系统时间消息
     */
    public static String buildSetSystemTimeMessage(String dateTime) {
        return String.format(
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                        "<Message>" +
                        "<system_time>%s</system_time>" +
                        "</Message>",
                dateTime
        );
    }

    /**
     * 构建事件处理结果通知消息
     */
    public static String buildEventProcessResultMessage(int eventId, int procRes) {
        return String.format(
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                        "<Message>" +
                        "<event_id>%d</event_id>" +
                        "<proc_res>%d</proc_res>" +
                        "</Message>",
                eventId, procRes
        );
    }
}
