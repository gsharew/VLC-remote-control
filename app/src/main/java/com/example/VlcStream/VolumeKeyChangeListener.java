package com.example.VlcStream;

import static android.content.Context.AUDIO_SERVICE;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.preference.PreferenceManager;

import com.example.VlcStream.ComputerControl.Control;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.concurrent.Executors;

public class VolumeKeyChangeListener extends BroadcastReceiver {

      static int previousValue = 0, difference;
      int currentValue = 0;
      boolean alreadyFound = false;
      int deviceMaxVolumeValue;
      SharedPreferences sharedPreferences;
    @Override
    public void onReceive(Context context, Intent intent) {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        String volumeKeyValue = sharedPreferences.getString("volumeAdjustmentSwitched", "");
        if (volumeKeyValue.equalsIgnoreCase("switched"))
        {
                if (!alreadyFound) {
                    final AudioManager audioManager = (AudioManager) context.getSystemService(AUDIO_SERVICE);
                    deviceMaxVolumeValue = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                    alreadyFound = true;
                }

                currentValue = (Integer) intent.getExtras().get("android.media.EXTRA_VOLUME_STREAM_VALUE");
                difference = previousValue - currentValue;
                if (difference > 0 || (0 == currentValue)) {
                    Executors.newSingleThreadExecutor().execute(() ->
                    {
                        try {
                            if(Control.socket != null)
                            {
                                DataOutputStream dataOutputStream = new DataOutputStream(Control.socket.getOutputStream());
                                dataOutputStream.writeUTF("PNEXT");
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
                } else if (difference < 0 || (currentValue == deviceMaxVolumeValue)) {
                    Executors.newSingleThreadExecutor().execute(() ->
                    {
                        try {
                            if(Control.socket != null)
                            {
                                DataOutputStream dataOutputStream = new DataOutputStream(Control.socket.getOutputStream());
                                dataOutputStream.writeUTF("PPREVIOUS");
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
                }

                previousValue = currentValue;
            }
    }
}
