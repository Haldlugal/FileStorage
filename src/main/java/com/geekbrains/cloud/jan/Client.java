package com.geekbrains.cloud.jan;

import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ResourceBundle;

import com.geekbrains.cloud.jan.model.ChangeDirMessage;
import com.geekbrains.cloud.jan.model.CloudMessage;
import com.geekbrains.cloud.jan.model.FileMessage;
import com.geekbrains.cloud.jan.model.FileRequest;
import io.netty.handler.codec.serialization.ObjectDecoderInputStream;
import io.netty.handler.codec.serialization.ObjectEncoderOutputStream;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

public class Client implements Initializable {

    private static final int SIZE = 256;
    public ListView<String> clientView;
    public ListView<String> serverView;
    public Button clientUpFolderBtn;
    public Button serverUpFolderBtn;
    public TextField clientPathField;
    public TextField serverPathField;
    private Path clientDir;
    private CloudMessageProcessor processor;
    private ObjectDecoderInputStream is;
    private ObjectEncoderOutputStream os;
    private byte[] buf;

    private void readLoop() {
        try {
            while (true) {
                CloudMessage message = (CloudMessage) is.readObject();
                processor.processMessage(message);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

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
            processor = new CloudMessageProcessor(clientDir, clientView, serverView, serverPathField);
            Socket socket = new Socket("localhost", 8189);
            System.out.println("Network created...");
            os = new ObjectEncoderOutputStream(socket.getOutputStream());
            is = new ObjectDecoderInputStream(socket.getInputStream());
            Thread readThread = new Thread(this::readLoop);
            readThread.setDaemon(true);
            readThread.start();
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
        os.writeObject(msg);
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
            os.writeObject(new ChangeDirMessage(serverPathField.getText()));
        }
    }

    public void upload(ActionEvent actionEvent) throws IOException {
        String fileName = clientView.getSelectionModel().getSelectedItem();
        os.writeObject(new FileMessage(clientDir.resolve(fileName)));
    }


    public void download(ActionEvent actionEvent) throws IOException {
        String fileName = serverView.getSelectionModel().getSelectedItem();
        os.writeObject(new FileRequest(fileName));
    }


}
