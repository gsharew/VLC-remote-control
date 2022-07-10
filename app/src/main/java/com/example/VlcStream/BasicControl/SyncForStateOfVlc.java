package com.example.VlcStream.BasicControl;

import android.content.Context;
import android.content.SharedPreferences;

import android.preference.PreferenceManager;
import android.text.format.DateUtils;
import android.util.Base64;
import android.util.Log;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.VlcStream.R;

import org.json.JSONException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;


public class SyncForStateOfVlc  {
    SharedPreferences sharedPreferences;
    RequestQueue requestQueue;
    Context context;
    ImageView pauseImage, shuffleImage, repeatImage;
    SeekBar seekBar, musicPosition;
    int currentVLCPosition, length;
    String currentPlayingName;
    TextView startPosition, endPosition, artistName;
    private  static  ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
    String serverIp, luaPassword, serverPort;
    SyncForStateOfVlc(Context context, ImageView pauseImage, ImageView shuffleImage, ImageView repeatImage, SeekBar seekBar, TextView startPosition, TextView endPosition, SeekBar musicPosition, TextView artistName)
    {
        this.context = context;
        this.pauseImage = pauseImage;
        this.repeatImage = repeatImage;
        this.shuffleImage = shuffleImage;
        this.seekBar = seekBar;
        this.startPosition = startPosition;
        this.endPosition = endPosition;
        this.musicPosition = musicPosition;
        this.artistName = artistName;
        requestQueue = Volley.newRequestQueue(context.getApplicationContext());

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        serverIp = sharedPreferences.getString("ServerIpAddress", null);
        luaPassword = sharedPreferences.getString("Lua Password", null);
        serverPort = sharedPreferences.getString("ServerPort", null);
    }

    public  void run()
    {
        Runnable runnable = SyncForStateOfVlc.this::syncForStatusChange;
        ScheduledFuture<?> runnableHandler = scheduledExecutorService.scheduleAtFixedRate(runnable, 0, 1, TimeUnit.SECONDS);
        Runnable canceller = () -> runnableHandler.cancel(false);
        scheduledExecutorService.schedule(canceller, 24, TimeUnit.HOURS);
    }

    private void syncForStatusChange()
    {
        if (serverIp == null)
        {
            return;
        }

        if (luaPassword == null)
        {
            return;
        }
        if(serverPort == null)
        {
            return;
        }

        String url = "http://" + serverIp + ":" + serverPort + "/requests/status.json";
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, null,
                response -> {
                    try {
                        if (response.getString("state").equals("playing"))
                        {
                            if(Playlist.getVlcState().equals("paused"))
                            pauseImage.setImageResource(R.drawable.pause);
                            Playlist.setVlcState("playing");
                            length =Integer.parseInt(response.getString("length"));
                            musicPosition.setMax(length);

                            //get the current playing position for the vlc media player
                            currentVLCPosition = Integer.parseInt(response.getString("time"));
                            seekBar.setProgress(Integer.parseInt(response.getString("volume"))/4);
                            musicPosition.setProgress(currentVLCPosition);
                            String currentValue = DateUtils.formatElapsedTime(currentVLCPosition + 1);
                            startPosition.setText(currentValue);

                            String endPositionValue = DateUtils.formatElapsedTime(length);
                            endPosition.setText(endPositionValue);

                            currentPlayingName = response.getJSONObject("information").getJSONObject("category").getJSONObject("meta").getString("filename");
                            if(currentPlayingName.length() > 47)
                                artistName.setText(currentPlayingName.substring(0,45) + "...");
                            else
                                artistName.setText(currentPlayingName);
                        }

                        else if(response.getString("state").equals("paused"))
                        {
                            Playlist.setVlcState("paused");
                            pauseImage.setImageResource(R.drawable.play);
                        }

                        else
                        {
                            Playlist.setVlcState("stopped");
                            pauseImage.setImageResource(R.drawable.play);
                        }

                        if (response.getString("random").equals("true"))
                        {
                            Playlist.shuffleStatus = "Shuffled";
                            shuffleImage.setImageResource(R.drawable.shuffle_on);
                        }

                        else
                        {
                            Playlist.shuffleStatus = "notShuffled";
                            shuffleImage.setImageResource(R.drawable.shuffle);
                        }

                        if (response.getString("loop").equalsIgnoreCase("true"))
                        {
                            repeatImage.setImageResource(R.drawable.repeat_on);
                            Playlist.doesPreviouslyFetchd = true;
                        }
                        else
                        {
                            repeatImage.setImageResource(R.drawable.repeat);
                            Playlist.doesPreviouslyFetchd = false;
                        }

                    } catch (JSONException e) {
                        Log.e("inner tag error", "" + e.getMessage());
                    }
                }, error -> {
            Log.e("this is tag", "" +  error.getMessage());
        }) {

            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> param = new HashMap<>();
                //String credential = String.format("%s:%s","","Getachew");
                String credential = "" + ":" + luaPassword;
                String auth = "Basic " + android.util.Base64.encodeToString(credential.getBytes(), Base64.NO_WRAP);
                param.put("Authorization", auth);
                return param;
            }
        };
        requestQueue.add(jsonObjectRequest);
    }

    public static void initialize()
    {
        scheduledExecutorService = Executors.newScheduledThreadPool(1);
    }

    public  static void shutdownTheScheduler()
    {
        scheduledExecutorService.shutdownNow();
    }
}
