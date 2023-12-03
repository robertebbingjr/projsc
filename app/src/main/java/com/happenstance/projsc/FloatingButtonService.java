package com.happenstance.projsc;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.happenstance.projsc.constants.Broadcast;
import com.happenstance.projsc.constants.Notifications;
import com.happenstance.projsc.preferences.Preferences;
import com.happenstance.projsc.utils.Utilities;

import java.util.Calendar;

public class FloatingButtonService extends Service {
    private static final String TAG = "FloatingButtonService";

    private static Intent intentConsentToken;
    private static ForegroundWindow foregroundWindow;
    private static ImageView ivButton; //, ivDelete; //, ivCamera;
    private static boolean isServiceRunning = false, showButtonSwitch = false; //, isForegroundRunning = false;
    private static DisplayMetrics displayMetrics;

    public static DisplayMetrics getDisplayMetrics() {
        return displayMetrics;
    }

    public static void setDisplayMetrics(DisplayMetrics displayMetrics) {
        FloatingButtonService.displayMetrics = displayMetrics;
    }

    public static boolean isServiceRunning() {
        return isServiceRunning;
    }

    public static boolean isShowButtonOn() {
        return showButtonSwitch;
    }

//    public static boolean isForegroundRunning() { return isForegroundRunning; }

    public static boolean hasConsentToken() {
        return intentConsentToken != null;
    }

    public static void setConsentToken(Intent intent) {
        intentConsentToken = intent;
    }

    public static void showUI() {
        new Handler(Looper.getMainLooper()).post(new Runnable(){
            @Override
            public void run() {
                if (foregroundWindow != null && foregroundWindow.viewFloatingTool != null) {
                    foregroundWindow.viewFloatingTool.setVisibility(View.VISIBLE);
                }
                showButtonSwitch = true;
            }
        });
    }

    public static void hideUI() {
        new Handler(Looper.getMainLooper()).post(new Runnable(){
            @Override
            public void run() {
                if (foregroundWindow != null && foregroundWindow.viewFloatingTool != null) {
                    foregroundWindow.viewFloatingTool.setVisibility(View.GONE);
                }
                showButtonSwitch = false;
            }
        });
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        initializeNotification();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        isServiceRunning = true;
        foregroundWindow = new ForegroundWindow(this); //, runnableOnPress);
        foregroundWindow.attachViewFloatingTool();
        foregroundWindow.attachViewDeleteArea();
        showUI();

        return START_NOT_STICKY;
    }

    // For android version >=O, app needs to create custom notification stating foreground service is running
    private void initializeNotification() {
        NotificationChannel notificationChannel = new NotificationChannel(
                getString(R.string.notification_channel_id),
                Notifications.NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//        assert notificationManager != null;
        notificationManager.createNotificationChannel(notificationChannel);

        Intent intentNotification = new Intent(this, MainActivity.class);
        PendingIntent piNotification =
                PendingIntent.getActivity(this,0, intentNotification, PendingIntent.FLAG_IMMUTABLE);

        Intent intentShow = new Intent(this, NotificationResponderService.class);
        intentShow.setAction(Notifications.ACTION_SHOW);
        PendingIntent piShow = PendingIntent.getService(this, 0, intentShow, PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);

        Intent intentHide = new Intent(this, NotificationResponderService.class);
        intentHide.setAction(Notifications.ACTION_HIDE);
        PendingIntent piHide = PendingIntent.getService(this, 0, intentHide, PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);

        Intent intentDismiss = new Intent(this, NotificationResponderService.class);
        intentDismiss.setAction(Notifications.ACTION_DISMISS);
//        PendingIntent piDismiss = PendingIntent.getService(this, 0, intentDismiss, PendingIntent.FLAG_CANCEL_CURRENT);

//        Intent intentSettings = new Intent(this, SettingsActivity.class);
//        PendingIntent piSettings = PendingIntent.getActivity(this,0, intentSettings, 0);

        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, getString(R.string.notification_channel_id));
        Notification notification = notificationBuilder.setOngoing(true)
                .setContentTitle("Control Dashboard")
//                .setContentText("You can show or hide floating button, or switch power off")
                .setStyle(new NotificationCompat.BigTextStyle().bigText("Show or hide floating button, or switch power off"))
                .setSmallIcon(R.raw.logo_transparent)
                .setPriority(NotificationManager.IMPORTANCE_MAX)
                .setCategory(Notification.CATEGORY_SERVICE)
                .setContentIntent(piNotification)
                // Add action shows only up to 3 items
                .addAction(R.drawable.ic_play, "Show", piShow)
                .addAction(R.drawable.ic_stop, "Hide", piHide)
//                .addAction(R.drawable.ic_close, "Power Off", piDismiss)
                .addAction(R.drawable.ic_settings, "Go to App", piNotification)
                .build();

        startForeground(Notifications.NOTIFICATION_ID, notification);
//        isForegroundRunning = true;

        BroadcastReceiver brStopService = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                stopForeground(true);
                stopSelf();
            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver(brStopService,
                new IntentFilter(Broadcast.STOP_FLOATING_BUTTON_SERVICE));
    }

