package com.yiminilife.netty.server;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;

/**
 * Created on 2020/7/21.
 *
 * @author yanjing
 */
@ChannelHandler.Sharable // Annotate with @Sharable to share between channels
public class EchoServerHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ByteBuf in = (ByteBuf) msg;
        System.out.println(
                "Server received: " + in.toString(CharsetUtil.UTF_8));
        ctx.write(in); // Write the received messages back . Be aware that this will not "flush" the messages to the remote peer yet.
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        // Flush all previous written messages (that are pending) to the remote peer, and close the channel
        //after the operation is complete.
        ctx.writeAndFlush(Unpooled.EMPTY_BUFFER)
                .addListener(ChannelFutureListener.CLOSE);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace(); // Log exception
        ctx.close(); //Close channel on exception
    }
}
