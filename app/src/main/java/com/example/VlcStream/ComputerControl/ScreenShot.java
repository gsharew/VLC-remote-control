package com.example.VlcStream.ComputerControl;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.Executors;

public class ScreenShot {

    private static final ScreenShot screenShot  = new ScreenShot();

    private  ScreenShot()
    {

    }

    public static ScreenShot getScreenShot()
    {
        return screenShot;
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
