package com.geekbrains.cloud.jan.server;

import com.geekbrains.cloud.jan.model.*;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

@ChannelHandler.Sharable
public class CloudServerHandler extends SimpleChannelInboundHandler<CloudMessage> {

    private Path currentDir;
    private Path clientDir;

    private final String DATA_FOLDER = "data";

    private final AuthService authService = new DbAuthService();

    public CloudServerHandler() {
        System.out.println("handler created!");
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        System.out.println("Handler added");
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) {
        System.out.println("Handler removed");
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        authService.start();
        currentDir = Paths.get(DATA_FOLDER);
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
                break;
            case LOGIN_REQUEST:
                processLogin((LoginRequest) cloudMessage, ctx);
        }
    }

    private void processChangeDirectory(ChangeDirMessage msg) {
        String newDir = msg.getPathString();
        boolean goUp = msg.getGoUp();
        if (goUp) {
            if (currentDir.compareTo(clientDir) != 0) {
                currentDir = currentDir.getParent();
            }
        } else if (newDir.startsWith(clientDir.toString()) && Files.isDirectory(Paths.get(newDir))) {
            currentDir = Paths.get(newDir);
        }
    }

    private void sendList(ChannelHandlerContext ctx) throws IOException {
        System.out.println("sending list");
        ctx.writeAndFlush(new ListMessage(currentDir));
    }

    private void processFileMessage(FileMessage cloudMessage) throws IOException {
        Files.write(currentDir.resolve(cloudMessage.getFileName()), cloudMessage.getBytes());
    }

    private void processFileRequest(FileRequest cloudMessage, ChannelHandlerContext ctx) throws IOException {
        Path path = currentDir.resolve(cloudMessage.getFileName());
        ctx.writeAndFlush(new FileMessage(path));
    }

    private void processLogin(LoginRequest cloudMessage, ChannelHandlerContext ctx) throws IOException {
        Optional<User> user = authService.getNickByLoginAndPass(cloudMessage.getLogin(), cloudMessage.getPassword());

        if (user.isPresent()) {
            System.out.println("login ok");
            currentDir = Paths.get(DATA_FOLDER + "\\" + user.get().nick);
            clientDir = Paths.get(DATA_FOLDER + "\\" + user.get().nick);
            sendList(ctx);
            ctx.writeAndFlush(new LoginResponse(user.get().nick, true));
        } else {
            System.out.println("login fail");
            ctx.writeAndFlush(new LoginResponse("", false));
        }


    }
}
