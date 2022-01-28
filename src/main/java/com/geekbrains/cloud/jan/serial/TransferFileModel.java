package com.geekbrains.cloud.jan.serial;

import java.io.Serializable;
import java.util.Arrays;

public class TransferFileModel implements Serializable {
    String name;
    byte[] content;


    public TransferFileModel(String name, byte[] content) {
        this.name = name;
        this.content = content;
    }

    public TransferFileModel() {
    }

    public String getName() {
        return name;
    }

    public byte[] getContent() {
        return content;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "TransferFileModel{" +
                "name='" + name + '\'' +
                ", content=" + Arrays.toString(content) +
                '}';
    }
}
