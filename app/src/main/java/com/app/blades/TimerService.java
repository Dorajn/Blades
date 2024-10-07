package com.app.blades;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.Nullable;

import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TimerService extends Service {

    public static final String TIMER_UPDATED = "timerUpdated";
    public static final String TIMER_VALUE = "timerValue";
    ScheduledExecutorService myschedule_executor;
    TextView timerView;
    int seconds = 0;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        seconds = 0;

        myschedule_executor = Executors.newScheduledThreadPool(1);
        myschedule_executor.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                seconds++;

                Handler mainHandler = new Handler(getMainLooper());
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (Car.timerView != null) {
                            Car.timerView.setTextColor(Color.parseColor("#bc422d"));
                            Car.timerView.setText(getTimerText());
                            Car.time = seconds;
                        }
                    }
                });

            }
        }, 1, 1, TimeUnit.SECONDS);

        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        myschedule_executor.shutdown();
    }

    private String getTimerText() {

        int rounded = (int)Math.round(seconds);

        int seconds = ((rounded % 86400) % 3600) % 60;
        int minutes = ((rounded % 86400) % 3600) / 60;
        int hours = (rounded % 86400) / 3600;

        return String.format("%02d", hours) + ":" + String.format("%02d", minutes) + ":" + String.format("%02d", seconds);

    }

    public void setView(TextView view){
        timerView = view;
    }
}
