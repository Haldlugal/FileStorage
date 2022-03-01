package com.geekbrains.cloud.jan;

import com.geekbrains.cloud.jan.model.CloudMessage;
import com.geekbrains.cloud.jan.model.FileMessage;
import com.geekbrains.cloud.jan.model.ListMessage;
import com.geekbrains.cloud.jan.model.LoginResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;

import java.io.IOException;
import java.nio.file.Path;

public class ClientMainHandler  extends SimpleChannelInboundHandler<CloudMessage> {
    CloudMessageProcessor processor;
    public ClientMainHandler(CloudMessageProcessor processor) {
        this.processor = processor;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, CloudMessage message) throws Exception {
        processor.processMessage(message);
    }



}
