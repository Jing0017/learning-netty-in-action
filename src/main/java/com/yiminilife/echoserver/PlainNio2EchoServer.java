package com.yiminilife.echoserver;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.CountDownLatch;

/**
 * Created on 2020/7/17.
 *
 * @author yanjing
 */
public class PlainNio2EchoServer {

    //        #1 Bind Server to port
    //        #2 Start to accept new Client connections. Once one is accepted the CompletionHandler will get called.
    //        #3 Again accept new Client connections
    //        #4 Trigger a read operation on the Channel, the given CompletionHandler will be notified once something was read
    //        #5 Close the socket on error
    //        #6 Trigger a write operation on the Channel, the given CompletionHandler will be notified once something was written
    //        #7 Trigger again a write operation if something is left in the ByteBuffer
    //        #8 Trigger a read operation on the Channel, the given CompletionHandler will be notified once something was read
    public void serve(int port) throws IOException {
        System.out.println("Listening for connections on port " + port);
        final AsynchronousServerSocketChannel serverChannel = AsynchronousServerSocketChannel.open();
        InetSocketAddress address = new InetSocketAddress(port);
        serverChannel.bind(address); //#1
        final CountDownLatch latch = new CountDownLatch(1);
        serverChannel.accept(null, new CompletionHandler<AsynchronousSocketChannel, Object>() { //#2
            @Override
            public void completed(AsynchronousSocketChannel channel, Object attachment) {
                serverChannel.accept(null, this); //#3
                ByteBuffer buffer = ByteBuffer.allocate(100);
                channel.read(buffer, buffer, new EchoCompletionHandler(channel)); //#4
            }

            @Override
            public void failed(Throwable exc, Object attachment) {
                try {
                    serverChannel.close(); //#5
                } catch (IOException e) {
                    // ingnore on close
                } finally {
                    latch.countDown();
                }
            }
        });
        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static final class EchoCompletionHandler implements CompletionHandler<Integer, ByteBuffer> {
        private final AsynchronousSocketChannel channel;
        EchoCompletionHandler(AsynchronousSocketChannel channel) {
            this.channel = channel;
        }

        @Override
        public void completed(Integer result, ByteBuffer buffer) {
            buffer.flip();
            channel.write(buffer, buffer, new CompletionHandler<Integer, ByteBuffer>() { //#6
                @Override
                public void completed(Integer result, ByteBuffer buffer) {
                    if (buffer.hasRemaining()) {
                        channel.write(buffer, buffer, this); //#7
                    } else {
                        buffer.compact();
                        channel.read(buffer, buffer, this); //#8
                    }
                }
                @Override
                public void failed(Throwable exc, ByteBuffer attachment) {
                    try {
                        channel.close();
                    } catch (IOException e) {
                        // ingnore on close
                    }
                }
            });
        }

        @Override
        public void failed(Throwable exc, ByteBuffer attachment) {
            try {
                channel.close();
            } catch (IOException e) {
                // ingnore on close
            }
        }
    }
}
