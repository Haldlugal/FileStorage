package com.geekbrains.cloud.jan;

import com.geekbrains.cloud.jan.model.CloudMessage;
import com.geekbrains.cloud.jan.model.FileMessage;
import com.geekbrains.cloud.jan.model.ListMessage;
import com.geekbrains.cloud.jan.model.LoginResponse;
import javafx.application.Platform;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class CloudMessageProcessor {

    private Path clientDir;
    public Pane loginUI;
    public ListView<String> clientView;
    public ListView<String> serverView;
    public TextField serverPathField;

    public CloudMessageProcessor(Path clientDir,
                                 ListView<String> clientView,
                                 ListView<String> serverView,
                                    TextField serverPathField,
                                 Pane loginUI) {
        this.clientDir = clientDir;
        this.clientView = clientView;
        this.serverView = serverView;
        this.serverPathField = serverPathField;
        this.loginUI = loginUI;
    }

    public void processMessage(CloudMessage message) throws IOException {
        System.out.println(message.getType());
        switch (message.getType()) {
            case LIST:
                processMessage((ListMessage) message);
                break;
            case FILE:
                processMessage((FileMessage) message);
                break;
            case LOGIN_RESPONSE:
                processMessage((LoginResponse) message);
        }
    }

    public void processMessage(LoginResponse message) {
        if (message.isSuccess()) {
            loginUI.setVisible(false);
        }
    }

    public void processMessage(FileMessage message) throws IOException {
        Files.write(clientDir.resolve(message.getFileName()), message.getBytes());
        Platform.runLater(this::updateClientView);
    }

    public void processMessage(ListMessage message) {
        Platform.runLater(() -> {
            serverView.getItems().clear();
            serverView.getItems().addAll(message.getFiles());
        });
        serverPathField.setText(message.getPath());
    }

    private void updateClientView() {
        try {
            clientView.getItems().clear();
            Files.list(clientDir)
                    .map(p -> p.getFileName().toString())
                    .forEach(f -> clientView.getItems().add(f));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
