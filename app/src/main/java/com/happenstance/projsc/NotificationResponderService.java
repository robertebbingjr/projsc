package com.happenstance.projsc;

import android.app.IntentService;
import android.content.Intent;

import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.happenstance.projsc.constants.Broadcast;
import com.happenstance.projsc.constants.Notifications;

public class NotificationResponderService extends IntentService {

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public NotificationResponderService(String name) {
        super(name);
    }

    public NotificationResponderService() {
        super("NotificationResponderService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        final String action = intent.getAction();
        if (action != null && !action.isEmpty()) {
            if (action.equals(Notifications.ACTION_SHOW)) {
                FloatingButtonService.showUI();

                Intent intentShowButton = new Intent(Broadcast.SHOW_BUTTON_TOGGLE);
                intentShowButton.putExtra(Broadcast.SHOW_BUTTON_TOGGLE_EXTRA, true);
                LocalBroadcastManager.getInstance(this).sendBroadcast(intentShowButton);
            } else if (action.equals(Notifications.ACTION_HIDE)) {
                FloatingButtonService.hideUI();

                Intent intentHideButton = new Intent(Broadcast.SHOW_BUTTON_TOGGLE);
                intentHideButton.putExtra(Broadcast.SHOW_BUTTON_TOGGLE_EXTRA, false);
                LocalBroadcastManager.getInstance(this).sendBroadcast(intentHideButton);
            } else if (action.equals(Notifications.ACTION_DISMISS)) {
                Intent intentPowerOff = new Intent(Broadcast.POWER_OFF);
                LocalBroadcastManager.getInstance(this).sendBroadcast(intentPowerOff);
            }
        }
    }

    private void hideButton() {
        FloatingButtonService.hideUI();

        Intent intentPowerOff = new Intent(Broadcast.SHOW_BUTTON_TOGGLE);
        intentPowerOff.putExtra(Broadcast.SHOW_BUTTON_TOGGLE_EXTRA, false);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intentPowerOff);
    }

}
