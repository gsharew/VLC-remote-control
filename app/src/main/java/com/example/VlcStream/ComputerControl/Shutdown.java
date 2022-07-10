package com.example.VlcStream.ComputerControl;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.Executors;

public class Shutdown {

    private static final Shutdown shutDown  = new Shutdown();

    private  Shutdown()
    {

    }

    public static Shutdown getShutDown()
    {
        return shutDown;
    }

    public void HandleTheRequest(Socket socket, String command)
    {
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
                    dataOutputStream.writeUTF(command);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
