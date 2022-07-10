package com.example.VlcStream.FetchComputerFiles;

import static com.example.VlcStream.FetchComputerFiles.ComputerFileAdapter.handleChange;
import static com.example.VlcStream.FetchComputerFiles.FetchComputerData.computerFileContainer;
import static com.example.VlcStream.FetchComputerFiles.FetchComputerData.fileNameContainer;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.EmptyStackException;
import java.util.HashMap;
import java.util.Map;

public class FetchDetailedComputerFile
{
    static RequestQueue requestQueue;
    static SharedPreferences sharedPreferences;
    ComputerFileAdapter recyclerAdapter;


    public void fetchTheComputerFiles(String url, Context context, boolean isFromBackButton, ComputerFileAdapter recyclerAdapter) {

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String serverIp = sharedPreferences.getString("ServerIpAddress", "No ip provided");
        String luaPassword = sharedPreferences.getString("Lua Password", "No password provided");
        String serverPort = sharedPreferences.getString("ServerPort", "No port is proveded");

        String textViewUrl = url;
        this.recyclerAdapter = recyclerAdapter;
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        url = "http://" + serverIp + ":" + serverPort + "/requests/browse.json?uri=" + url;

        if(requestQueue == null)
        {
            requestQueue = Volley.newRequestQueue(context.getApplicationContext());
        }

        if (computerFileContainer != null)
        {
            //clear the previous data
            computerFileContainer.clear();
        }

        if (fileNameContainer != null)
        {
            //remove the current list
            fileNameContainer.clear();
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try{
                        //Toast.makeText(getContext(), "Getachew", Toast.LENGTH_SHORT).show();
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

                        if (isFromBackButton)
                        {

                            //this checks that when a user is browsing a history and reachs at the top of it's parent
                            if (ComputerFileAdapter.historyPathContainer.size() == 0)
                            {
                                FetchComputerData.theTopPath = "file:///";
                            }
                        }

                        if (ComputerFileAdapter.historyPathContainer.size() != 0)
                        {
                            FetchComputerData.textView.setText( textViewUrl);
                            FetchComputerData.theTopPath = textViewUrl;
                        }

                        //Now notify for the new data changed
                        handleChange(FetchComputerData.recyclerView, recyclerAdapter );
                    } catch (JSONException e) {
                        Toast.makeText(context, "Error fetching your data", Toast.LENGTH_SHORT).show();
                        Log.e("this is tag", "" +  e.getMessage());
                    }
                }, error -> {

                if (error != null && !textViewUrl.substring(textViewUrl.lastIndexOf("/") + 1).equals("root"))
                {
                    //clear for the list for any error happened
                    computerFileContainer.clear();
                    fileNameContainer.clear();
                    ComputerFileAdapter.historyPathContainer.clear();
                    FetchComputerData.textView.setText("file:///");
                    FetchComputerData.theTopPath = "file:///";
                    handleChange(FetchComputerData.recyclerView, recyclerAdapter);
                    FetchComputerData.doesPreviouslyFetched  = false;
                }

                try
                {
                    //remove the top bad uri which is either root which have not read permission or any other bad uri
                    ComputerFileAdapter.historyPathContainer.pop();
                    fetchTheComputerFiles(ComputerFileAdapter.historyPathContainer.peek(), context, false, recyclerAdapter);
                }

                catch (EmptyStackException e)
                {
                    Log.e("message", "Stream error called");
                    Toast.makeText(context, "Error Streaming the file", Toast.LENGTH_SHORT).show();
                }

        }) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> param = new HashMap<>();
                String credential = "" + ":" + luaPassword;
                String auth = "Basic " + android.util.Base64.encodeToString(credential.getBytes(), Base64.NO_WRAP);
                param.put("Authorization", auth);
                return param;
            }
        };
        requestQueue.add(jsonObjectRequest);
    }
}
