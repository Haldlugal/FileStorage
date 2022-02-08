package com.geekbrains.cloud.jan;

import com.geekbrains.cloud.jan.model.CloudMessage;
import com.geekbrains.cloud.jan.model.FileMessage;
import com.geekbrains.cloud.jan.model.ListMessage;
import javafx.application.Platform;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class CloudMessageProcessor {

    private Path clientDir;
    public ListView<String> clientView;
    public ListView<String> serverView;
    public TextField serverPathField;

    public CloudMessageProcessor(Path clientDir,
                                 ListView<String> clientView,
                                 ListView<String> serverView,
                                    TextField serverPathField) {
        this.clientDir = clientDir;
        this.clientView = clientView;
        this.serverView = serverView;
        this.serverPathField = serverPathField;
    }

    public void processMessage(CloudMessage message) throws IOException {
        switch (message.getType()) {
            case LIST:
                processMessage((ListMessage) message);
                break;
            case FILE:
                processMessage((FileMessage) message);
                break;
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
