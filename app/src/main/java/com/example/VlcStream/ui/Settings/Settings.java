package com.example.VlcStream.ui.Settings;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.view.Gravity;
import android.view.MenuItem;

import android.view.View;
import android.widget.Button;

import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;

import com.example.VlcStream.BasicControl.Playlist;
import com.example.VlcStream.FetchComputerFiles.FetchComputerData;

import com.example.VlcStream.MainActivity;
import com.example.VlcStream.R;
import com.gitonway.lee.niftymodaldialogeffects.lib.Effectstype;
import com.gitonway.lee.niftymodaldialogeffects.lib.NiftyDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Objects;

public class Settings extends AppCompatActivity implements View.OnClickListener {
    static SharedPreferences sharedPreferences;
    SwitchCompat volumeKeySwitch, darkTheme;
    TextView changeServerIP, changeLuaPassword, changePcPassword, serverPort;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting);

        volumeKeySwitch = findViewById(R.id.volumekey_switch);
        changeLuaPassword = findViewById(R.id.luapassword);
        changeServerIP = findViewById(R.id.serverip);
        changePcPassword = findViewById(R.id.PCPassword);
        serverPort = findViewById(R.id.serverPort);
        darkTheme = findViewById(R.id.darkTheme);

        serverPort.setOnClickListener(this);
        changeLuaPassword.setOnClickListener(this);
        changeServerIP.setOnClickListener(this);
        changePcPassword.setOnClickListener(this);
        
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        //sharedPreferences = getApplicationContext().getSharedPreferences("setting", MODE_PRIVATE);

        initialiseTheSetting();
        if (getSupportActionBar() != null)
        {
            getSupportActionBar().setElevation(0);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        darkTheme.setOnCheckedChangeListener((compoundButton, isChecked) ->{
            SharedPreferences.Editor editor = sharedPreferences.edit();
            if (isChecked)
            {
                editor.putString("darkTheme", "enabled");
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            }

            else
            {
                editor.putString("darkTheme", "disabled");
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }

            editor.apply();
        });

        volumeKeySwitch.setOnCheckedChangeListener((compoundButton, isChecked) -> {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            if (isChecked)
            {
                editor.putString("volumeAdjustmentSwitched", "switched");
            }

            else
            {
                editor.putString("volumeAdjustmentSwitched", "");
            }
            editor.apply();
        });
    }

    private void initialiseTheSetting(){
        String volumeKeyValue = sharedPreferences.getString("volumeAdjustmentSwitched", "");
        String darkthemevalue = sharedPreferences.getString("darkTheme", "");
        if (volumeKeyValue.length() != 0)
        {
            volumeKeySwitch.setChecked(true);
        }

        if(darkthemevalue.equals("enabled"))
        {
            darkTheme.setChecked(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home)
        {
            this.finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
    if (view == changeLuaPassword)
    {
        openTheLuaPasswordChanger(view);
    }

    else if (view == changeServerIP)
    {
        openTheServerIpChanger(view);
    }
    
    else if(view == changePcPassword)
    {
        openPcPasswordChanger(view);
    }
    else if(view == serverPort)
    {
        openPcPortChanger(view);
    }
    }

    private void openPcPasswordChanger(View v) {
        NiftyDialogBuilder dialogBuilder = NiftyDialogBuilder.getInstance(this);
        View view = getLayoutInflater().inflate(R.layout.password_modifier_layout, null);
        Button button =  view.findViewById(R.id.update);
        TextInputEditText textInputEditText = view.findViewById(R.id.value);
        textInputEditText.setInputType(InputType.TYPE_CLASS_TEXT);
        String IpAddress = sharedPreferences.getString("PcPassword", "");
        textInputEditText.setText(IpAddress);
        dialogBuilder
                .withTitle("Change Ip")                                  //.withTitle(null)  no title
                .withTitleColor("#FFFFFFFF")                                  //def
                .withDividerColor("#11000000")                              //def
                .withDialogColor("#1B313A")                             //def  | withDialogColor(int resid)
                .withMessage("                New Pc Password")
                .withMessageColor("#FFFFFFFF")
                .withIcon(getResources().getDrawable(R.drawable.info))
                .withDuration(100)                                          //def
                .withEffect(Effectstype.Slidetop)                                       //def Effectstype.Slidetop                 //def gone
                .isCancelableOnTouchOutside(true)                           //def    | isCancelable(true)
                .setCustomView(view,v.getContext())         //.setCustomView(View or ResId,context)
                .show();

        button.setOnClickListener(view12 -> {
            if (Objects.requireNonNull(textInputEditText.getText()).toString().length() == 0)
            {
                textInputEditText.setError("Invalid Password");
            }
            else
            {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("PcPassword",textInputEditText.getText().toString());
                editor.apply();
                //still notify for the changing of ip address
                Playlist.doesPreviouslyFetchd = false;
                FetchComputerData.doesPreviouslyFetched = false;
                //show the success toast message
                View view1 = getLayoutInflater().inflate(R.layout.toast, null);
                Toast toast = new Toast(getApplicationContext());
                toast.setGravity(Gravity.TOP,0,15);
                toast.setDuration(Toast.LENGTH_SHORT);
                toast.setView(view1);
                toast.show();
                dialogBuilder.cancel();
            }

        });
    }

    private void openTheServerIpChanger(View v) {
        NiftyDialogBuilder dialogBuilder = NiftyDialogBuilder.getInstance(this);
        View view = getLayoutInflater().inflate(R.layout.password_modifier_layout, null);
        Button button =  view.findViewById(R.id.update);
        TextInputEditText textInputEditText = view.findViewById(R.id.value);
        textInputEditText.setRawInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        String IpAddress = sharedPreferences.getString("ServerIpAddress", null);
        if (IpAddress != null)
            textInputEditText.setText(IpAddress);
            dialogBuilder
            .withTitle("Change Ip")                                  //.withTitle(null)  no title
                    .withTitleColor("#FFFFFFFF")                                  //def
                    .withDividerColor("#11000000")                              //def
                    .withDialogColor("#1B313A")                            //def  | withDialogColor(int resid)
                    .withMessage("                New Ip Address")
                    .withMessageColor("#FFFFFFFF")
                    .withIcon(getResources().getDrawable(R.drawable.info))
                    .withDuration(100)                                          //def
                    .withEffect(Effectstype.Slidetop)                                       //def Effectstype.Slidetop                 //def gone
                    .isCancelableOnTouchOutside(true)                           //def    | isCancelable(true)
                    .setCustomView(view,v.getContext())         //.setCustomView(View or ResId,context)
                    .show();

        button.setOnClickListener(view12 -> {
            if (textInputEditText.getText().toString().trim().length() == 0)
            {
                textInputEditText.setError("Invalid IP");
            }
            else
            {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("ServerIpAddress",textInputEditText.getText().toString());
                editor.apply();
                //still notify for the changing of ip address
                Playlist.doesPreviouslyFetchd = false;
                FetchComputerData.doesPreviouslyFetched = false;
                //show the success toast message
                View view1 = getLayoutInflater().inflate(R.layout.toast, null);
                Toast toast = new Toast(getApplicationContext());
                toast.setGravity(Gravity.TOP,0,15);
                toast.setDuration(Toast.LENGTH_SHORT);
                toast.setView(view1);
                toast.show();
                dialogBuilder.cancel();
                RestartTheApplication();

            }

        });
    }

    private void openTheLuaPasswordChanger(View v) {
        NiftyDialogBuilder dialogBuilder = NiftyDialogBuilder.getInstance(this);
        View view = getLayoutInflater().inflate(R.layout.password_modifier_layout, null);
        Button button = view.findViewById(R.id.update);
        TextInputEditText textInputEditText = view.findViewById(R.id.value);
        String luaPassword = sharedPreferences.getString("Lua Password", null);
        if (luaPassword != null)
        textInputEditText.setText(luaPassword);

        dialogBuilder
                .withTitle("Change Password")                                  //.withTitle(null)  no title
                .withTitleColor("#FFFFFFFF")                                  //def
                .withDividerColor("#11000000")                              //def
                .withDialogColor("#1B313A")                            //def  | withDialogColor(int resid)
                .withMessage("                New Password")
                .withMessageColor("#FFFFFFFF")
                .withIcon(getResources().getDrawable(R.drawable.info))
                .withDuration(100)                                          //def
                .withEffect(Effectstype.Slidetop)                                       //def Effects type Slidetop                 //def gone
                .isCancelableOnTouchOutside(true)                           //def    | isCancelable(true)
                .setCustomView(view,v.getContext())         //.setCustomView(View or ResId,context)
                .show();

        button.setOnClickListener(view12 -> {

            if (textInputEditText.getText().toString().trim().length() == 0)
            {
                textInputEditText.setError("Empity Password");
            }

            else
            {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("Lua Password",textInputEditText.getText().toString());
                editor.apply();
                //still notify for any changed on the password
                Playlist.doesPreviouslyFetchd = false;
                FetchComputerData.doesPreviouslyFetched = false;
                //toast the sucess message for the user
                View view1 = getLayoutInflater().inflate(R.layout.toast, null);
                Toast toast = new Toast(getApplicationContext());
                toast.setGravity(Gravity.TOP,0,15);
                toast.setDuration(Toast.LENGTH_SHORT);
                toast.setView(view1);
                toast.show();
                dialogBuilder.cancel();
                RestartTheApplication();
            }
        });
    }

    private void openPcPortChanger(View v) {
        NiftyDialogBuilder dialogBuilder = NiftyDialogBuilder.getInstance(this);
        View view = getLayoutInflater().inflate(R.layout.password_modifier_layout, null);
        Button button = view.findViewById(R.id.update);
        TextInputEditText textInputEditText = view.findViewById(R.id.value);
        TextInputLayout CustomServerPort = view.findViewById(R.id.customServerPortLayout);
        TextInputLayout textInputLayout = view.findViewById(R.id.textInputLayout);
        TextInputEditText JarPortEditText = view.findViewById(R.id.jarport);
        textInputLayout.setHint("VLC Port");

        CustomServerPort.setVisibility(View.VISIBLE);
        //Set the input type to a class type number
        textInputEditText.setInputType(InputType.TYPE_CLASS_NUMBER);
        JarPortEditText.setInputType(InputType.TYPE_CLASS_NUMBER);

        String VLCPort = sharedPreferences.getString("ServerPort", null);
        String JarPort = sharedPreferences.getString("JarPort", null);

        if (VLCPort != null)
            textInputEditText.setText(VLCPort);
        if(JarPort != null)
            JarPortEditText.setText(JarPort);
        dialogBuilder
                .withTitle("Change Server Port")                                  //.withTitle(null)  no title
                .withTitleColor("#FFFFFFFF")                                  //def
                .withDividerColor("#11000000")                              //def
                .withDialogColor("#1B313A")                              //def  | withDialogColor(int resid)
                .withMessage("                New Port")
                .withMessageColor("#FFFFFFFF")
                .withIcon(getResources().getDrawable(R.drawable.info))
                .withDuration(100)                                          //def
                .withEffect(Effectstype.Slidetop)                                       //def Effects type Slidetop                 //def gone
                .isCancelableOnTouchOutside(true)                           //def    | isCancelable(true)
                .setCustomView(view,v.getContext())         //.setCustomView(View or ResId,context)
                .show();

        button.setOnClickListener(view12 -> {

            if (textInputEditText.getText().toString().trim().length() == 0)
            {
                textInputEditText.setError("Empty Port");
            }

            else if(JarPortEditText.getText().toString().trim().length() == 0)
            {
                JarPortEditText.setError("Empty Port");
            }

            else
            {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("ServerPort",textInputEditText.getText().toString());
                editor.putString("JarPort", JarPortEditText.getText().toString());
                editor.apply();

                //toast the sucess message for the user
                View view1 = getLayoutInflater().inflate(R.layout.toast, null);
                Toast toast = new Toast(getApplicationContext());
                toast.setGravity(Gravity.TOP,0,15);
                toast.setDuration(Toast.LENGTH_SHORT);
                toast.setView(view1);
                toast.show();
                dialogBuilder.cancel();
                RestartTheApplication();
            }
        });
    }

    private void RestartTheApplication() {
        Intent mStartActivity = new Intent(getApplicationContext(), MainActivity.class);
        int mPendingIntentId = 123456;
        PendingIntent mPendingIntent = PendingIntent.getActivity(getApplicationContext(), mPendingIntentId,    mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager mgr = (AlarmManager)getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        Runnable runnable  = () -> {
            System.exit(0);
            mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
        };
        new Handler().postDelayed(runnable, 1000);
    }
}