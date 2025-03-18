package org.example.nettydemo.demos.netty.unuse;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.FixedLengthFrameDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.concurrent.TimeUnit;


/**
 * 客户端
 */
@Component
@Slf4j
public class client{

    private static final String ip = "192.168.80.6";
    private static final int port = 6008;


    Bootstrap bootstrap = new Bootstrap();



    public void connect(int port, String host) throws Exception{

        /**
         * 客户端的NIO线程组
         *
         */
        EventLoopGroup group = new NioEventLoopGroup();

        try {
            /**
             * Bootstrap 是一个启动NIO服务的辅助启动类 客户端的
             */
            /**
             * 设置group
             */
            bootstrap = bootstrap.group(group);
            /**
             * 关联客户端通道
             */
            bootstrap = bootstrap.channel(NioSocketChannel.class).option(ChannelOption.TCP_NODELAY, true);
            /**
             * 设置 I/O处理类,主要用于网络I/O事件，记录日志，编码、解码消息
             */
            bootstrap = bootstrap.handler(new ServerHandlerInit());

            System.out.println("netty client start success!");

            /**
             * 连接服务端
             */
            ChannelFuture f = bootstrap.connect(host, port).sync();
            //通常需要写不断重连服务端
            f.addListener((ChannelFutureListener) future -> {
                if (!future.isSuccess()) {
                    //重连交给后端线程执行
                    future.channel().eventLoop().schedule(() -> {
                        log.info("重连服务端...");
                        try {
                            connect(port,ip);
                        } catch (Exception e) {
                            //                        e.printStackTrace();
                            log.error("连接失败。。。");
                        }
                    }, 3000, TimeUnit.MILLISECONDS);
                } else {
                    log.info("服务端连接成功...");
                }
            });
            /**
             * 等待连接端口关闭
             */
            f.channel().closeFuture().sync();

        } finally {
            /**
             * 退出，释放资源
             */
            group.shutdownGracefully();
        }
    }

    /**
     * 通道初始化
     */
    static class ServerHandlerInit extends ChannelInitializer<SocketChannel> {
        @Override
        protected void initChannel(SocketChannel ch) throws Exception {
            ChannelPipeline pipeline = ch.pipeline();
            // 解码出具体的数据类型
            //该篇对解码器和编码器进行了详细说明，推荐阅读https://blog.csdn.net/tang_huan_11/article/details/133853786
//            pipeline.addLast(new MyDecoder());
            pipeline.addLast(new FixedLengthFrameDecoder(5)); // 固定长度 1097 字节
//            pipeline.addLast(new RadarDataDecoder()); // 自定义解码器
            pipeline.addLast(new handler());//handler类是自己写的处理类
//            pipeline.addLast("encoder", new StringEncoder());
        }
    }

//    @PostConstruct
//    //这个方法在服务启动时就会执行，即服务启动后则客户端就启动了
//    public void afterPropertiesSet() throws Exception {
//        Thread thread = new Thread(() -> {
//            try {
//                connect(port,ip);//TCP工具做服务端进行测试的时候的端口和ip
//            } catch (Exception e) {
//                throw new RuntimeException(e);
//            }
//        });
//        thread.start();
//    }
//
//    @PreDestroy
//    public void destroy() throws Exception {
//        log.info("客户端关闭");
//        bootstrap.group().shutdownGracefully();
//    }
}
