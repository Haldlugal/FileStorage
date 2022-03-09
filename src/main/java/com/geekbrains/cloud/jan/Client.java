package com.geekbrains.cloud.jan;

import java.io.IOException;
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
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;

public class Client implements Initializable {

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
    public Path clientDir;
    private ClientMessageProcessor processor;
    private static Channel channel;

    public void updateClientView() {
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
        clientDir = Paths.get(System.getProperty("user.home"));
        initMouseListeners();
        processor = new ClientMessageProcessor(this);

        NioEventLoopGroup workerGroup = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(workerGroup);
        bootstrap.channel(NioSocketChannel.class);
        bootstrap.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            public void initChannel(SocketChannel ch) {
                ch.pipeline().addLast(
                        new ObjectDecoder(ClassResolvers.cacheDisabled(null)),
                        new ObjectEncoder(),
                        new ClientMainHandler(processor)
                );
            }
        });
        ChannelFuture channelFuture = bootstrap.connect("localhost", 8189);
        channel = channelFuture.channel();
    }

    private void initMouseListeners() {
        clientView.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                Path current = clientDir.resolve(clientView.getSelectionModel().getSelectedItem());
                if (Files.isDirectory(current)) {
                    this.setClientDir(current);
                    Platform.runLater(this::updateClientView);
                }
            }
        });

        serverView.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                String selectedItem = serverView.getSelectionModel().getSelectedItem();
                String newPath = serverPathField.getText() + "\\" + selectedItem;
                System.out.println(newPath);
                try {
                    channel.writeAndFlush(new ChangeDirMessage(newPath));
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });

    }

    public void goUpDirClient() {
        if (clientDir.getParent() != null) {
            this.setClientDir(clientDir.getParent());
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
                this.setClientDir(Paths.get(clientPathField.getText()));
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

    public void upload() throws IOException {
        String fileName = clientView.getSelectionModel().getSelectedItem();
        channel.writeAndFlush(new FileMessage(clientDir.resolve(fileName)));
    }


    public void download() {
        String fileName = serverView.getSelectionModel().getSelectedItem();
        System.out.println(fileName);
        channel.writeAndFlush(new FileRequest(fileName));
    }

    public void login() {
        String login = loginField.getText();
        String password = passwordField.getText();
        channel.writeAndFlush(new LoginRequest(login, password));
    }

    public void setClientDir(Path clientDir) {
        this.clientDir = clientDir;
    }

}
