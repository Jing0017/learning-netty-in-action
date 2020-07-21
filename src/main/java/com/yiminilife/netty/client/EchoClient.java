package com.yiminilife.netty.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.net.InetSocketAddress;

/**
 * Created on 2020/7/21.
 *
 * @author yanjing
 */
public class EchoClient {

    private final String host;

    private final int port;

    public EchoClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void start() throws Exception {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap(); // Create bootstrap for client
            b.group(group) //Specify EventLoopGroup to handle client events. NioEventLoopGroup is used, as the NIO-Transport  should be used
                    .channel(NioSocketChannel.class) //Specify channel type; use correct one for NIO-Transport
                    .remoteAddress(new InetSocketAddress(host, port)) //Set InetSocketAddress to which client connects
                    .handler(new ChannelInitializer<SocketChannel>() { // Specify ChannelHandler, using ChannelInitializer, called once connection established and channel created
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new EchoClientHandler()); // Add EchoClientHandler to ChannelPipeline that belongs to channel. ChannelPipeline holds all ChannelHandlers of channel
                        }
                    });
            ChannelFuture f = b.connect().sync(); // Connect client to remote peer; wait until sync() completes connect completes
            f.channel().closeFuture().sync(); //Wait until ClientChannel closes. This will block.
        } finally {
            group.shutdownGracefully().sync(); // Shut down bootstrap and thread pools; release all resources
        }
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.err.println(
                    "Usage: " + EchoClient.class.getSimpleName() +
                            " <host> <port>");
            return;
        }
        // Parse options.
        final String host = args[0];
        final int port = Integer.parseInt(args[1]);
        new EchoClient(host, port).start();
//        new EchoClient("127.0.0.1", 8080).start();
    }

    // A Bootstrap instance is created to bootstrap the client.
    // The NioEventLoopGroup instance is created and assigned to handle the event
    //processing, such as creating new connections, receiving data, writing data, and so on.
    // The remote InetSocketAddress to which the client will connect is specified.
    // A handler is set that will be executed once the connection is established.
    // After everything is set up, the ServerBootstrap.connect() method is called to
    //connect to the remote peer (the echo-server in our case).
}
