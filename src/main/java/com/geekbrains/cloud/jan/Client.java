package com.geekbrains.cloud.jan;

import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ResourceBundle;

import com.geekbrains.cloud.jan.serial.TransferFileModel;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class Client implements Initializable {

    @FXML
    public Button fileButton;
    public AnchorPane anchorPane;
    private DataInputStream is;
    private ObjectOutputStream os;

    final FileChooser fileChooser = new FileChooser();

    private void sendFile(File file) throws IOException {
        TransferFileModel data = new TransferFileModel(file.getName(), Files.readAllBytes(Paths.get(file.getAbsolutePath())));
        os.writeObject(data);
    }

    private void connect() throws IOException {
        Socket socket = new Socket("localhost", 8189);
        System.out.println("Network created...");
        is = new DataInputStream(socket.getInputStream());
        os = new ObjectOutputStream(socket.getOutputStream());
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            connect();
            Stage stage = new Stage();
            fileButton.setOnAction(
                    new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(final ActionEvent e) {
                            File file = fileChooser.showOpenDialog(stage);
                            if (file != null) {
                                try {
                                    sendFile(file);
                                } catch (IOException ex) {
                                    ex.printStackTrace();
                                }

                            }
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
