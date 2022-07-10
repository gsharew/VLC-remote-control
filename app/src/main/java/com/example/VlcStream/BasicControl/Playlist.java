package com.example.VlcStream.BasicControl;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.VlcStream.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Playlist extends Fragment implements  View.OnClickListener, SwipeRefreshLayout.OnRefreshListener {
    static RequestQueue requestQueue = null;
    TextView textView, startPosition, endPosition, artistName;
    public static RecyclerView recyclerView;
    public RecyclerAdapter recyclerAdapter;
    public static ArrayList<PlaylistModel> playlistInformation = new ArrayList<>();
    public static ArrayList<String> musicContainer = new ArrayList<>();
    public static boolean doesPreviouslyFetchd = false;
    public static String vlcState = "paused", shuffleStatus = "notShuffled";
    private ImageView pauseImage, playPreviousImage, seekBackwardImage, seekForwardImage, playNextImage, volumeImage, shuffleImage, repeatImage;
    SharedPreferences sharedPreferences;
    static ActionBar actionBar;
    boolean StateChanged = false, connected;
    static boolean doesTheMusicMuted = false, doesLoopEnabled = false;
    ConstraintLayout constraintLayout;
    SeekBar seekBar, musicPosition;
    static int lastSeekBarValue = 0;
    SwipeRefreshLayout swipeRefreshLayout;

    @Override
    public void onPause() {
        SyncForStateOfVlc.shutdownTheScheduler();
        super.onPause();
    }

    @Override
    public void onResume() {
        startTheThread2Again(getContext());
        super.onResume();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_playlist, container, false);
        textView = view.findViewById(R.id.Music_Name);
        recyclerView = view.findViewById(R.id.recyclerView);
        seekBar = view.findViewById(R.id.seekbar);
        musicPosition = view.findViewById(R.id.music_position);
        startPosition = view.findViewById(R.id.startTime);
        endPosition = view.findViewById(R.id.endTime);
        artistName = view.findViewById(R.id.artist);

        swipeRefreshLayout = view.findViewById(R.id.swipeLayout);
        swipeRefreshLayout.setOnRefreshListener(this);

        shuffleImage = view.findViewById(R.id.shuffle);
        playPreviousImage= view.findViewById(R.id.previous);
        seekBackwardImage = view.findViewById(R.id.seekbackward);
        seekForwardImage = view.findViewById(R.id.seekforward);
        pauseImage = view.findViewById(R.id.pause);
        playNextImage = view.findViewById(R.id.next);
        volumeImage = view.findViewById(R.id.volume);
        constraintLayout = view.findViewById(R.id.constraintLayout);
        repeatImage = view.findViewById(R.id.repeat);

        shuffleImage.setOnClickListener(this);
        playPreviousImage.setOnClickListener(this);
        seekBackwardImage.setOnClickListener(this);
        seekForwardImage.setOnClickListener(this);
        pauseImage.setOnClickListener(this);
        playNextImage.setOnClickListener(this);
        playPreviousImage.setOnClickListener(this);
        volumeImage.setOnClickListener(this);
        repeatImage.setOnClickListener(this);

        //if we create a volley library everytime we could possibly get memory leak problem on any device.
        if(requestQueue == null)
        requestQueue = Volley.newRequestQueue(getActivity().getApplicationContext());

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                lastSeekBarValue = i;
                //check if the seekbar is changed by the user
                if(b)
                HandleBasicRequest.notifyVLC(getContext(), "volume&val=" + i*4);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        musicPosition.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                //check if the seekbar is changed by the user
                if(b)
                HandleBasicRequest.notifyVLC(getContext(), "seek&val=" + i + "s");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                startTheThread2Again(getContext());
            }
        });

        return  view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        actionBar =  ((AppCompatActivity)getActivity()).getSupportActionBar();

        //for not let fetch the data if the connection is not established
        if (checkIfaUserIsConnectdToWifi()) {

            if (!doesPreviouslyFetchd)
            {
                actionBar.setTitle("Vlc Stream");
                fetchTheData();
            }

            else
            {
                renderTheRecyclerViewItems();
            }

        }
        else
        {
            pauseImage.setImageResource(R.drawable.play);
            vlcState = "paused";
            actionBar.setTitle("Not Connected");
        }
    }

    public void fetchTheData() {
        if (playlistInformation != null && playlistInformation.size() !=0 && musicContainer != null && musicContainer.size() != 0)
        {
        	//this line of code helps us to remove the previous stored music informations and then when ever the user navigate to the 				app it will restore the app
            playlistInformation.clear();

            //this line of code also support the previous music removal.
            musicContainer.clear();
            renderTheRecyclerViewItems();
        }

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        String serverIp = sharedPreferences.getString("ServerIpAddress", null);
        String luaPassword = sharedPreferences.getString("Lua Password", null);
        String serverPort = sharedPreferences.getString("ServerPort", null);
        if (serverIp == null)
        {
            Toast.makeText(getContext(), "Set up your ip first", Toast.LENGTH_SHORT).show();
            return;
        }

        if (luaPassword == null)
        {
            Toast.makeText(getContext(), "lua password required", Toast.LENGTH_SHORT).show();
            return;
        }
        if(serverPort == null)
        {
            Toast.makeText(getContext(), "server port is empty", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = "http://" + serverIp + ":" + serverPort + "/requests/playlist.json";
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, null,
                response -> {
                    try{
                            JSONArray jsonArray = response.getJSONArray("children").getJSONObject(0).getJSONArray("children");
                            for (int i = 0; i < jsonArray.length(); i++)
                            {
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                String name = jsonObject.getString("name");
                                String id = jsonObject.getString("id");
                                String uri = jsonObject.getString("uri");
                                playlistInformation.add(new PlaylistModel(name, id, uri));
                                musicContainer.add(name);
                            }

                        //set doesPreviouslyFetched boolean value to true becauase we already got the data
                        doesPreviouslyFetchd = true;

                        renderTheRecyclerViewItems();
                        StateChanged = true;
                        constraintLayout.setVisibility(View.VISIBLE);
                       } catch (JSONException e) {
                        //Toast.makeText(getContext(), "Error fetching your data", Toast.LENGTH_SHORT).show();
                        constraintLayout.setVisibility(View.GONE);
                        Log.e("this is tag", "" +  e.getMessage());
                    }
                }, error -> {

            }) {

            @Override
            public Map<String, String> getHeaders() {
                        HashMap<String, String> param = new HashMap<>();
                        String credential = "" + ":" + luaPassword;
                        String auth = "Basic " + Base64.encodeToString(credential.getBytes(), Base64.NO_WRAP);
                        param.put("Authorization", auth);
                        return param;
            }

        };

        //this is a retry policy for volley
        jsonObjectRequest.setRetryPolicy( new DefaultRetryPolicy(100,DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        requestQueue.add(jsonObjectRequest);
    }

    private boolean checkIfaUserIsConnectdToWifi()
    {
         connected = false;
        ConnectivityManager connectivityManager = (ConnectivityManager)getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
                //we are connected to a network
                connected = true;
        }
        return true;
    }

    @Override
    public void onClick(View view) {
        if ( view == shuffleImage )
        {
            if (shuffleStatus.equals("notShuffled"))
            {
                shuffleImage.setImageResource(R.drawable.shuffle_on);
                shuffleStatus = "Shuffled";
            }

            else
            {
                shuffleImage.setImageResource(R.drawable.shuffle);
                shuffleStatus = "notShuffled";
            }

                //toggle between the random value status
                HandleBasicRequest.notifyVLC(getContext(), "pl_random");
        }

        else if ( view == playPreviousImage )
        {
			    HandleBasicRequest.notifyVLC(getContext(), "pl_previous");
			    pauseImage.setImageResource(R.drawable.pause);
        }

        else if ( view == seekBackwardImage )
        {
		        HandleBasicRequest.notifyVLC(getContext(), "seek&val=-10");
		        pauseImage.setImageResource(R.drawable.pause);
        }

        else if ( view == seekForwardImage )
        {
            HandleBasicRequest.notifyVLC(getContext(), "seek&val=+10");
            pauseImage.setImageResource(R.drawable.pause);
        }

        else if ( view == pauseImage )
        {
            if (vlcState.equals("playing"))
            {
                //pause the music
                vlcState = "paused";
                pauseImage.setImageResource(R.drawable.play);
            }
            else
            {
                vlcState = "playing";
                pauseImage.setImageResource(R.drawable.pause);
            }

            //toggles the state of the vlc
            HandleBasicRequest.notifyVLC(getContext(), "pl_pause");
        }

        else if ( view == playNextImage )
        {
            HandleBasicRequest.notifyVLC(getContext(), "pl_next");
            pauseImage.setImageResource(R.drawable.pause);
        }

        else if ( view == volumeImage)
        {
            if (!doesTheMusicMuted)
            {
                volumeImage.setImageResource(R.drawable.mute);
                doesTheMusicMuted = true;
                HandleBasicRequest.notifyVLC(getContext(), "volume&val=" + 0);
            }

            else
            {
                volumeImage.setImageResource(R.drawable.volume);
                doesTheMusicMuted = false;
                HandleBasicRequest.notifyVLC(getContext(), "volume&val=" + lastSeekBarValue * 4);
            }
        }
        else if (view == repeatImage)
        {
            if (!doesLoopEnabled)
            {
                doesLoopEnabled = true;
                repeatImage.setImageResource(R.drawable.repeat_on);
            }
            else
            {
                doesLoopEnabled = false;
                repeatImage.setImageResource(R.drawable.repeat);
            }
            HandleBasicRequest.notifyVLC(getContext(), "pl_repeat");
        }
    }

    public void renderTheRecyclerViewItems()
    {
        recyclerAdapter = new RecyclerAdapter(getContext(), musicContainer, recyclerView);
        recyclerView.setAdapter(recyclerAdapter);
    }


    public static void setVlcState(String state)
    {
        vlcState = state;
    }

    public  static  String getVlcState()
    {
        return  vlcState;
    }

    public void startTheThread2Again(Context context)
    {
        SyncForStateOfVlc.initialize();
        SyncForStateOfVlc syncInBackgroundForVlc = new SyncForStateOfVlc(context, pauseImage, shuffleImage, repeatImage, seekBar, startPosition, endPosition, musicPosition, artistName);
        syncInBackgroundForVlc.run();
    }

    @Override
    public void onRefresh() {
        //now refersh the recycler view data
        fetchTheData();
        swipeRefreshLayout.setRefreshing(false);
    }
}
