package com.example.VlcStream.ComputerControl;

import android.app.Activity;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.VlcStream.VolumeKeyChangeListener;
import com.example.VlcStream.R;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Control extends Fragment implements View.OnClickListener {

    Button lockButton, unLockButton, halterButton, restartButton, pageDownButton, pageUpButton, screenShotButton;
    ConnectionClass connection;
    TextView connectionStatus, ServerName;
    SharedPreferences sharedPreferences;
    public static Socket socket;
    ExecutorService executorService;
    ImageButton refreshConnection;
    String deviceName = "";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_control, container, false);
        lockButton = view.findViewById(R.id.lock);
        unLockButton = view.findViewById(R.id.unlock);
        restartButton = view.findViewById(R.id.restarter);
        halterButton = view.findViewById(R.id.shutdown);
        pageDownButton = view.findViewById(R.id.scrolldown);
        pageUpButton = view.findViewById(R.id.scrollup);
        screenShotButton = view.findViewById(R.id.screenshot);
        connectionStatus = view.findViewById(R.id.connectionStatus);
        ServerName = view.findViewById(R.id.serverName);
        refreshConnection = view.findViewById(R.id.restartConnection);

        lockButton.setOnClickListener(this);
        unLockButton.setOnClickListener(this);
        halterButton.setOnClickListener(this);
        restartButton.setOnClickListener(this);
        pageDownButton.setOnClickListener(this);
        pageUpButton.setOnClickListener(this);
        screenShotButton.setOnClickListener(this);
        try {
            if (socket.isConnected())
            {
                connectionStatus.setText("Connected");
                ServerName.setText(deviceName);
                ServerName.setTextColor(getResources().getColor(R.color.ConnColor));
                connectionStatus.setTextColor(getResources().getColor(R.color.ConnColor));
            }
        }

        catch (NullPointerException e)
        {
            Log.e("null pointer exception", "is thrown");
        }


        refreshConnection.setOnClickListener(view1 -> {
            initiateTheConnection();
        });

        registrerReciever();

        //initiate the connection for the first time the user enters to the UI
