package com.example.VlcStream.FetchComputerFiles;
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
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FetchComputerData extends Fragment implements View.OnClickListener {

    public static RecyclerView recyclerView;
    public ComputerFileAdapter recyclerAdapter;
    static RequestQueue requestQueue;
    public static ArrayList<ComputerFileModel> computerFileContainer = new ArrayList<>();
    public static ArrayList<String> fileNameContainer = new ArrayList<>();
    ImageView cardHistoryFetcherButton;
    public  static TextView textView;
    public static boolean doesPreviouslyFetched = false;
    static String theTopPath = "Path";
    SharedPreferences sharedPreferences;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_computer_file, container, false);
        recyclerView = view.findViewById(R.id.recyclerView);
        textView = view.findViewById(R.id.pathView);
        textView.setText(theTopPath);
        if(requestQueue == null)
        {
            requestQueue = Volley.newRequestQueue(getActivity().getApplicationContext());
        }

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
        cardHistoryFetcherButton = view.findViewById(R.id.FetchPrevious);
        cardHistoryFetcherButton.setOnClickListener(this);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                cardHistoryFetcherButton.setClickable(false);

                if (newState == RecyclerView.SCROLL_STATE_DRAGGING)
                {
                    cardHistoryFetcherButton.setClickable(false);
                }

                if (newState == RecyclerView.SCROLL_STATE_IDLE)
                {
                    cardHistoryFetcherButton.setClickable(true);
                }

            }
        });

        return  view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //Toast.makeText(getContext(), "" + doesPreviouslyFetched, Toast.LENGTH_SHORT).show();
            if (!doesPreviouslyFetched)
            {
                sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
                String serverIp = sharedPreferences.getString("ServerIpAddress", "");
                String serverPort = sharedPreferences.getString("ServerPort", "");
                String url = "http://" + serverIp + ":" + serverPort + "/requests/browse.json?uri=file:///";
                ExecutorService executorService;
                executorService = Executors.newSingleThreadExecutor();
                executorService.execute(() -> fetchTheComputerFiles(url));
            }

            else
            {
                ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle("Vlc Stream");
                renderTheRecyclerViewItems();
            }
    }

    public void fetchTheComputerFiles(String url) {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        String luaPassword = sharedPreferences.getString("Lua Password", "Not Configured");
        if (computerFileContainer != null)
        {
            computerFileContainer.clear();
        }

        if (fileNameContainer != null)
        {
            fileNameContainer.clear();
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try{
                        JSONArray jsonArray = response.getJSONArray("element");
                        for (int i = 0; i < jsonArray.length(); i++)
                        {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            String name = jsonObject.getString("name");
                            String uri = jsonObject.getString("uri");
                            String type = jsonObject.getString("type");
                            String path = jsonObject.getString("path");
                            if (!name.equals("..") && !name.equals("."))
                            {
                                computerFileContainer.add(new ComputerFileModel(name, uri, type, path));
                                fileNameContainer.add(name);
                            }

                        }
                        doesPreviouslyFetched = true;
                        textView.setText("file:///");
                        ComputerFileAdapter.historyPathContainer.push("file:///");
                        theTopPath =(String) textView.getText();
                        renderTheRecyclerViewItems();
                    } catch (JSONException e) {
                        //Toast.makeText(getContext(), "Error fetching your data", Toast.LENGTH_SHORT).show();
                        Log.e("this is tag", "" +  e.getMessage());
                    }
                }, error -> {
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

    private void renderTheRecyclerViewItems() {
        recyclerAdapter = new ComputerFileAdapter(getContext(), fileNameContainer);
        recyclerView.setAdapter(recyclerAdapter);
    }

    private boolean checkIfaUserIsConnectdToWifi()
    {
        boolean connected = false;
        ConnectivityManager connectivityManager = (ConnectivityManager)getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {

            //if you are connected to a network
            connected = true;
        }
        else
            connected = false;
        return connected;
    }

    @Override
    public void onClick(View view) {
        //now time to fetch the history
        if (view == cardHistoryFetcherButton)
        {
            if (ComputerFileAdapter.historyPathContainer.size()  <= 1)
            {
                Toast.makeText(getContext(), "Opps! you are at your home", Toast.LENGTH_SHORT).show();
            }
            else
            {
                //now remove the current parent folder
                ComputerFileAdapter.historyPathContainer.pop();
                new FetchDetailedComputerFile().fetchTheComputerFiles(ComputerFileAdapter.historyPathContainer.peek(), getContext(), true, recyclerAdapter);
            }
        }
    }
}
