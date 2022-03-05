package com.strawberry.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Set;

public class CharServer {

    public void startServer() throws IOException {
        // 1.创建Selector选择器
        Selector selector = Selector.open();

        // 2.创建ServerSocketChannel通道
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();

        // 3.为channel通道绑定监听窗口
        serverSocketChannel.bind(new InetSocketAddress(8000));
        serverSocketChannel.configureBlocking(false); //设置非阻塞模式

        // 4.把channel通道注册到selector选择器上
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        System.out.println("服务器已经启动成功了");

        // 5.循环 等待有新连接接入
        while (true) {
            // 获取channel数量
            int channels = selector.select();
            if (channels == 0) {
                continue;
            }

            // 获取可用的channel
            Set<SelectionKey> selectionKeys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = selectionKeys.iterator();
            while (iterator.hasNext()) {
                SelectionKey selectionKey = iterator.next();

                // Selector不会自己从已选择键集中移除SelectionKey实例。必须在处理完通道时自己移除。
                // 下次该通道变成就绪时，Selector会再次将其放入已选择键集中。
                iterator.remove();

                // 6.根据就绪状态，调用对应方法实现具体业务操作
                if (selectionKey.isAcceptable()) {
                    acceptOperator(serverSocketChannel, selector);
                }

                if (selectionKey.isReadable()) {
                    readOperator(selector, selectionKey);
                }
            }
        }
    }

    private void readOperator(Selector selector, SelectionKey selectionKey) throws IOException {
        // 1. 从SelectionKey获取已经就绪的通道
        SocketChannel socketChannel = (SocketChannel) selectionKey.channel();

        // 2. 创建buffer
        ByteBuffer buf = ByteBuffer.allocate(1024);

        // 3. 循环读取客户端消息
        int readLength = socketChannel.read(buf);
        StringBuilder msg = new StringBuilder();
        while (readLength > 0) {
            buf.flip();
            msg.append(StandardCharsets.UTF_8.decode(buf));
            buf.clear();
            readLength = socketChannel.read(buf);
        }

        // 4. 将channel再次注册到选择器上，监听可读状态
        socketChannel.register(selector, SelectionKey.OP_READ);

        // 5. 把客户端的消息，广播到其他客户端
        if (msg.length() > 0) {
            System.out.println(msg);
            castOtherClient(msg.toString(), selector, socketChannel);
        }
    }

    private void castOtherClient(String msg, Selector selector, SocketChannel socketChannel) throws IOException {
        // 1. 获取所有已经接入的客户端channel
        Set<SelectionKey> selectionKeySet = selector.keys();

        // 2. 循环向所有channel广播消息
        for(SelectionKey selectionKey : selectionKeySet) {
           Channel channel = selectionKey.channel();
           if (channel instanceof SocketChannel && channel != socketChannel) {
               ((SocketChannel) channel).write(StandardCharsets.UTF_8.encode(msg));
           }
        }
    }

    private void acceptOperator(ServerSocketChannel serverSocketChannel, Selector selector) throws IOException {
        // 1. 处理接入状态，创建socketChannel
        SocketChannel socketChannel = serverSocketChannel.accept();

        // 2. 设置socketChannel为非阻塞
        socketChannel.configureBlocking(false);

        // 3. 把channel注册到selector选择器上，监听可读状态
        socketChannel.register(selector, SelectionKey.OP_READ);

        socketChannel.write(StandardCharsets.UTF_8.
                encode("欢迎进入聊天室，请注意隐私安全"));
    }

    public static void main(String[] args) {
        try {
            new CharServer().startServer();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}
