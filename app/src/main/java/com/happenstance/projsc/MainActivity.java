// Swipe Capture
package com.happenstance.projsc;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.happenstance.projsc.constants.Broadcast;
import com.happenstance.projsc.exception_handler.ExceptionHandler;
//import com.happenstance.projsc.models.InterstitialAdObject;
import com.happenstance.projsc.preferences.SettingsActivity;
import com.happenstance.projsc.utils.Utilities;
import com.google.android.material.card.MaterialCardView;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final int MEDIA_PROJECTION_REQUEST_CODE = 100;
    private static final int REQUEST_CODE_WRITE_EXTERNAL_STORAGE = 5005;

    private boolean powerSwitch = false, isMainActivityRunning = true;
    private ImageView ivPowerSwitch;
    private TextView tvPowerSwitch;
    private Switch switchFloatingButton;

//    private InterstitialAdObject interstitialAdObject = new InterstitialAdObject();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this));
        setContentView(R.layout.activity_main);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

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

        ivPowerSwitch = findViewById(R.id.ivPowerButton);
        tvPowerSwitch = findViewById(R.id.tvPowerSwitch);
        ivPowerSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (powerSwitch) {
                    // If switch is already on, turn it off
                    powerOff();
                } else {
                    // If switch is off, turn it on
                    powerOn();
                }
            }
        });

        switchFloatingButton = findViewById(R.id.switchFloatingButton);
        switchFloatingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (switchFloatingButton.isChecked()) {
                    if (FloatingButtonService.isServiceRunning()) {
                        FloatingButtonService.showUI();
                    } else {
                        Toast.makeText(MainActivity.this, "You must switch power on first", Toast.LENGTH_SHORT).show();
                        switchFloatingButton.setChecked(false);
                    }
                } else {
                    FloatingButtonService.hideUI();
                }
            }
        });

        MaterialCardView mcvSettings = findViewById(R.id.mcvSettings);
        mcvSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
            }
        });

        BroadcastReceiver brCloseMainActivity = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // Make sure MainActivity is not running in the foreground, in which case
                // there is no need to close MainActivity upon screen capture
                if (!isMainActivityRunning)
                    finish();
            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver(brCloseMainActivity,
                new IntentFilter(Broadcast.CLOSE_MAIN_ACTIVITY));

        BroadcastReceiver brPowerOff = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // Make sure MainActivity is not running in the foreground, in which case
                // there is no need to close MainActivity upon screen capture
                powerOff();
            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver(brPowerOff,
                new IntentFilter(Broadcast.POWER_OFF));

        BroadcastReceiver brButtonToggleReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                boolean buttonSwitch = intent.getBooleanExtra(Broadcast.SHOW_BUTTON_TOGGLE_EXTRA, false);
                if (buttonSwitch) {
                    switchFloatingButton.setChecked(true);
                    FloatingButtonService.showUI();
                } else {
                    switchFloatingButton.setChecked(false);
                    FloatingButtonService.hideUI();
                }
            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver(brButtonToggleReceiver,
                new IntentFilter(Broadcast.SHOW_BUTTON_TOGGLE));

        DisplayMetrics displayMetrics = Utilities.getDisplayMetrics(this);
        if (displayMetrics == null) {
            Utilities.showErrorMessage(this, "Failed to get display metrics, screen capture may not work correctly");
        } else {
            FloatingButtonService.setDisplayMetrics(displayMetrics);
        }

        checkExternalStoragePermission();
        checkOverlayPermission();
        checkMediaProjectionPermission();
    }

    private void checkExternalStoragePermission() {
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_CODE_WRITE_EXTERNAL_STORAGE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case REQUEST_CODE_WRITE_EXTERNAL_STORAGE:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length == 0 ||
                        grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    String title = "Storage permission denied";
                    String msg = "You must grant " + getString(R.string.app_name) + " storage access permission in order to proceed";
                    String positive = "Go to Settings";
                    String negative = "Quit";
                    Runnable runOnPositive = new Runnable() {
                        @Override
                        public void run() {
                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            Uri uri = Uri.fromParts("package", getPackageName(), null);
                            intent.setData(uri);
                            startActivity(intent);
                        }
                    };
                    Runnable runOnNegative = new Runnable() {
                        @Override
                        public void run() {
                            finish();
                        }
                    };

                    Utilities.showDialogPositiveNegative(this, title, msg, positive, negative,
                            runOnPositive, runOnNegative);
                }
        }
    }

