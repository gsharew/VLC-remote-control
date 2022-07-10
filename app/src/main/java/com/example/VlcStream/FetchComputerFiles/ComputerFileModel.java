package com.example.VlcStream.FetchComputerFiles;

public class ComputerFileModel {

    String name;
    String uri;
    String type;
    String path;

    ComputerFileModel(String musicName, String uri, String type, String path) {
        name = musicName;
        this.uri = uri;
        this.type = type;
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public String getUri() {
        return uri;
    }

    public String getPath() {
        return path;
    }

    public String getType() {
        return type;
    }
}