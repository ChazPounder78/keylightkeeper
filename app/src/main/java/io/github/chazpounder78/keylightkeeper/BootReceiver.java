package io.github.chazpounder78.keylightkeeper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences prefs = context.getSharedPreferences(MainActivity.PREFS, Context.MODE_PRIVATE);
        if (prefs.getBoolean(MainActivity.KEY_ENABLED, false)) {
            context.startForegroundService(new Intent(context, KeyLightService.class));
        }
    }
}
