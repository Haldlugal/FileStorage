package com.geekbrains.cloud.jan;

import com.geekbrains.cloud.jan.model.CloudMessage;
import com.geekbrains.cloud.jan.model.FileMessage;
import com.geekbrains.cloud.jan.model.ListMessage;
import com.geekbrains.cloud.jan.model.LoginResponse;
import javafx.application.Platform;
import java.io.IOException;
import java.nio.file.Files;

public class ClientMessageProcessor {

    private final Client client;

    public ClientMessageProcessor(Client client) {
        this.client = client;
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
            client.loginUI.setVisible(false);
            client.updateClientView();
        }
    }

    public void processMessage(FileMessage message) throws IOException {
        Files.write(client.clientDir.resolve(message.getFileName()), message.getBytes());
        Platform.runLater(this::updateClientView);
    }

    public void processMessage(ListMessage message) {
        Platform.runLater(() -> {
            client.serverView.getItems().clear();
            client.serverView.getItems().addAll(message.getFiles());
        });
        client.serverPathField.setText(message.getPath());
    }

    private void updateClientView() {
        try {
            client.clientView.getItems().clear();
            Files.list(client.clientDir)
                    .map(p -> p.getFileName().toString())
                    .forEach(f -> client.clientView.getItems().add(f));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
