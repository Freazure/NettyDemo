package org.example.nettydemo.demos.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * <p>Project: NettyDemo - RadarClientHandler</p>
 * <p>Powered by szl On 2025-03-14 16:30:45</p>
 * <p>Description:
 *
 * @author szl
 * @version 1.0
 * @since 1.8
 */
@Component
@Slf4j
public class RadarClientHandler extends ChannelInboundHandlerAdapter {

    public static RadarClientHandler radarClientHandler;

    private static final Logger radarLogger = LoggerFactory.getLogger("RadarLogger");

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Autowired
    private RadarProcessor radarProcessor;

    @PostConstruct
    //因为是new出来的handler,没有托管给spring容器,所以要先初始化,否则autowired失效
    public void init() {
        radarClientHandler = this;
    }

    /**
     * 从服务端收到新的数据时，这个方法会在收到消息时被调用
     * 这里写收到服务端的数据之后要做的处理，通常有数据类型转换，数据解析
     * @param ctx
     * @param msg
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        InetSocketAddress insocket = (InetSocketAddress) ctx.channel().remoteAddress();
        String clientIp = insocket.getAddress().getHostAddress();
        String clientPort = String.valueOf(insocket.getPort());
        ByteBuf buf = (ByteBuf) msg;
        try {
            // 将buf写入到一个字节数组中
            byte[] bytes = new byte[buf.readableBytes()];
            buf.readBytes(bytes);
            executorService.execute(() -> {
                radarClientHandler.radarProcessor.process(clientIp + ":" + clientPort, bytes);
            });
//            radarLogger.info("收到设备[{}]原消息: {}", clientIp+":"+clientPort, sb.toString().trim());
        } finally {
            // 确保释放 ByteBuf
            buf.release();
        }
    }

    /**
     * 从服务端收到新的数据、读取完成时调用
     *
     * @param ctx
     */
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws IOException
    {
//        log.info("channelReadComplete");
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
        RadarClientManager.removeRadarDevice(clientIp+":"+clientPort);
        radarLogger.info("设备[{}]异常: ", clientIp+":"+clientPort, cause);
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
        RadarClientManager.addRadarDevice(clientIp+":"+clientPort, ctx.channel());
        radarLogger.info("设备连接成功:{}, 当前设备数量：{}", clientIp+":"+clientPort, RadarClientManager.getRadarDeviceChannelCount());

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
        RadarClientManager.removeRadarDevice(clientIp+":"+clientPort);
        ctx.close();
        radarLogger.info("设备断开:{}, 当前设备数量：{}", clientIp+":"+clientPort, RadarClientManager.getRadarDeviceChannelCount());
    }
}
