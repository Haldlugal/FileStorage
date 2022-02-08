package com.geekbrains.cloud.jan.server;

import com.geekbrains.cloud.jan.model.*;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class CloudServerHandler extends SimpleChannelInboundHandler<CloudMessage> {

    private Path currentDir;
    private final String DATA_FOLDER = "data";

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        // init client dir
        currentDir = Paths.get(DATA_FOLDER);
        sendList(ctx);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx,
                                CloudMessage cloudMessage) throws Exception {
        switch (cloudMessage.getType()) {
            case FILE_REQUEST:
                processFileRequest((FileRequest) cloudMessage, ctx);
                break;
            case FILE:
                processFileMessage((FileMessage) cloudMessage);
                sendList(ctx);
                break;
            case CHANGE_DIRECTORY:
                processChangeDirectory((ChangeDirMessage) cloudMessage);
                sendList(ctx);
        }
    }

    private void processChangeDirectory(ChangeDirMessage msg) {
        String newDir = msg.getPathString();
        boolean goUp = msg.getGoUp();
        if (goUp) {
            if (currentDir.compareTo(Paths.get(DATA_FOLDER)) != 0) {
                currentDir = currentDir.getParent();
            }
        } else if (newDir.startsWith(DATA_FOLDER) && Files.isDirectory(Paths.get(newDir))) {
            currentDir = Paths.get(newDir);
        }
    }

    private void sendList(ChannelHandlerContext ctx) throws IOException {
        ctx.writeAndFlush(new ListMessage(currentDir));
    }

    private void processFileMessage(FileMessage cloudMessage) throws IOException {
        Files.write(currentDir.resolve(cloudMessage.getFileName()), cloudMessage.getBytes());
    }

    private void processFileRequest(FileRequest cloudMessage, ChannelHandlerContext ctx) throws IOException {
        Path path = currentDir.resolve(cloudMessage.getFileName());
        ctx.writeAndFlush(new FileMessage(path));
    }
}
