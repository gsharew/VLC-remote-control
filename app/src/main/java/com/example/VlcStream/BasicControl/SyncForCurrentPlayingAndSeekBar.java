//package com.example.VlcStream.BasicControl;
//
//import static com.example.VlcStream.BasicControl.Audio.musicContainer;
//import static com.example.VlcStream.BasicControl.Audio.playlistInformation;
//
//import android.content.Context;
//import android.content.SharedPreferences;
//import android.preference.PreferenceManager;
//import android.util.Log;
//import android.widget.SeekBar;
//import android.widget.TextView;
//
//import com.android.volley.Request;
//import com.android.volley.RequestQueue;
//import com.android.volley.toolbox.JsonObjectRequest;
//import com.android.volley.toolbox.Volley;
//
//import org.json.JSONArray;
//import org.json.JSONException;
//import org.json.JSONObject;
//
//import java.util.Base64;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.concurrent.Executors;
//import java.util.concurrent.ScheduledExecutorService;
//import java.util.concurrent.ScheduledFuture;
//import java.util.concurrent.TimeUnit;
//
//
//public class SyncForCurrentPlayingAndSeekBar{
//
//    SeekBar musicPosition;
//    Context context;
//    SharedPreferences sharedPreferences;
//    RequestQueue requestQueue;
//    TextView startPosition, endPosition;
//    private static  ScheduledExecutorService scheduledExecutorService = null;
//
//    SyncForCurrentPlayingAndSeekBar(Context context, SeekBar currentMusicPosition, TextView startPosition, TextView endPosition)
//    {
//        this.context = context;
//        this.musicPosition = currentMusicPosition;
//        this.startPosition = startPosition;
//        this.endPosition = endPosition;
//    }
//
//    public  void run()
//    {
//        Runnable runnable = () -> {
//            if(Audio.getVlcState().equalsIgnoreCase("playing") || Audio.getVlcState().equalsIgnoreCase("paused"))
//            {
//                updateTheSeekBarAndPlaylist();
//            }
//        };
//
//        ScheduledFuture<?> runnableHandler = scheduledExecutorService.scheduleAtFixedRate(runnable, 0, 1, TimeUnit.SECONDS);
//        Runnable canceller = () -> runnableHandler.cancel(false);
//        scheduledExecutorService.schedule(canceller, 1, TimeUnit.HOURS);
//    }
//
//    private void updateTheSeekBarAndPlaylist()
//    {
//        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
//        String serverIp = sharedPreferences.getString("ServerIpAddress", null);
//        String luaPassword = sharedPreferences.getString("Lua Password", null);
//
//        if (serverIp == null)
//        {
//            return;
//        }
//
//        if (luaPassword == null)
//        {
//            return;
//        }
//
//        requestQueue = Volley.newRequestQueue(context.getApplicationContext());
//        String url = "http://" + serverIp + ":8080/requests/playlist.json";
//        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, null,
//                response -> {
//                    try {
//                        JSONArray jsonArray = response.getJSONArray("children").getJSONObject(0).getJSONArray("children");
//                        if(jsonArray.length() == 0)
//                        {
//                            musicContainer.clear();
//                            playlistInformation.clear();
//                            startPosition.setText("0:00");
//                            endPosition.setText("0:00");
//                            musicPosition.setMax(0);
//                            Audio.recyclerView.post(Audio.recyclerAdapter::notifyDataSetChanged);
//                        }
//                        //check if a user is manually playing a music without using this app remotely or removes a file
//                        else if (jsonArray.length() > 0 && (jsonArray.length() != musicContainer.size()))
//                        {
//                            musicContainer.clear();
//                            playlistInformation.clear();
//
//                            for (int i = 0; i < jsonArray.length(); i++)
//                            {
//                                JSONObject jsonObject = jsonArray.getJSONObject(i);
//                                String name = jsonObject.getString("name");
//                                String id = jsonObject.getString("id");
//                                String uri = jsonObject.getString("uri");
//
//                                playlistInformation.add(new PlaylistModel(name, id, uri));
//                                musicContainer.add(name);
//                            }
//
//                            //update the user interface
//                            if(Audio.recyclerAdapter != null && Audio.recyclerView != null)
//                            Audio.recyclerView.post(Audio.recyclerAdapter::notifyDataSetChanged);
//
//                        }
//
//
//                    } catch (JSONException e) {
//                        Log.e("inner tag error", "" + e.getMessage());
//                    }
//                }, error -> {
//            Log.e("this is tag", "" +  error.getMessage());
//        })
//
//        {
//            @Override
//            public Map<String, String> getHeaders() {
//                HashMap<String, String> param = new HashMap<>();
//                //String credential = String.format("%s:%s","","Getachew");
//                String credential = "" + ":" + luaPassword;
//                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
//                    String auth = "Basic " + Base64.getEncoder().encodeToString(credential.getBytes()); //this is for the java import statement
//                    //String auth = "Basic " + Base64.encodeToString(credential.getBytes(), Base64.NO_WRAP); //this is for android based level import
//                    param.put("Authorization", auth);
//                }
//                return param;
//            }
//        };
//        requestQueue.add(jsonObjectRequest);
//    }
//
//    public static void initialize()
//    {
//        scheduledExecutorService = Executors.newScheduledThreadPool(1);
//    }
//
//    public static void shutdownTheScheduler()
//    {
//        scheduledExecutorService.shutdown();
//    }
//
//}
