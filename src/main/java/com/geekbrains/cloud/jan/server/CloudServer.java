package com.geekbrains.cloud.jan.server;

import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

public class CloudServer extends com.geekbrains.cloud.jan.server.BaseNettyServer {

    public CloudServer() {
        super(

        );
    }

    public static void main(String[] args) {
        new CloudServer();
    }
}
