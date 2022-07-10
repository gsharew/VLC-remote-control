package com.example.VlcStream.ComputerControl;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.Executors;

public class Restart {

    private static final Restart restart  = new Restart();

    private  Restart()
    {

    }

    public static Restart getRestart()
    {
        return restart;
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