    private void startScreenCapture() {
        if (intentConsentToken == null) {
            Toast.makeText(this, "You must grant permission to capture screen first", Toast.LENGTH_SHORT).show();
        } else if (foregroundWindow == null || foregroundWindow.viewFloatingTool == null) {
            Toast.makeText(this, "UI is not initialized", Toast.LENGTH_SHORT).show();
        } else {
            ScreenCapture screenCapture = new ScreenCapture(this, intentConsentToken);
            screenCapture.takeScreenshot();
        }
    }

//    @Override
//    protected void onHandleIntent(@Nullable Intent intent) {
//        final String action = intent.getAction();
//        if (action != null && !action.isEmpty()) {
//            if (action.equals(ACTION_SHOW_BUTTON)) {
//                showUI();
//            } else if (action.equals(ACTION_HIDE_BUTTON)) {
//                hideUI();
//            }
//        }
//    }

    private class ForegroundWindow {
        private static final String TAG = "ForegroundWindow";

        private Context context;
        private WindowManager.LayoutParams layoutParams;
        private WindowManager windowManager;
        private LayoutInflater layoutInflater;
        private View viewFloatingTool, viewDeleteArea;

        private View.OnTouchListener onTouchListener = new View.OnTouchListener() {
            private static final int MAX_CLICK_DURATION = 100;
            private long startClickTime;
            private int initialX;
            private int initialY;
            private float initialTouchX;
            private float initialTouchY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
//                Log.d("AD","Action E" + event);
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        //Log.d("AD","Action Down");
                        startClickTime = Calendar.getInstance().getTimeInMillis();
                        initialX = layoutParams.x;
                        initialY = layoutParams.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        viewDeleteArea.setVisibility(View.VISIBLE);
                        return true;
                    case MotionEvent.ACTION_UP:
                        viewDeleteArea.setVisibility(View.GONE);
                        //Log.d("AD","Action Up");
                        long clickDuration = Calendar.getInstance().getTimeInMillis() - startClickTime;
                        if (clickDuration < MAX_CLICK_DURATION) {
                            //Log.e("AD","Action Button Press");
                            hideUI();

                            startScreenCapture();
                        } else {
                            if (Utilities.isViewOverlapping(viewFloatingTool, viewDeleteArea)) {
//                                Toast.makeText(context, "DELETE", Toast.LENGTH_SHORT).show();
                                hideUI();
                            }
                        }
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        //Log.d("AD","Action Move");
                        layoutParams.x = initialX + (int) (event.getRawX() - initialTouchX);
                        layoutParams.y = initialY + (int) (event.getRawY() - initialTouchY);
//                        Log.e(TAG, "Changing from initialX " + initialX + " or initialTouchX " + initialTouchX +
//                                " to mParams.x " + mParams.x + " via event.getRawX() " + event.getRawX());
                        windowManager.updateViewLayout(viewFloatingTool, layoutParams);
//                        ivDelete.setVisibility(View.VISIBLE);
                        return true;
                }
                return false;
            }
        };

        private ForegroundWindow(Context context) { //, Runnable runnableOnButtonPress){
            this.context = context;
            windowManager = (WindowManager) context.getSystemService(WINDOW_SERVICE);
            layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            //this.runnableOnButtonPress = runnableOnButtonPress;
        }

        private void attachViewFloatingTool() {
            // set the layout parameters of the window
            layoutParams = new WindowManager.LayoutParams(
                    // Shrink the window to wrap the content rather
                    // than filling the screen
                    WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT,
                    // Display it on top of other application windows
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                    // Don't let it grab the input focus
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    // Make the underlying application window visible
                    // through any transparent parts
                    PixelFormat.TRANSLUCENT);
            layoutParams.gravity = Gravity.LEFT | Gravity.CENTER_VERTICAL;
//            windowManager = (WindowManager) context.getSystemService(WINDOW_SERVICE);
//            layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            viewFloatingTool = layoutInflater.inflate(R.layout.tools_tray, null);

            viewFloatingTool.setOnTouchListener(onTouchListener);
            windowManager.addView(viewFloatingTool, layoutParams);

            ivButton = viewFloatingTool.findViewById(R.id.ivCircle);
            setButtonColor(FloatingButtonService.this, null);
            setButtonSize(FloatingButtonService.this, null);
            setButtonOpacity(FloatingButtonService.this, null);
        }

        private void attachViewDeleteArea() {
            // set the layout parameters of the window
            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(
                    // Shrink the window to wrap the content rather
                    // than filling the screen
                    WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT,
                    // Display it on top of other application windows
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                    // Don't let it grab the input focus
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    // Make the underlying application window visible
                    // through any transparent parts
                    PixelFormat.TRANSLUCENT);
            layoutParams.gravity = Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM;
            layoutParams.y = 50;
//            windowManager = (WindowManager) context.getSystemService(WINDOW_SERVICE);
//            layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            viewDeleteArea = layoutInflater.inflate(R.layout.delete_tray, null);

//            viewDeleteArea.setOnTouchListener(onTouchListener);
            windowManager.addView(viewDeleteArea, layoutParams);

//            ivDelete = viewDeleteArea.findViewById(R.id.ivDelete);
//            setButtonColor(FloatingButtonService.this, null);
//            setButtonSize(FloatingButtonService.this, null);
//            setButtonOpacity(FloatingButtonService.this, null);
        }

        private void detachView() {
            if (windowManager != null && viewFloatingTool != null) {
                windowManager.removeView(viewFloatingTool);
                windowManager.removeView(viewDeleteArea);
            }
        }
    }

    public static void setButtonColor(Context context, @Nullable String strButtonColor) {
        int buttonColor = 0;
        if (strButtonColor != null && !strButtonColor.isEmpty()) {
            buttonColor = Utilities.getColorResourceId(context, strButtonColor);
        } else {
            buttonColor = Preferences.getFloatingButtonColor(context);
        }

        if (ivButton != null) {
            ivButton.setBackgroundTintList(ContextCompat.getColorStateList(context, buttonColor));
        }
    }

    public static void setButtonSize(Context context, @Nullable String newButtonSize) {
        int circleSize = 0;
        if (newButtonSize != null && !newButtonSize.isEmpty()) {
            circleSize = Utilities.getCircleButtonSize(context, newButtonSize);
        } else {
            String strCircleSize = Preferences.getFloatingButtonSize(context);
            circleSize = Utilities.getCircleButtonSize(context, strCircleSize);
        }

        if (ivButton != null) {
            ivButton.getLayoutParams().height = circleSize;
            ivButton.getLayoutParams().width = circleSize;
            ivButton.requestLayout();
        }
    }

    public static void setButtonOpacity(Context context, @Nullable String newButtonTransparency) {
        int intNewButtonTransparency = 0;
        float buttonAlpha = 0;
        if (newButtonTransparency != null && !newButtonTransparency.isEmpty() && Utilities.isInteger(newButtonTransparency)) {
            intNewButtonTransparency = Integer.valueOf(newButtonTransparency);
            buttonAlpha = Utilities.getButtonAlpha(context, intNewButtonTransparency);
        } else {
            intNewButtonTransparency = Preferences.getFloatingButtonOpacity(context);
            buttonAlpha = Utilities.getButtonAlpha(context, intNewButtonTransparency);
        }
        if (ivButton != null) {
            ivButton.setAlpha(buttonAlpha);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (foregroundWindow != null) {
            foregroundWindow.detachView();
        }
        isServiceRunning = false;
    }
}