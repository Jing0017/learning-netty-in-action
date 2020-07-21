package com.yiminilife.jdk.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Set;

/**
 * Created on 2020/7/17.
 *
 * @author yanjing
 */
public class PlainNioEchoServer {
    //    #1 Bind server to port
    //    #2 Register the channel with the selector to be interested in new Client connections that get accepted
    //    #3 Block until something is selected
    //    #4 Get all SelectedKey instances
    //    #5 Remove the SelectedKey from the iterator
    //    #6 Accept the client connection
    //    #7 Register connection to selector and set ByteBuffer
    //    #8 Check for SelectedKey for read
    //    #9 Read data to ByteBuffer
    //    #10 Check for SelectedKey for write
    //    #11 Write data from ByteBuffer to channel
    public void serve(int port) throws IOException {
        System.out.println("Listening for connections on port " + port);

        ServerSocketChannel serverChannel = ServerSocketChannel.open();
        ServerSocket ss = serverChannel.socket();
        InetSocketAddress address = new InetSocketAddress(port);
        ss.bind(address); //#1
        serverChannel.configureBlocking(false);
        Selector selector = Selector.open();
        serverChannel.register(selector, SelectionKey.OP_ACCEPT); //#2

        while(true){
            try {
                selector.select(); //#3
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }

            Set<SelectionKey> readyKeys = selector.selectedKeys(); //#4
            Iterator<SelectionKey> iterator = readyKeys.iterator();
            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                iterator.remove(); //5

                try {
                    if (key.isAcceptable()) {
                        ServerSocketChannel server = (ServerSocketChannel) key.channel();
                        SocketChannel client = server.accept(); //#6
                        System.out.println("Accepted connection from " + client);
                        client.configureBlocking(false);
                        //#7
                        client.register(selector, SelectionKey.OP_WRITE | SelectionKey.OP_READ, ByteBuffer.allocate(100));
                    }

                    //#8
                    if (key.isReadable()) {
                        SocketChannel client = (SocketChannel) key.channel();
                        ByteBuffer output = (ByteBuffer) key.attachment();
                        client.read(output); //#9
                    }

                    if (key.isWritable()) { //#10
                        SocketChannel client = (SocketChannel) key.channel();
                        ByteBuffer output = (ByteBuffer) key.attachment();
                        output.flip();
                        client.write(output); //#11
                        output.compact();
                    }
                } catch (IOException e) {
                    key.cancel();
                    try {
                        key.channel().close();
                    } catch (IOException cex) {
                    }
                }
            }
        }

    }
}
