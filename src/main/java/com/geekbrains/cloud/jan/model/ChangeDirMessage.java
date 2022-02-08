package com.geekbrains.cloud.jan.model;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ChangeDirMessage implements CloudMessage {
    private final String pathString;
    private Boolean goUp = false;

    public ChangeDirMessage(String path) throws IOException {
        pathString = path;
    }



    @Override
    public CommandType getType() {
        return CommandType.CHANGE_DIRECTORY;
    }

    public String getPathString() {
        return pathString;
    }

    public Boolean getGoUp() {
        return goUp;
    }

    public void setGoUp(Boolean goUp) {
        this.goUp = goUp;
    }
}
