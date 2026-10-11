package io.github.chazpounder78.keylightkeeper;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;

/**
 * Holds the hardware key backlight at full brightness while the screen is on and
 * turns it off when the screen goes to sleep. The MediaTek keypad driver resets
 * the LED on screen transitions, so we re-assert it periodically.
 */
public class KeyLightService extends Service {

    static final String LED = "/sys/class/leds/button-backlight/brightness";
    private static final String CHANNEL_ID = "keylight";
    private static final int NOTIFICATION_ID = 1;
    private static final long TICK_MS = 1000L;

    private final RootShell shell = new RootShell();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private PowerManager powerManager;

    private final BroadcastReceiver screenReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            applyState();
        }
    };

    private final Runnable tick = new Runnable() {
        @Override
        public void run() {
            applyState();
            handler.postDelayed(this, TICK_MS);
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        createChannel();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        registerReceiver(screenReceiver, filter);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForeground(NOTIFICATION_ID, buildNotification());
        shell.start();
        handler.removeCallbacks(tick);
        handler.post(tick);
        return START_STICKY;
    }

    private void applyState() {
        boolean screenOn = powerManager != null && powerManager.isInteractive();
        int wanted = screenOn ? 255 : 0;
        // Re-assert every tick: the keypad driver resets the LED behind our back.
        shell.exec("echo " + wanted + " > " + LED);
    }

    @Override
    public void onDestroy() {
        handler.removeCallbacks(tick);
        try {
            unregisterReceiver(screenReceiver);
        } catch (IllegalArgumentException ignored) {
        }
        shell.exec("echo 0 > " + LED);
        shell.stop();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createChannel() {
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                getString(R.string.channel_name),
                NotificationManager.IMPORTANCE_LOW);
        channel.setDescription(getString(R.string.channel_desc));
        NotificationManager nm = getSystemService(NotificationManager.class);
        if (nm != null) nm.createNotificationChannel(channel);
    }

    private Notification buildNotification() {
        return new Notification.Builder(this, CHANNEL_ID)
                .setContentTitle(getString(R.string.notif_title))
                .setContentText(getString(R.string.notif_text))
                .setSmallIcon(R.drawable.ic_stat_keylight)
                .setOngoing(true)
                .build();
    }
}
