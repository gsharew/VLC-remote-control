package com.example.VlcStream.BasicControl;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.util.Log;

import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

public class HandleBasicRequest {
    static RequestQueue requestQueue;
    static SharedPreferences sharedPreferences;
    public static void notifyVLC(Context context, String command) {
        if(requestQueue == null)
        {
            requestQueue = Volley.newRequestQueue(context);
        }

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        //sharedPreferences = getContext().getSharedPreferences("settings", Context.MODE_PRIVATE);
        String serverIp = sharedPreferences.getString("ServerIpAddress", null);
        String luaPassword = sharedPreferences.getString("Lua Password", null);
        String serverPort = sharedPreferences.getString("ServerPort", null);

        if (serverIp == null)
        {
            //Toast.makeText(context, "Set up your ip first", Toast.LENGTH_SHORT).show();
            return;
        }

        if (luaPassword == null)
        {
            //Toast.makeText(context, "Please setup a lua password first", Toast.LENGTH_SHORT).show();
            return;
        }

        if(serverPort == null)
        {
            //Toast.makeText(context, "Server port is empty", Toast.LENGTH_SHORT).show();
        }

        String url = "http://" + serverIp + ":" + serverPort + "/requests/status.json?command=" + command;
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    if (!command.equalsIgnoreCase("pl_pause") && !command.equalsIgnoreCase("pl_repeat"))
                    {
                        Playlist.vlcState = "playing";
                    }

                }, error -> {
                    Playlist.recyclerView.removeAllViewsInLayout();
                    Log.e("this is tag", "" + error.getMessage());
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
        requestQueue.add(stringRequest);
    }
}
