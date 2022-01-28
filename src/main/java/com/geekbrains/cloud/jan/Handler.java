package com.geekbrains.cloud.jan;

import com.geekbrains.cloud.jan.serial.TransferFileModel;
import com.geekbrains.cloud.jan.serial.TransferObject;

import java.io.*;
import java.net.Socket;
import java.util.Arrays;

public class Handler implements Runnable {

    private ObjectInputStream is;
    private DataOutputStream os;

    public Handler(Socket socket) throws IOException {
        is = new ObjectInputStream(socket.getInputStream());
        os = new DataOutputStream(socket.getOutputStream());
    }

    @Override
    public void run() {
        try {
            while (true) {
                TransferFileModel file = (TransferFileModel) is.readObject();
                System.out.println(file.getName());
                System.out.println(new String(file.getContent()));
                File newFile = new File("test_data",  file.getName());
                newFile.createNewFile();
                FileWriter fw = new FileWriter( newFile.getAbsoluteFile());
                BufferedWriter bw = new BufferedWriter( fw );
                bw.write( new String(file.getContent()) );
                bw.flush();
                bw.close();
                System.out.println("received: " + file.getName());
                os.writeUTF("received: " + file.getName());
                os.flush();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
