package com.example.VlcStream.ui.Contactus;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.VlcStream.R;

public class ContactUs{
    public static Intent sendMail()
    {
        Intent emailUsIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto","gsharewz@gmail.com", null));
        emailUsIntent.putExtra(Intent.EXTRA_SUBJECT, "Extra subject");
        return  emailUsIntent;
    }
}