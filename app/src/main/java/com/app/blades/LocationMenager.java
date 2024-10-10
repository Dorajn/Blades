package com.app.blades;

import static androidx.core.app.ActivityCompat.requestPermissions;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.lang.reflect.Constructor;

public class LocationMenager extends Service {

    public static final int TIME_INTERVAL_SENDING_GPS_SIGNAL = 5000;
    public static final int TIME_INTERVAL_GETTING_FAST_SIGNAL = 5000;
    public static final int ERROR_RANGE = 5;
    private static final String CHANNEL_ID = "LocationTrackingChannel";

    Context context;

    //Google location variables
    FusedLocationProviderClient fusedLocationProviderClient;
    LocationRequest locationRequest;
    LocationCallback locationCallBack;

    //my variables
    long mileage;
    public Location location;
    public Location previousLocation;
    TextView x, y, meters, estimatedLength, averageSpeed;
    EditText editText;
    double metersDriven = 0.0;

    public LocationMenager(Context context, TextView x, TextView y, TextView meters, EditText editText, TextView estimatedLength, TextView averageSpeed){

        this.averageSpeed = averageSpeed;
        this.estimatedLength = estimatedLength;
        this.editText = editText;
        this.meters = meters;
        this.x = x;
        this.y = y;
        this.context = context;
        locationRequest = new LocationRequest();
        locationRequest.setInterval(TIME_INTERVAL_SENDING_GPS_SIGNAL);
        locationRequest.setFastestInterval(TIME_INTERVAL_GETTING_FAST_SIGNAL);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context);

        //this event is being called every time the interval is reached
        locationCallBack = new LocationCallback(){

            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                location = locationResult.getLastLocation();

                calculateMetersDriven();
                previousLocation = location;
                updateUI();
            }
        };
    }

    public void startLocationUpdates() {
        metersDriven = 0;
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallBack, null);
    }

    public void endLocationUpdates() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallBack);
        x.setText("Not tracking");
        y.setText("Not tracking");
        editText.setText(convertMetersToMileage());
        estimatedLength.setText("Estimated route length: " + getEstimatedRouteLength());

    }

    private void updateUI(){
        x.setText(String.valueOf(location.getLatitude()));
        y.setText(String.valueOf(location.getLongitude()));
        meters.setText("Meters driven: " + String.format("%.2f", metersDriven));
        averageSpeed.setText("Average speed: " + getAverageSpeed());
    }

    public void setMileage(long mileage){this.mileage = mileage;}

    private void calculateMetersDriven(){
        if(metersDriven != 0){
            double mDriven = (double)previousLocation.distanceTo(location);

            if(mDriven >= ERROR_RANGE){
                metersDriven += (double)previousLocation.distanceTo(location);
                previousLocation = location;
            }
        }else{
            metersDriven = 0.001;
            previousLocation = location;
        }
    }

    private String convertMetersToMileage(){
        long kiloMetersDriven = (long) Math.ceil(metersDriven / 1000);
        return String.valueOf(kiloMetersDriven + mileage);
    }

    private String getEstimatedRouteLength(){
        return String.valueOf((long) Math.ceil(metersDriven / 1000));
    }

    private String getAverageSpeed(){
        return String.format("%.2f" ,(metersDriven / 1000) / (Car.time / 3600));
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        fusedLocationProviderClient.removeLocationUpdates(locationCallBack);
    }

    @SuppressLint("ForegroundServiceType")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        createNotificationChannel();

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Tracking Location")
                .setContentText("Your location is being tracked.")
                .setSmallIcon(R.drawable.baseline_location_on_24)
                .build();

        startForeground(1, notification);

        return START_STICKY;
    }

    private void createNotificationChannel() {
        NotificationChannel channel = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Location Tracking Channel",
                    NotificationManager.IMPORTANCE_LOW
            );
        }
        NotificationManager manager = getSystemService(NotificationManager.class);
        if (manager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                manager.createNotificationChannel(channel);
            }
        }
    }
}
