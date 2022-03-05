package com.strawberry.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class ChatClient {
    public void startClient(String name) throws IOException {
        // 1. 连接服务器
        SocketChannel socketChannel =
                SocketChannel.open(new InetSocketAddress("127.0.0.1", 8000));

        // 2. 接收服务器响应数据
        Selector selector = Selector.open();
        socketChannel.configureBlocking(false);
        socketChannel.register(selector, SelectionKey.OP_READ);

        // 创建线程
        new Thread(new ClientThread(selector)).start();

        // 3. 向服务器端发送消息
        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNextLine()) {
            String msg = scanner.nextLine();
            if (msg.length() > 0) {
                socketChannel.write(StandardCharsets.UTF_8.encode(name + " : " + msg));
            }
        }
    }
}
