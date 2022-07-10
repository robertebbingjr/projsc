package com.happenstance.projsc.about;

import androidx.appcompat.app.AppCompatActivity;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.happenstance.projsc.R;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        TextView tvVersion = findViewById(R.id.tvVersion);

        String version = "";
        long versionCode = 0;
        String versionDisplay = "";

        try {
            PackageInfo packageInfo = this.getPackageManager().getPackageInfo(getPackageName(), 0);
            version = packageInfo.versionName;
            versionCode = packageInfo.getLongVersionCode();
            versionDisplay = "Version " + version + " (" + String.valueOf(versionCode) + ")";
        } catch (PackageManager.NameNotFoundException e) {
            Toast.makeText(this, "Error retrieving version info", Toast.LENGTH_SHORT).show();
        }
        tvVersion.setText(versionDisplay);
    }
}