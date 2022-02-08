package com.geekbrains.cloud.jan.model;

import lombok.Data;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class ListMessage implements CloudMessage {

    private final String path;
    private final List<String> files;

    public ListMessage(Path path) throws IOException {
        this.path = path.toString();
        files = Files.list(path)
                .map(p -> p.getFileName().toString())
                .collect(Collectors.toList());
    }

    @Override
    public CommandType getType() {
        return CommandType.LIST;
    }
}
