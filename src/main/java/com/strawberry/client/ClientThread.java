package com.strawberry.client;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Set;

public class ClientThread implements Runnable {
    private Selector selector;

    public ClientThread(Selector selector) {
        this.selector = selector;
    }

    @Override
    public void run() {
        try {
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
                    if (selectionKey.isReadable()) {
                        readOperator(selector, selectionKey);
                    }
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
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
        }
    }

}
