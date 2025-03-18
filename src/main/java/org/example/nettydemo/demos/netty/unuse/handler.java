package org.example.nettydemo.demos.netty.unuse;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * 处理类
 */
@Service
@Slf4j
public class handler extends ChannelInboundHandlerAdapter {

    /**
     * 从服务端收到新的数据时，这个方法会在收到消息时被调用
     * 这里写收到服务端的数据之后要做的处理，通常有数据类型转换，数据解析
     * @param ctx
     * @param msg
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception, IOException
    {
        InetSocketAddress insocket = (InetSocketAddress) ctx.channel().remoteAddress();
//        Thread.sleep(1000);
        String clientIp = insocket.getAddress().getHostAddress();
        String clientPort = String.valueOf(insocket.getPort());
        ByteBuf buf = (ByteBuf) msg;
        // 将buf写入到一个字节数组中
        byte[] bytes = new byte[buf.readableBytes()];
        buf.readBytes(bytes);
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("0x%02X ", b));
        }
        log.info("收到设备[{}]原消息: {}", clientIp+":"+clientPort, sb.toString().trim());
    }

    /**
     * 从服务端收到新的数据、读取完成时调用
     *
     * @param ctx
     */
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws IOException
    {
        log.info("channelReadComplete");
    }

    /**
     * 当出现 Throwable 对象才会被调用，即当 Netty 由于 IO 错误或者处理器在处理事件时抛出的异常时
     *
     * @param ctx
     * @param cause
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws IOException
    {
        InetSocketAddress insocket = (InetSocketAddress) ctx.channel().remoteAddress();
        String clientIp = insocket.getAddress().getHostAddress();
        String clientPort = String.valueOf(insocket.getPort());
        DeviceChannelManager.removeDevice(clientIp+":"+clientPort);
        log.info("设备[{}]异常: ", clientIp+":"+clientPort, cause);
        ctx.close();//抛出异常，断开与客户端的连接
    }

    /**
     * 客户端与服务端第一次建立连接时 执行
     *
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception, IOException
    {
        super.channelActive(ctx);
        InetSocketAddress insocket = (InetSocketAddress) ctx.channel().remoteAddress();
        String clientIp = insocket.getAddress().getHostAddress();
        String clientPort = String.valueOf(insocket.getPort());
        DeviceChannelManager.addDevice(clientIp+":"+clientPort, ctx.channel());
        log.info("设备连接成功:{}, {}", clientIp+":"+clientPort, ctx.name());
        log.info("当前设备数量：{}", DeviceChannelManager.getDeviceChannelCount());

    }

    /**
     * 客户端与服务端 断连时 执行
     *
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception, IOException
    {
        super.channelInactive(ctx);
        InetSocketAddress insocket = (InetSocketAddress) ctx.channel().remoteAddress();
        String clientIp = insocket.getAddress().getHostAddress();
        String clientPort = String.valueOf(insocket.getPort());
        DeviceChannelManager.removeDevice(clientIp+":"+clientPort);
        ctx.close();
        log.info("设备断开:{}, {}", clientIp+":"+clientPort, ctx.name());
        log.info("当前设备数量：{}", DeviceChannelManager.getDeviceChannelCount());
    }
}
