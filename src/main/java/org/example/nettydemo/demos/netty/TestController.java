package org.example.nettydemo.demos.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.example.nettydemo.demos.netty.unuse.DeviceChannelManager;
import org.example.nettydemo.demos.netty.unuse.client;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequestMapping("/test")
@Api(tags = "测试接口")
@CrossOrigin
public class TestController {

    // 开启雷达扫描数据， 0x01 0x02 0x03 0x04
    byte[] openScan = new byte[]{(byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04};

    byte[] openUpload = new byte[]{(byte) 0x95, (byte) 0x9A, (byte) 0xFA, (byte) 0xF5};

    // 关闭雷达扫描数据， 0x95 0x9A 0xE0 0xA4
    byte[] closeScan = new byte[]{(byte) 0x95, (byte) 0x9A, (byte) 0xE0, (byte) 0xA4};

    byte[] openUploadScan = new byte[]{(byte) 0x95, (byte) 0x9A, (byte) 0xFF, (byte) 0xF0};

    // 区域报警常开， 0x75 0x7A 0x00 0x0F
    byte[] openAlarm = new byte[]{(byte) 0x75, (byte) 0x7A, (byte) 0x00, (byte) 0x0F};
    // 区域报警常关， 0x75 0x7A 0x01 0x0F
    byte[] closeAlarm = new byte[]{(byte) 0x75, (byte) 0x7A, (byte) 0x01, (byte) 0x0F};
    // 切换区域 0x35 0x3A XX 0xFF
    byte[] changeArea = new byte[]{(byte) 0x35, (byte) 0x3A, (byte) 0x01, (byte) 0x0F};

    byte[] restart = new byte[]{(byte)0x95 ,(byte)0x9A ,(byte)0xF0 ,(byte)0xFF};

    byte[] ip = new byte[]{(byte)0x02 ,(byte)0x53 ,(byte)0x54 ,(byte)0x52};

    boolean change = false;

    @PostMapping("/addDevice")
    @ApiOperation(value = "添加设备")
    public String addDevice(@RequestBody SendRequest sendRequest) {
        RadarDevice radarDevice = new RadarDevice(sendRequest.getDeviceId(), sendRequest.getIp(), sendRequest.getPort());
        RadarClientManager.addRadarClient(radarDevice);
        return "添加设备成功";
    }

    @PostMapping("deleteDevice")
    @ApiOperation(value = "删除设备")
    public String deleteDevice(@RequestBody SendRequest sendRequest) {
        RadarClientManager.removeRadarClient(sendRequest.getDeviceId());
        return "删除设备成功";
    }


    @PostMapping("/sendCommandToDevice")
    @ApiOperation(value = "切换区域")
    public String sendCommandToDevice(@RequestBody SendRequest sendRequest) throws InterruptedException {
        // 0x35 0x3A XX 0xFF
        byte[] a15 = new byte[]{(byte) 0x35, (byte) 0x3A , (byte) 0x0F,(byte) 0xFF};
        byte[] a10 = new byte[]{(byte) 0x35, (byte) 0x3A , (byte) 0x0A,(byte) 0xFF};
        Channel channel = DeviceChannelManager.getDeviceChannel(sendRequest.getDeviceId());
            if (channel != null && channel.isActive()) {
                if (change) {
                    ByteBuf buffer15 = null;
                    buffer15 = Unpooled.buffer(a15.length);
                    buffer15.writeBytes(a15);
                    channel.writeAndFlush(buffer15);
                    log.info("指令发送到设备 {}: {}", sendRequest.getDeviceId(), buffer15);
                    change = false;
                }
                else {
                    ByteBuf buffer10 = null;
                    buffer10 = Unpooled.buffer(a10.length);
                    buffer10.writeBytes(a10);
                    channel.writeAndFlush(buffer10);
                    log.info("指令发送到设备 {}: {}", sendRequest.getDeviceId(), buffer10);
                    change = true;
                }
            } else {
                log.warn("设备 {} 不在线，无法发送指令", sendRequest.getDeviceId());
                return "设备不在线，无法发送指令";
            }
        return null;
    }
    @PostMapping("/changeArea")
    @ApiOperation(value = "发送指令到设备")
    public String changeArea(@RequestBody SendRequest sendRequest) {
        byte[] a15 = new byte[]{(byte) 0x35, (byte) 0x3A , (byte) 0x0F,(byte) 0xFF};
        byte[] a10 = new byte[]{(byte) 0x35, (byte) 0x3A , (byte) 0x0A,(byte) 0xFF};
        Channel channel = DeviceChannelManager.getDeviceChannel(sendRequest.getDeviceId());
        if (channel != null && channel.isActive()) {
            ByteBuf buffer = null;
            switch (sendRequest.getCommand()) {
                case "openScan":
                    buffer = Unpooled.buffer(openScan.length);
                    buffer.writeBytes(openScan);
                    break;
                case "closeScan":
                    buffer = Unpooled.buffer(closeScan.length);
                    buffer.writeBytes(closeScan);
                    break;
                case "openAlarm":
                    buffer = Unpooled.buffer(openAlarm.length);
                    buffer.writeBytes(openAlarm);
                    break;
                case "closeAlarm":
                    buffer = Unpooled.buffer(closeAlarm.length);
                    buffer.writeBytes(closeAlarm);
                    break;
                case "changeArea10":
                    buffer = Unpooled.buffer(a10.length);
                    buffer.writeBytes(a10);
                    break;
                case "changeArea15":
                    buffer = Unpooled.buffer(a15.length);
                    buffer.writeBytes(a15);
                    break;
                case "restart":
                    buffer = Unpooled.buffer(restart.length);
                    buffer.writeBytes(restart);
                    break;
                case "ip":
                    buffer = Unpooled.buffer(ip.length);
                    buffer.writeBytes(ip);
                    break;
                case "openUpload":
                    buffer = Unpooled.buffer(openUpload.length);
                    buffer.writeBytes(openUpload);
                    break;
                case "openUploadScan":
                    buffer = Unpooled.buffer(openUploadScan.length);
                    buffer.writeBytes(openUploadScan);
                    break;
                default:
                    log.warn("未知的指令 {}", sendRequest.getCommand());
                    return "未知的指令";
            }
            channel.writeAndFlush(buffer);
            log.info("指令发送到设备 {}: {}", sendRequest.getDeviceId(), sendRequest.getCommand());
            return "指令发送成功";
        } else {
            log.warn("设备 {} 不在线，无法发送指令", sendRequest.getDeviceId());
            return "设备不在线，无法发送指令";
        }
    }

    @PostMapping("/connectDevice")
    @ApiOperation(value = "连接设备")
    public String connectDevice(@RequestBody SendRequest sendRequest) {
        client client = new client();
        try {
            Thread thread = new Thread(() -> {
                try {
                    client.connect(sendRequest.getPort(), sendRequest.getIp());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
            thread.start();
            log.info("连接设备 {} 成功", sendRequest.getDeviceId());
            return "连接设备成功";
        } catch (Exception e) {
            log.error("连接设备 {} 失败", sendRequest.getDeviceId(), e);
            return "连接设备失败";
        }
    }
}
