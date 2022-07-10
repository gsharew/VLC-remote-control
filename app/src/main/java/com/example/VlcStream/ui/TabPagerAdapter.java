package com.example.VlcStream.ui;
import android.content.Context;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.example.VlcStream.BasicControl.Playlist;
import com.example.VlcStream.ComputerControl.Control;
import com.example.VlcStream.FetchComputerFiles.FetchComputerData;

public class TabPagerAdapter extends FragmentPagerAdapter {
    Context myContext;
    //String[] tabName = new String[]{"Audio","Control","Files"};
       int totalTabs;
    public TabPagerAdapter(Context context, FragmentManager fm, int totalTabs) {
        super(fm);
        myContext = context;
        this.totalTabs = totalTabs;
    }

    @Override
    public Fragment getItem(int position) {

        switch (position) {
            case 0:
                return new Playlist();
            case 1:
                return new Control();
            case 2:
                return new FetchComputerData();
        }
        return  null;
    }
    @Override
    public int getCount() {
        return totalTabs;
    }
}