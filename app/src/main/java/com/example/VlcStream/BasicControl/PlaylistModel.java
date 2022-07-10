package com.example.VlcStream.BasicControl;

public class PlaylistModel {

    String name;
    String id;
    String uri;

    PlaylistModel(String musicName, String id, String uri)
    {
        name = musicName;
        this.id = id;
        this.uri = uri;
    }

    public String getName()
    {
        return name;
    }

    public String getUri()
    {
        return uri;
    }

    public String getId()
    {
        return id;
    }
}
