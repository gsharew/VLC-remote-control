package com.example.VlcStream.ComputerControl;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.Executors;

public class Lock {

    private static final Lock lockObject  = new Lock();

    private  Lock()
    {

    }

    public static Lock getLockObject()
    {
        return lockObject;
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
