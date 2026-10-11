package io.github.chazpounder78.keylightkeeper;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Switch;
import android.widget.TextView;

public class MainActivity extends Activity {

    static final String PREFS = "keylight_prefs";
    static final String KEY_ENABLED = "enabled";

    private Switch toggle;
    private TextView status;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toggle = findViewById(R.id.toggle);
        status = findViewById(R.id.status);

        SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        boolean enabled = prefs.getBoolean(KEY_ENABLED, false);
        toggle.setChecked(enabled);
        updateStatus(enabled);

        toggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean(KEY_ENABLED, isChecked).apply();
            Intent intent = new Intent(this, KeyLightService.class);
            if (isChecked) {
                requestNotificationPermission();
                startForegroundService(intent);
            } else {
                stopService(intent);
            }
            updateStatus(isChecked);
        });
    }

    private void updateStatus(boolean enabled) {
        status.setText(enabled ? R.string.status_on : R.string.status_off);
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1);
        }
    }
}
