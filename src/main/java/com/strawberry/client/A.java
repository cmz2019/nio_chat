package com.strawberry.client;

import java.io.IOException;

public class A {
    public static void main(String[] args) {
        try {
            new ChatClient().startClient("A");
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}
