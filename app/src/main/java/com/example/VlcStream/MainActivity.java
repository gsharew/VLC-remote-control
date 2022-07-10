package com.example.VlcStream;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Menu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;

import com.example.VlcStream.BasicControl.Playlist;
import com.example.VlcStream.BasicControl.HandleBasicRequest;
import com.example.VlcStream.BasicControl.PlaylistModel;
import com.example.VlcStream.BasicControl.RecyclerAdapter;
import com.example.VlcStream.ComputerControl.Control;
import com.example.VlcStream.ui.TabPagerAdapter;
import com.example.VlcStream.ui.About.About;
import com.example.VlcStream.ui.Contactus.ContactUs;
import com.example.VlcStream.ui.Manual.UserManual;
import com.example.VlcStream.ui.Settings.Settings;
import com.google.android.material.navigation.NavigationView;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

import java.io.IOException;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

     DrawerLayout drawerLayout;
     ActionBarDrawerToggle actionBarDrawerToggle;
     Toolbar toolbar;
     NavigationView navigationView;
     TabLayout tabLayout;
     ViewPager viewPager;
     Toast toast;
     boolean onbackpressed;
     SharedPreferences sharedPreferences;

    @SuppressLint("ResourceAsColor")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        drawerLayout  = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        toolbar  =  findViewById(R.id.toolbar);
        tabLayout = findViewById(R.id.tabs);
        viewPager = findViewById(R.id.viewpager);

        //changing the bottom navigation background color
        //getWindow().setNavigationBarColor(getResources().getColor(R.color.purple_200));

        //setting up the tab layout with fragment attached to it
        tabLayout.addTab(tabLayout.newTab().setText("Playlist"));
        tabLayout.addTab(tabLayout.newTab().setText("Control"));
        tabLayout.addTab(tabLayout.newTab().setText("Files"));

        tabLayout.setTabGravity(TabLayout.GRAVITY_CENTER);

        final TabPagerAdapter adapter = new TabPagerAdapter(this,getSupportFragmentManager(), tabLayout.getTabCount());
        viewPager.setAdapter(adapter);

        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

    //now time to setup navigation drawer
        setSupportActionBar(toolbar);
        actionBarDrawerToggle = new ActionBarDrawerToggle(MainActivity.this, drawerLayout,toolbar,R.string.open, R.string.close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        else
        {
            Toast.makeText(this, "your device does not support action bar", Toast.LENGTH_SHORT).show();
            return;
        }

        actionBarDrawerToggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String themeValue = sharedPreferences.getString("darkTheme", "");

        if(themeValue.equals("enabled"))
        {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }

        else
        {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.main, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_share)
        {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/html");
            intent.putExtra(Intent.EXTRA_SUBJECT, "Share");
            intent.putExtra(Intent.EXTRA_TEXT, "http://play.google.com/store/apps/detailsid=com.android.apps.VlcStream");
            startActivity(Intent.createChooser(intent, "Share Via"));
        }

        else if(item.getItemId() == R.id.clearPlayList)
        {
            Executors.newSingleThreadExecutor().execute(() ->
            {
                HandleBasicRequest.notifyVLC(getApplicationContext(), "pl_empty");
            });
        }

        else if(item.getItemId() == R.id.fullScreen)
        {
            Executors.newSingleThreadExecutor().execute(() -> {
                HandleBasicRequest.notifyVLC(getApplicationContext(), "fullscreen");
            });
        }

        else if(item.getItemId() == R.id.Inorder)
        {
            for (int i = 0; i < Playlist.musicContainer.size(); i++)
            {
                for (int j = i + 1; j < Playlist.musicContainer.size(); j++)
                {
                     if (Playlist.musicContainer.get(i).compareTo(Playlist.musicContainer.get(j)) > 0)
                     {
                         String musicName = Playlist.musicContainer.get(i).trim();
                         Playlist.musicContainer.set(i, Playlist.musicContainer.get(j).trim());
                         Playlist.musicContainer.set(j, musicName);

                         PlaylistModel playlistModel = Playlist.playlistInformation.get(i);
                         Playlist.playlistInformation.set(i, Playlist.playlistInformation.get(j));
                         Playlist.playlistInformation.set(j, playlistModel);
                     }
                }
            }
             String command = "pl_sort&id=0&val=title";
             HandleBasicRequest.notifyVLC(getApplicationContext(), command);
             RecyclerAdapter.notifyChange();
        }

        else if(item.getItemId() == R.id.DisOrder)
        {
            for (int i = 0; i < Playlist.musicContainer.size(); i++)
            {
                for (int j = i + 1; j < Playlist.musicContainer.size(); j++)
                {
                    if (Playlist.musicContainer.get(i).compareTo(Playlist.musicContainer.get(j)) < 0)
                    {
                        String musicName = Playlist.musicContainer.get(i).trim();
                        Playlist.musicContainer.set(i, Playlist.musicContainer.get(j).trim());
                        Playlist.musicContainer.set(j, musicName);

                        PlaylistModel playlistModel = Playlist.playlistInformation.get(i);
                        Playlist.playlistInformation.set(i, Playlist.playlistInformation.get(j));
                        Playlist.playlistInformation.set(j, playlistModel);
                    }
                }
            }
            String command = "pl_sort&id=1&val=title";
            HandleBasicRequest.notifyVLC(getApplicationContext(), command);
            RecyclerAdapter.notifyChange();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.nav_home)
        {
            //close the drawer
            drawerLayout.closeDrawer(GravityCompat.START);
        }

        else if (item.getItemId() == R.id.nav_usermanual)
        {
                startActivity(new Intent(this, UserManual.class));
                drawerLayout.closeDrawer(GravityCompat.START);
        }
        else if (item.getItemId() == R.id.nav_settings)
        {
            startActivity(new Intent(this, Settings.class));
            drawerLayout.closeDrawer(GravityCompat.START);
        }
        else if (item.getItemId() == R.id.nav_contactus)
        {
            drawerLayout.closeDrawer(GravityCompat.START);
            Intent intent = ContactUs.sendMail();
            startActivity(intent);
        }
        else if (item.getItemId() == R.id.nav_about)
        {
            startActivity(new Intent(this, About.class));
            drawerLayout.closeDrawer(GravityCompat.START);
        }
        return false;
    }

    @Override
    public void onBackPressed() {

        //this the below line of code is used to get the current selected tab position
        //int tabPosition = tabLayout.getSelectedTabPosition();
        //Toast.makeText(this, "The selected tab position is: " + tabPosition, Toast.LENGTH_SHORT).show();
        if (drawerLayout.isDrawerOpen(GravityCompat.START))
        {
              drawerLayout.closeDrawer(GravityCompat.START);
              onbackpressed = false;
        }

        else if (!onbackpressed)
        {
           toast= Toast.makeText(this, "Press again to exit",Toast.LENGTH_SHORT);
           toast.show();
        }

        else
        {
               toast.cancel();
               if(Control.socket != null) {
                   try {
                       Control.socket.close();
                   } catch (IOException e) {
                       e.printStackTrace();
                   }
               }
               finish();
        }

        Runnable runnable = () -> {
            onbackpressed = true;
            expireTheBackPressedLifeTime();
        };
        new Handler().postDelayed(runnable, 100);
    }

    private void expireTheBackPressedLifeTime() {
        Runnable runnable = () -> onbackpressed = false;
        //call the runnable object after 1second then it will make expire the time
        new Handler().postDelayed(runnable,1000);
    }
}
