package com.app.blades;

import static androidx.core.app.ActivityCompat.requestPermissions;
import android.os.HandlerThread;
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
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.lang.reflect.Constructor;

public class LocationMenager {

    public static final int TIME_INTERVAL_SENDING_GPS_SIGNAL = 5000;
    public static final int TIME_INTERVAL_GETTING_FAST_SIGNAL = 5000;

    Context context;

    //Google location variables
    FusedLocationProviderClient fusedLocationProviderClient;
    LocationRequest locationRequest;
    LocationCallback locationCallBack;

    //my variables
    long mileage;
    public Location location;
    public Location previousLocation;
    TextView x, y, meters, estimatedLength;
    EditText editText;
    double metersDriven = 0.0;

    public LocationMenager(Context context, TextView x, TextView y, TextView meters, EditText editText, TextView estimatedLenght){

        this.estimatedLength = estimatedLenght;
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
    }

    public void setMileage(long mileage){this.mileage = mileage;}

    private void calculateMetersDriven(){
        if(metersDriven != 0){
            double mDriven = (double)previousLocation.distanceTo(location);

            if(mDriven >= 5){
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


}
