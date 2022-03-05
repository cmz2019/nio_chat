package com.strawberry.client;

import java.io.IOException;

public class C {
    public static void main(String[] args) {
        try {
            new ChatClient().startClient("C");
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}
