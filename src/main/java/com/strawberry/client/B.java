package com.strawberry.client;

import java.io.IOException;

public class B {
    public static void main(String[] args) {
        try {
            new ChatClient().startClient("B");
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}
