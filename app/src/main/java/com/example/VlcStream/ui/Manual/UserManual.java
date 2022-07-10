package com.example.VlcStream.ui.Manual;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.denzcoskun.imageslider.ImageSlider;
import com.denzcoskun.imageslider.models.SlideModel;
import com.example.VlcStream.R;

import java.util.ArrayList;
import java.util.List;

public class UserManual extends AppCompatActivity {
    ImageSlider imageSlider;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.usermanual);
        imageSlider = findViewById(R.id.slider);
        startTheSlider();
        if (getSupportActionBar() != null)
        {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void startTheSlider() {
        List<SlideModel> slideModels = new ArrayList<>();
        slideModels.add(new SlideModel(R.drawable.vlc_configuration));
        slideModels.add(new SlideModel(R.drawable.ready_vlc));
        slideModels.add(new SlideModel(R.drawable.custom_server_configuration));
        imageSlider.setImageList(slideModels, true);
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
}