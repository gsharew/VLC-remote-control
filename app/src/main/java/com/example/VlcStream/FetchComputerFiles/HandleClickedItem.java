package com.example.VlcStream.FetchComputerFiles;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

public class HandleClickedItem {
    static SharedPreferences sharedPreferences;
    static RequestQueue requestQueue;

    public static void notifyVLC(Context context, String command) {
        if(sharedPreferences == null)
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

         String luaPassword = sharedPreferences.getString("Lua Password", "Not Configured");
         String computerIP = sharedPreferences.getString("ServerIpAddress", "not found");
         String serverPort = sharedPreferences.getString("ServerPort", " no server port is provided");

        if(requestQueue == null)
        requestQueue = Volley.newRequestQueue(context);

        String url = "http://" + computerIP + ":" + serverPort + "/requests/status.json?command=in_play&input=" + command;
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {

                }, error -> {
                    if (error != null)
                    {
                            Toast.makeText(context, "Error Streaming your file", Toast.LENGTH_SHORT).show();
                            FetchComputerData.recyclerView.removeAllViews();
                    }
                    else
                    {
                        Toast.makeText(context, "you might changed the password or hostAddress", Toast.LENGTH_LONG).show();
                    }
                }) {

            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> param = new HashMap<>();
                //String credential = String.format("%s:%s","","Getachew");
                String credential = "" + ":" + luaPassword;
                String auth = "Basic " + android.util.Base64.encodeToString(credential.getBytes(), Base64.NO_WRAP);
                param.put("Authorization", auth);
                return param;
                //used to send a request using android phone android orio.
//                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
//                    String auth = "Basic " + Base64.getEncoder().encodeToString(credential.getBytes()); //this is for the java import statement
//                    //String auth = "Basic " + Base64.encodeToString(credential.getBytes(), Base64.NO_WRAP); //this is for android based level import
//                    param.put("Authorization", auth);
//                }
//                return param;
            }
        };
        requestQueue.add(stringRequest);
    }


}
