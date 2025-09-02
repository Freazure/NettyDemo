package org.example.nettydemo.demos.netty.common.util;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.example.nettydemo.demos.netty.protocol.hikcaller.message.EventNotification;

/**
 * <p>Project: NettyDemo - XmlMessageParser</p>
 * <p>Powered by szl On 2025-08-01 10:24:53</p>
 * <p>Description: XML消息解析工具类 </p>
 *
 * @author szl
 * @version 1.0
 * @since 1.8
 */
public class XmlMessageParser {

    /**
     * 解析事件通知
     */
    public static EventNotification parseEventNotification(String xml) {
        try {
            Document doc = DocumentHelper.parseText(xml);
            Element root = doc.getRootElement();

            EventNotification event = new EventNotification();
            event.setDevType(root.elementText("dev_type"));
            event.setDevId(root.elementText("dev_id"));
            event.setEventId(Integer.parseInt(root.elementText("event_id")));
            event.setPara0(root.elementText("para_0"));
            event.setPara1(root.elementText("para_1"));
            event.setPara2(root.elementText("para_2"));

            return event;
        } catch (Exception e) {
            throw new RuntimeException("解析事件通知失败", e);
        }
    }
}
