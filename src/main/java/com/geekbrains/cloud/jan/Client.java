package com.geekbrains.cloud.jan;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
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
    public TextField clientPath;
    public TextField serverPath;
    private Path clientDir;
    private DataInputStream is;
    private DataOutputStream os;
    private byte[] buf;

    // read from network
    private void readLoop() {
        try {
            while (true) {
                String command = is.readUTF();
                System.out.println("received: " + command);// wait message
                if (command.equals("#list#")) {
                    Platform.runLater(() -> serverView.getItems().clear());
                    String newServerDir = is.readUTF();
                    serverPath.setText(newServerDir);
                    int filesCount = is.readInt();
                    for (int i = 0; i < filesCount; i++) {
                        String fileName = is.readUTF();
                        Platform.runLater(() -> serverView.getItems().add(fileName));
                    }
                } else if (command.equals("#file#")) {
                    Sender.getFile(is, clientDir, SIZE, buf);
                    Platform.runLater(this::updateClientView);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            buf = new byte[SIZE];
            clientDir = Paths.get(System.getProperty("user.home"));
            updateClientView();
            clientPath.setText(clientDir.toString());

            Socket socket = new Socket("localhost", 8189);
            System.out.println("Network created...");
            is = new DataInputStream(socket.getInputStream());
            os = new DataOutputStream(socket.getOutputStream());

            Thread readThread = new Thread(this::readLoop);
            readThread.setDaemon(true);
            readThread.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void goUpDirClient() {
        if (clientDir.getParent() != null) {
            clientDir = clientDir.getParent();
            clientPath.setText(clientDir.toString());
            updateClientView();
        }
    }

    public void goUpServerClient() throws IOException {
        os.writeUTF("#change_dir_up#");
    }

    public void changeClientDir(KeyEvent actionEvent) {
        if (actionEvent.getCode().equals(KeyCode.ENTER)) {
            if (Files.isDirectory(Paths.get(clientPath.getText()))) {
                clientDir = Paths.get(clientPath.getText());
                updateClientView();
            }
        }
    }

    public void changeServerDir(KeyEvent actionEvent) throws IOException {
        if (actionEvent.getCode().equals(KeyCode.ENTER)) {
            os.writeUTF("#change_dir#");
            os.writeUTF(serverPath.getText());
        }
    }

    public void upload(ActionEvent actionEvent) throws IOException {
        String fileName = clientView.getSelectionModel().getSelectedItem();
        Sender.sendFile(fileName, os, clientDir);
    }


    public void download(ActionEvent actionEvent) throws IOException {
        String fileName = serverView.getSelectionModel().getSelectedItem();
        os.writeUTF("#get_file#");
        os.writeUTF(fileName);
        os.flush();
    }


}
