package com.example.VlcStream.ComputerControl;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class PageNext {

    private static final PageNext pageNext  = new PageNext();

    private  PageNext()
    {

    }

    public static PageNext getPageNext()
    {
        return pageNext;
    }

    public void HandleTheRequest(Socket socket, String command)
    {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
                dataOutputStream.writeUTF(command);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}
