package services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Citrus on 18.12.2017.
 */

public class BackgroundLocationService extends Service {

    private Timer backgroundTimer = new Timer();

    private final int DELAY_MS = 0;
    private final int PERIOD_MS = 5000;


    private void start() {

        this.backgroundTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    //TODO: SQL get buses at around current time
                    Log.e("INFO", "BackgroundService run output");
                } catch (Exception e) {
                    Log.e("ERROR", e.getMessage());
                }
            }
        }, DELAY_MS, PERIOD_MS);
    }

    public void onCreate() {
        super.onCreate();
        start();
        Log.e("INFO", "Background service started");
    }

    public void onDestroy() {
        Log.e("INFO", "Background service stopped");
        super.onDestroy();
        stopTimer();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void stopTimer() {
        this.backgroundTimer.cancel();
    }
}
