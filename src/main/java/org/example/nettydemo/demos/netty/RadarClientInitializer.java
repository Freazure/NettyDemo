package org.example.nettydemo.demos.netty;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.FixedLengthFrameDecoder;

/**
 * <p>Project: NettyDemo - RadarClientInitializer</p>
 * <p>Powered by szl On 2025-03-14 16:27:25</p>
 * <p>Description:
 *
 * @author szl
 * @version 1.0
 * @since 1.8
 */
public class RadarClientInitializer extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        // 添加编解码器（如果需要）
        pipeline.addLast(new FixedLengthFrameDecoder(5));
        pipeline.addLast();
        // 为每个连接创建新的 ChannelHandler 实例
        pipeline.addLast(new RadarClientHandler());
    }
}
