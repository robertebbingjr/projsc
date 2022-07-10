package com.happenstance.projsc.preferences;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.happenstance.projsc.R;
import com.happenstance.projsc.exception_handler.ExceptionHandler;
//import com.happenstance.projsc.models.InterstitialAdObject;

public class SettingsActivity extends AppCompatActivity {
    private static final String TAG = "SettingsActivity";

//    private InterstitialAdObject interstitialAdObject = new InterstitialAdObject();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this));
        setContentView(R.layout.activity_settings);

        AdView avMain = findViewById(R.id.adView);
        FrameLayout flBanner = findViewById(R.id.flBanner);

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
                AdRequest adRequest = new AdRequest.Builder().build();
                avMain.loadAd(adRequest);
                avMain.setAdListener(new AdListener() {
                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        super.onAdFailedToLoad(loadAdError);
                        Log.e(TAG, "Failed to load Admob banner: " + loadAdError.getMessage());
                    }

                    @Override
                    public void onAdLoaded() {
                        super.onAdLoaded();
                        flBanner.setVisibility(View.VISIBLE);
                        Log.i(TAG, "Successfully loaded Admob banner");
                    }
                });
            }
        });

        // below line is to change
        // the title of our action bar.
        getSupportActionBar().setTitle("Settings");

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.idFrameLayout, new SettingsFragment())
                .commit();
    }

//    @Override
//    public void onBackPressed() {
//        super.onBackPressed();
//
//        if (interstitialAdObject.getInterstitialAd() != null) {
//            interstitialAdObject.getInterstitialAd().show(this);
//        }
//    }
}