//    private ActivityResultLauncher<Intent> arlOverlayPermission =
//            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
//                @Override
//                public void onActivityResult(ActivityResult result) {
//                    if (!Settings.canDrawOverlays(MainActivity.this)) {
//                        checkOverlayPermission();
//                    }
//                }
//            });

    // method to ask user to grant the Overlay permission
    private void checkOverlayPermission() {
        if (!Settings.canDrawOverlays(this)) {
            String title = "Overlay Permission";
            String msg = "You must grant " + getString(R.string.app_name) + " permission to draw or display over other apps in order to proceed.";
            String positive = "Go to Settings";
            String negative = "Quit";
            int imageResource = R.raw.sample_overlay_permission;
            Runnable runOnPositive = new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    intent.setData(Uri.parse("package:" + getPackageName()));
                    //intent.setData(uri);
//                    arlOverlayPermission.launch(intent);
                    startActivity(intent);
                }
            };
            Runnable runOnNegative = new Runnable() {
                @Override
                public void run() {
                    finish();
                }
            };

            Utilities.showDialogPositiveNegativeWithImage(this, title, msg, positive, negative,
                    runOnPositive, runOnNegative, imageResource);
        }
    }

    private void checkMediaProjectionPermission() {
        if (!FloatingButtonService.hasConsentToken()) {
            MediaProjectionManager mProjectionManager =
                    (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
            // Obtain user permission for screen capture and handle in onActivityResult()
            Intent intentScreenCapture = mProjectionManager.createScreenCaptureIntent();
            startActivityForResult(intentScreenCapture, MEDIA_PROJECTION_REQUEST_CODE);
        }
    }

    // MediaProjectionManager.createScreenCaptureIntent() activity results in an intent data
    // with the tag EXTRA_MEDIA_PROJECTION, which is used to verify the user's consent to screen
    // capture. Hence, you pass this on to ToolsTrayService, so that it can be used as a token
    // when performing screen capture with MediaProjection service
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intentData) {
        super.onActivityResult(requestCode, resultCode, intentData);
        if (requestCode == MEDIA_PROJECTION_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                FloatingButtonService.setConsentToken(intentData);
            } else {
                Toast.makeText(this, "Failed to obtain permission to capture screen - you will not be able to take screenshots", Toast.LENGTH_SHORT).show();
                powerOff();
            }
        }
    }


    // method for starting the service
    private void startFloatingButtonService() {
        if (!Settings.canDrawOverlays(this)) {
            Utilities.showErrorMessage(this, "Failed to obtain permission to draw over other apps - app will not work properly");
        } else {
            // Check for existing service to prevent duplicate buttons
            if (!FloatingButtonService.isServiceRunning()) {
                startForegroundService(new Intent(this, FloatingButtonService.class));
            }
        }
    }

    private void refreshPowerSwitchUI() {
        // Do not touch the switchFloatingButton here, because FloatingButtonService takes some time
        // to initialize, so FloatingButtonService.isShowButtonOn() will not reflect correctly when
        // starting service
        if (powerSwitch) {
            ivPowerSwitch.setColorFilter(ContextCompat.getColor(MainActivity.this, R.color.power_on));
            tvPowerSwitch.setText("Power On");
//            if (FloatingButtonService.isShowButtonOn())
//                FloatingButtonService.showUI();
//            else
//                FloatingButtonService.hideUI();
        } else {
            ivPowerSwitch.setColorFilter(ContextCompat.getColor(MainActivity.this, R.color.power_off));
            tvPowerSwitch.setText("Power Off");
//            FloatingButtonService.hideUI();
        }
    }

    private void powerOn() {
        if (ContextCompat.checkSelfPermission(
                MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED) {
            checkExternalStoragePermission();
        } else if (!Settings.canDrawOverlays(this)) {
            checkOverlayPermission();
        } else if (!FloatingButtonService.hasConsentToken()) {
            checkMediaProjectionPermission();
        } else {
            startFloatingButtonService();
            powerSwitch = true;
            switchFloatingButton.setChecked(true);
            refreshPowerSwitchUI();
        }
    }

    private void powerOff() {
        // Although service is singleton, it starts via an instance, so in order to access the
        // service, you can do it through intent or notification, the latter is simpler option
        Intent intentDismissService = new Intent(Broadcast.STOP_FLOATING_BUTTON_SERVICE);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intentDismissService);

        powerSwitch = false;
        switchFloatingButton.setChecked(false);
        refreshPowerSwitchUI();
    }

    @Override
    protected void onResume() {
        super.onResume();

        powerSwitch = FloatingButtonService.isServiceRunning();
        if (powerSwitch) {
            switchFloatingButton.setChecked(FloatingButtonService.isShowButtonOn());
        } else {
            switchFloatingButton.setChecked(false);
        }
        refreshPowerSwitchUI();
        isMainActivityRunning = true;

//        checkCustomPermissions();
    }

    @Override
    protected void onStop() {
        super.onStop();
        isMainActivityRunning = false;
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