package com.geekbrains.cloud.jan;

import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ResourceBundle;

import com.geekbrains.cloud.jan.model.*;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.*;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;

public class Client implements Initializable {

    private static final int SIZE = 256;
    public ListView<String> clientView;
    public ListView<String> serverView;
    public Button clientUpFolderBtn;
    public Button serverUpFolderBtn;
    public TextField clientPathField;
    public TextField serverPathField;
    public Group mainUI;
    public Pane loginUI;
    public TextField passwordField;
    public TextField loginField;
    private Path clientDir;
    private CloudMessageProcessor processor;

    private static NioEventLoopGroup workerGroup;
    private static Bootstrap bootstrap;
    private static Channel channel;

    private void updateClientView() {
        try {
            updateClientPathField();
            clientView.getItems().clear();
            Files.list(clientDir)
                    .map(p -> p.getFileName().toString())
                    .forEach(f -> clientView.getItems().add(f));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            clientDir = Paths.get(System.getProperty("user.home"));
            updateClientView();
            initMouseListeners();
            processor = new CloudMessageProcessor(clientDir, clientView, serverView, serverPathField, loginUI);

            workerGroup = new NioEventLoopGroup();
            bootstrap = new Bootstrap();
            bootstrap.group(workerGroup);
            bootstrap.channel(NioSocketChannel.class);
            bootstrap.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline().addLast(
                            new ObjectDecoder(ClassResolvers.cacheDisabled(null)),
                            new ObjectEncoder(),
                            new ClientMainHandler(processor)
                    );
                }
            });
            ChannelFuture channelFuture = bootstrap.connect("localhost", 8189);
            channel = channelFuture.channel();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initMouseListeners() {
        clientView.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                Path current = clientDir.resolve(getItem());
                if (Files.isDirectory(current)) {
                    clientDir = current;
                    Platform.runLater(this::updateClientView);
                }
            }
        });

        serverView.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                // todo Home Work
            }
        });

    }

    private String getItem() {
        return clientView.getSelectionModel().getSelectedItem();
    }


    public void goUpDirClient() {
        if (clientDir.getParent() != null) {
            clientDir = clientDir.getParent();
            updateClientView();
        }
    }

    public void goUpServerClient() throws IOException {
        ChangeDirMessage msg = new ChangeDirMessage(serverPathField.getText());
        msg.setGoUp(true);
        channel.writeAndFlush(msg);
    }

    public void changeClientDir(KeyEvent actionEvent) {
        if (actionEvent.getCode().equals(KeyCode.ENTER)) {
            if (Files.isDirectory(Paths.get(clientPathField.getText()))) {
                clientDir = Paths.get(clientPathField.getText());
                updateClientView();
            }
        }
    }

    private void updateClientPathField() {
        clientPathField.setText(clientDir.toString());
    }

    public void changeServerDir(KeyEvent actionEvent) throws IOException {
        if (actionEvent.getCode().equals(KeyCode.ENTER)) {
            channel.writeAndFlush(new ChangeDirMessage(serverPathField.getText()));
        }
    }

    public void upload(ActionEvent actionEvent) throws IOException {
        String fileName = clientView.getSelectionModel().getSelectedItem();
        channel.writeAndFlush(new FileMessage(clientDir.resolve(fileName)));
    }


    public void download(ActionEvent actionEvent) throws IOException {
        String fileName = serverView.getSelectionModel().getSelectedItem();
        channel.writeAndFlush(new FileRequest(fileName));
    }

    public void login() {
        String login = loginField.getText();
        String password = passwordField.getText();
        channel.writeAndFlush(new LoginRequest(login, password));
    }

}
