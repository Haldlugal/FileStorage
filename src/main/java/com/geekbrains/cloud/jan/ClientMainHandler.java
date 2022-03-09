package com.geekbrains.cloud.jan;

import com.geekbrains.cloud.jan.model.CloudMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class ClientMainHandler  extends SimpleChannelInboundHandler<CloudMessage> {
    ClientMessageProcessor processor;
    public ClientMainHandler(ClientMessageProcessor processor) {
        this.processor = processor;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, CloudMessage message) throws Exception {
        processor.processMessage(message);
    }

}
