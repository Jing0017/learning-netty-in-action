package com.yiminilife.echoserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created on 2020/7/16.
 *
 * @author yanjing
 */
public class PlainEchoServer {

    //#1 Bind server to port
    //#2 Block until new client connection is accepted
    //#3 Create new thread to handle client connection
    //#4 Read data from client and write it back
    //#5 Start thread
    public void serve(int port) throws IOException{
        final ServerSocket socket = new ServerSocket(port); //#1
        try {
            while (true){
                final Socket clientSocket = socket.accept();//#2
                System.out.printf("Accepted connection from%s%n", clientSocket);

                new Thread(() -> { //#3
                    try {
                        BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                        PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true);
                        while (true){ //#4
                            writer.println(reader.readLine());
                            writer.flush();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        try {
                            clientSocket.close();
                        } catch (IOException ioException) {

                        }
                    }
                }).start();//#5
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