//        Runnable runnable = new Runnable() {
//            @Override
//            public void run() {
//                initiateTheConnection();
//            }
//        };
//        new Handler().postDelayed(runnable, 1000);
        initiateTheConnection();
        return  view;

    }

    private void registrerReciever() {
        VolumeKeyChangeListener volumeKeyChangeListener = new VolumeKeyChangeListener();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.media.VOLUME_CHANGED_ACTION");
        requireActivity().getApplicationContext().registerReceiver(volumeKeyChangeListener,intentFilter);
    }

    private void initiateTheConnection() {
        connection = new ConnectionClass();
        connection.run();
    }

    @Override
    public void onClick(View view) {
    try {

        if (view.getId() == lockButton.getId())
        {
            Lock lock = Lock.getLockObject();
            if(socket.isConnected()) {
                lock.HandleTheRequest(socket, "Lock");
            }
            else
            {
                Toast.makeText(getContext(), "Not connected", Toast.LENGTH_SHORT).show();
            }

        }

        else if (view.getId() == unLockButton.getId())
        {
            Unlock unlock = Unlock.getUnlockObject();
            if(socket.isConnected())
            {
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
                String pcPassword = sharedPreferences.getString("PcPassword", "");
                unlock.HandleTheRequest(socket, "UnLock:" + pcPassword);
            }

            else
            {
                Toast.makeText(getContext(), "Not Connected", Toast.LENGTH_SHORT).show();
            }
        }

        else if (view.getId() == restartButton.getId())
        {
            if(socket.isConnected())
            {
               showConfirmationDialogToTakeAnAction("restart");
            }

            else
            {
                Toast.makeText(getContext(), "Not Connected", Toast.LENGTH_SHORT).show();
            }
        }

        else if (view.getId() == halterButton.getId())
        {
            if(socket.isConnected())
            {
                showConfirmationDialogToTakeAnAction("halt");
            }

            else
            {
                Toast.makeText(getContext(), "Not Connected", Toast.LENGTH_SHORT).show();
            }
        }


        else if (view.getId() == pageDownButton.getId())
        {
            PageNext pageNext = PageNext.getPageNext();
            if(socket.isConnected())
            {
                pageNext.HandleTheRequest(socket, "PNEXT");
            }

            else
            {
                Toast.makeText(getContext(), "Not Connected", Toast.LENGTH_SHORT).show();
            }
        }

        else if (view.getId() == pageUpButton.getId())
        {
            PagePrevious pagePrevious = PagePrevious.getPagePrevious();
            if(socket.isConnected())
            {
                pagePrevious.HandleTheRequest(socket, "PPREVIOUS");
            }

            else
            {
                Toast.makeText(getContext(), "Not Connected", Toast.LENGTH_SHORT).show();
            }
        }

        else if (view.getId() == screenShotButton.getId())
        {
            ScreenShot screenShot = ScreenShot.getScreenShot();
            if(socket.isConnected())
            {
                screenShot.HandleTheRequest(socket, "ScreenShot");
            }

            else
            {
                Toast.makeText(getContext(), "Not Connected", Toast.LENGTH_SHORT).show();
            }
        }
    }
        catch(Exception e)
        {
            Log.e("Error from buttons", "button error");
        }
    }

    public class ConnectionClass
    {
        String serverIp;
        int serverPort;
        ArrayList<String> responseString;

        public ConnectionClass()
        {
            sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
            serverIp =  sharedPreferences.getString("ServerIpAddress", "");
            serverPort = Integer.parseInt(sharedPreferences.getString("JarPort", "0"));
            responseString = new ArrayList<>();

            responseString.add("Unable to open the file");
            responseString.add("The file has no associated application to open");
            responseString.add("The file is null or corrupted");
            responseString.add("Permission denied to open the file");
            responseString.add("XDG-OPEN is Not supported");
            responseString.add("Saved on the current Folder");
            responseString.add("Unable to Capture the Screen Shot");
            responseString.add("Error Capturing ScreenShot");

        }

        public  void run()
        {
            executorService = Executors.newSingleThreadExecutor();
            executorService.execute(() -> {
                try
                {
                    if (socket == null)
                    {
                        Log.e("message from closed", "closed thread");
                        socket = new Socket();
                        socket.connect(new InetSocketAddress(serverIp, serverPort));
                    }

                    else if(socket.isClosed())
                    {
                        socket = new Socket();
                        socket.connect(new InetSocketAddress(serverIp, serverPort));
                    }

                    else if(socket !=null && !socket.isClosed())
                    {
                        socket.close();
                        socket = new Socket();
                        socket.connect(new InetSocketAddress(serverIp, serverPort));
                    }

                    changeTheStatus("Connected");
                    DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
                    DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());

                    //now sending the device name to the server
                    final String deviceName1 = Build.MODEL;
                    dataOutputStream.writeUTF("DeviceName:" + deviceName1);

                    //this while statement checks if a server is disconnected for every 2 seconds
                    while(true)
                    {
                        String incommingMessage = dataInputStream.readUTF();
                        if(incommingMessage.equalsIgnoreCase("yesConnected"))
                            changeTheStatus("Connected");
                        else if(incommingMessage.contains("ServerName"))
                        {
                            Activity activity = getActivity();
                            if(activity != null && isAdded())
                            changeServerName(incommingMessage);
                        }

                        if (responseString.contains(incommingMessage))
                        {
                            requireActivity().runOnUiThread(() ->
                            {
                                Toast.makeText(getContext(), "" + incommingMessage, Toast.LENGTH_LONG).show();
                            });
                        }
                    }
                } catch (IOException e) {
                    Activity activity = getActivity();
                    if(activity != null && isAdded())
                    changeTheStatus("Connecting...");
                    Log.e("Message from socket", "error message" + e.getMessage());
                }
            });
        }

        private void changeServerName(String serverName) {
            try {
                requireActivity().runOnUiThread(() ->
                {
                    ServerName.setText("to: " + serverName.substring(10));
                    deviceName = serverName.substring(10);
                    ServerName.setTextColor(getResources().getColor(R.color.ConnColor));
                });
            }catch (Exception e)
            {
                Log.e("message from", "not attached");
            }

        }

        private void changeTheStatus(String status)
        {
                requireActivity().runOnUiThread(() -> {
                if(status.equalsIgnoreCase("Connected"))
                {
                    connectionStatus.setText(R.string.connected);
                    connectionStatus.setTextColor(getResources().getColor(R.color.ConnColor));
                }

                if(status.equalsIgnoreCase("Connecting..."))
                {
                    connectionStatus.setText(R.string.disConnected);
                    ServerName.setText("");
                }
            });
        }
    }

    private void showConfirmationDialogToTakeAnAction(String command)
    {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(getContext());
            builder1.setTitle("Are you Sure ? ");
        if(command.equalsIgnoreCase("restart"))
            builder1.setMessage("The Computer will be restart");
        else if(command.equalsIgnoreCase("halt"))
            builder1.setMessage("The Computer Will be shutdown");
        ArrayList<String> name = new ArrayList<>();

        builder1.setCancelable(true);
        builder1.setPositiveButton(
                "Yes",
                (dialog, id) -> {
                    if(command.equalsIgnoreCase("halt"))
                    {
                        Shutdown.getShutDown().HandleTheRequest(socket, "ShutDown");
                    }

                    else if(command.equalsIgnoreCase("restart"))
                    {
                        Restart.getRestart().HandleTheRequest(socket, "Restart");
                    }
                });

        builder1.setNegativeButton(
                "No",
                (dialog, id) -> dialog.cancel());

        AlertDialog alert11 = builder1.create();
        alert11.show();
    }

}
