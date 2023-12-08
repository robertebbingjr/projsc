package com.happenstance.projsc;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.happenstance.projsc.constants.RemoteConfigKeys;
import com.happenstance.projsc.constants.SharedPrefVariables;
import com.happenstance.projsc.exception_handler.ExceptionHandler;
import com.happenstance.projsc.utils.SharedPrefUtil;

import java.util.concurrent.TimeUnit;

public class SplashActivity extends AppCompatActivity {
    public static final String TAG = "SplashActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this));
        setContentView(R.layout.activity_splash);

        final FirebaseRemoteConfig firebaseRemoteConfig = FirebaseRemoteConfig.getInstance();

//        long fetchInterval = com.happenstance.projsc.BuildConfig.DEBUG
//                ? 0
//                : TimeUnit.HOURS.toSeconds(1);
        long fetchInterval = TimeUnit.HOURS.toSeconds(1);

        firebaseRemoteConfig.fetch(fetchInterval)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            //Log.d(TAG, "remote config is fetched.");
                            firebaseRemoteConfig.activate().addOnCompleteListener(new OnCompleteListener<Boolean>() {
                                @Override
                                public void onComplete(@NonNull Task<Boolean> task) {
//                                    AdsUtil.initializeAdUnitIds(SplashActivity.this);
                                    initializeRemoteConfigVars();

                                    android.os.Handler handler = new android.os.Handler();
                                    handler.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            runApp();
                                        }
                                    }, 1200);
                                }
                            });
                        } else {
                            runApp();
                        }
                    }
                });
    }

    private void runApp() {
        startActivity(new Intent(SplashActivity.this, MainActivity.class));
        finish();
    }

    private void initializeRemoteConfigVars() {
        FirebaseRemoteConfig remoteConfig = FirebaseRemoteConfig.getInstance();

        boolean adSwitch = remoteConfig.getBoolean(RemoteConfigKeys.AD_SWITCH);

        SharedPrefUtil sharedPrefUtil = new SharedPrefUtil(this);
        sharedPrefUtil.setBoolean(SharedPrefVariables.AD_SWITCH, adSwitch);
    }
}