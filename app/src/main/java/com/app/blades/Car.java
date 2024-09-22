package com.app.blades;

import static android.content.ContentValues.TAG;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Source;

import java.util.Timer;
import java.util.TimerTask;

public class Car extends AppCompatActivity {

    TextView vehicleName, vehicleMileage, vehicleFuelLevel, cost, timeValue;
    FirebaseFirestore db;
    FirebaseAuth auth;
    String userID;
    Button changeCar, trackRide, endTracking, inactiveButton;
    ImageView fuelChange, mileage;
    EditText fuelLevelEditTextDialog, mileageEditTextDialog, mileageTrackEditText, avgConsumptionEditText;

    Dialog dialogFuel, dialogMileage, dialogMore, dialogTrack;
    Button acceptFuelChange, acceptMileageChange, acceptTrack, dismiss, goBack;
    String vehicleUID;

    //vehicle stored values
    String mileageStored = "0";
    String fuelLevelStored = "0";

    //alert variables
    Button yes, no;
    Dialog alertYesNo;

    //timer
    TextView timerView;
    Timer timer;
    TimerTask timerTask;
    double time = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car);

        if(LocalStorage.newAddedCar){
            vehicleUID = LocalStorage.currentNewVehicleUID;
        }
        else{
            vehicleUID = LocalStorage.currentVehicleUID;
        }

        //initializing layout variables
        vehicleName = findViewById(R.id.carName);
        vehicleMileage = findViewById(R.id.mileage);
        vehicleFuelLevel = findViewById(R.id.petrolLevel);
        trackRide = findViewById(R.id.trackRideButton);
        endTracking = findViewById(R.id.endOfTracking);
        fuelChange = findViewById(R.id.fuelChangeButton);
        mileage = findViewById(R.id.mileageChangeButton);
        changeCar = findViewById(R.id.changeCarButton);
        timerView = findViewById(R.id.timer);

        timer = new Timer();

        //dialogs
        dialogMileage = new Dialog(Car.this);
        dialogMileage.setContentView(R.layout.mileage_change_dialog);
        dialogMileage.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialogMileage.getWindow().setBackgroundDrawable(getDrawable(R.drawable.dialogs_backgroud));

        dialogFuel = new Dialog(Car.this);
        dialogFuel.setContentView(R.layout.fuel_refill_dialog);
        dialogFuel.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialogFuel.getWindow().setBackgroundDrawable(getDrawable(R.drawable.dialogs_backgroud));

        dialogTrack = new Dialog(Car.this);
        dialogTrack.setContentView(R.layout.finish_tracking_dialog);
        dialogTrack.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialogTrack.getWindow().setBackgroundDrawable(getDrawable(R.drawable.dialogs_backgroud));
        dialogTrack.setCancelable(false);

        alertYesNo = new Dialog(Car.this);
        alertYesNo.setContentView(R.layout.alert_dialog);
        alertYesNo.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        alertYesNo.getWindow().setBackgroundDrawable(getDrawable(R.drawable.dialogs_backgroud));

        Window window = alertYesNo.getWindow();
        if (window != null) {
            window.setDimAmount(0.8f);
        }

        Window window2 = dialogTrack.getWindow();
        if (window2 != null) {
            window2.setDimAmount(0.8f);
        }

        fuelLevelEditTextDialog = dialogFuel.findViewById(R.id.fuelLevelEditTextDialog);
        acceptFuelChange = dialogFuel.findViewById(R.id.fuelAcceptButton);

        mileageEditTextDialog = dialogMileage.findViewById(R.id.mileageEditTextDialog);
        acceptMileageChange = dialogMileage.findViewById(R.id.mileageChangeButtonDialog);

        avgConsumptionEditText = dialogTrack.findViewById(R.id.avgConsumptionEditTextDialog);
        mileageTrackEditText = dialogTrack.findViewById(R.id.mileageOnFinishEditTextDialog);
        acceptTrack = dialogTrack.findViewById(R.id.trackAcceptButton);
        dismiss = dialogTrack.findViewById(R.id.dismiss);
        cost = dialogTrack.findViewById(R.id.cost);
        goBack = dialogTrack.findViewById(R.id.trackGoBackButton);
        inactiveButton = dialogTrack.findViewById(R.id.inactiveButton);
        timeValue = dialogTrack.findViewById(R.id.time);

        yes = alertYesNo.findViewById(R.id.yes);
        no = alertYesNo.findViewById(R.id.no);

        //firebase variables
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        userID = auth.getCurrentUser().getUid();

        dismiss.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertYesNo.show();
            }
        });

        yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertYesNo.dismiss();
                dialogTrack.dismiss();
            }
        });

        no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertYesNo.dismiss();
            }
        });

        goBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialogTrack.dismiss();
                dismiss.setVisibility(View.VISIBLE);
                inactiveButton.setVisibility(View.INVISIBLE);
                acceptTrack.setVisibility(View.VISIBLE);
                goBack.setVisibility(View.INVISIBLE);
                resetTrackDialog();
            }
        });

        acceptTrack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String currentMileageAfter = mileageTrackEditText.getText().toString();
                String avgFuelCon = avgConsumptionEditText.getText().toString();

                long currMileageAfterTrack = Long.parseLong(currentMileageAfter);
                double averageFuelCon = Double.parseDouble(avgFuelCon);
                long startMileage = Long.parseLong(mileageStored);

                if(currMileageAfterTrack < startMileage){
                    Toast.makeText(Car.this, "Mileage can not be lower than before ride", Toast.LENGTH_LONG).show();
                    return;
                }
                else if(averageFuelCon <= 0){
                    Toast.makeText(Car.this, "Your car is not that ecomomical...", Toast.LENGTH_LONG).show();
                    return;
                }

                //logics
                long kmDriven = currMileageAfterTrack - startMileage;
                double fuelUsed = (averageFuelCon * (double)kmDriven) / 100;
                double fuelLeft = Double.parseDouble(fuelLevelStored) - fuelUsed;

                cost.setText("Cost: " + String.format("%.2f", fuelUsed * LocalStorage.fuelPrice));

                userID = auth.getCurrentUser().getUid();
                DocumentReference userData = db.collection("vehicles")
                        .document(userID)
                        .collection("vehicles")
                        .document(vehicleUID);

                userData.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        DocumentSnapshot document = task.getResult();
                        if(document.exists()){
                            userData.update("mileage", currentMileageAfter);
                            userData.update("fuelLevel", String.valueOf(fuelLeft));
                        }

                        if(fuelLeft <= (double)LocalStorage.lowFuelWarning){
                            fuelChange.setImageResource(R.drawable.baseline_local_gas_station_24_red);
                        }
                        else{
                            fuelChange.setImageResource(R.drawable.baseline_local_gas_station_24_white);
                        }

                        Toast.makeText(Car.this, "Track saved!", Toast.LENGTH_SHORT).show();

                        vehicleMileage.setText(currentMileageAfter);
                        String fLevel2digits = String.format("%.2f", fuelLeft);
                        vehicleFuelLevel.setText(String.valueOf(fLevel2digits));
                        acceptTrack.setVisibility(View.INVISIBLE);
                        goBack.setVisibility(View.VISIBLE);

                        inactiveButton.setVisibility(View.VISIBLE);
                        dismiss.setVisibility(View.INVISIBLE);

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(view.getContext(), "Error occured", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        acceptMileageChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String newMileage = mileageEditTextDialog.getText().toString();
                resetMileageChangeDialog();

                userID = auth.getCurrentUser().getUid();
                DocumentReference userData = db.collection("vehicles")
                        .document(userID)
                        .collection("vehicles")
                        .document(vehicleUID);

                userData.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        DocumentSnapshot document = task.getResult();

                        if(Long.parseLong(newMileage) < 0){
                            Toast.makeText(Car.this, "Mileage can not be negative", Toast.LENGTH_LONG).show();
                            return;
                        }
                        else if(Long.parseLong(newMileage) < Long.parseLong(mileageStored)){
                            Toast.makeText(Car.this, "Warning, you entered lower mileage than before!", Toast.LENGTH_LONG).show();
                        }
                        else{
                            Toast.makeText(view.getContext(), "Mileage changed", Toast.LENGTH_SHORT).show();
                        }

                        if(document.exists()){
                            userData.update("mileage", newMileage);
                        }

                        vehicleMileage.setText(newMileage);
                        mileageStored = newMileage;
                        dialogMileage.dismiss();

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(view.getContext(), "Error occured", Toast.LENGTH_SHORT).show();
                        dialogMileage.dismiss();
                    }
                });

            }
        });

        //fuel level change
        acceptFuelChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String newFuelLevel = fuelLevelEditTextDialog.getText().toString();
                resetFuelRefillDialog();

                if(Long.parseLong(newFuelLevel) < 0){
                    Toast.makeText(Car.this, "Fuel level can not be negative value!", Toast.LENGTH_LONG).show();
                    return;
                }

                userID = auth.getCurrentUser().getUid();
                DocumentReference userData = db.collection("vehicles")
                        .document(userID)
                        .collection("vehicles")
                        .document(vehicleUID);

                userData.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        DocumentSnapshot document = task.getResult();

                        Toast.makeText(view.getContext(), "Fuel level changed", Toast.LENGTH_SHORT).show();
                        vehicleFuelLevel.setText(newFuelLevel);

                        if(Double.parseDouble(newFuelLevel) <= LocalStorage.lowFuelWarning){
                            fuelChange.setImageResource(R.drawable.baseline_local_gas_station_24_red);
                        }
                        else{
                            fuelChange.setImageResource(R.drawable.baseline_local_gas_station_24_white);
                        }
                        if(document.exists()){
                            userData.update("fuelLevel", newFuelLevel);
                        }

                        fuelLevelStored = newFuelLevel;
                        dialogFuel.dismiss();

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(view.getContext(), "Error occured", Toast.LENGTH_SHORT).show();
                        dialogFuel.dismiss();
                    }
                });

            }
        });

        fuelChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialogFuel.show();
            }
        });

        mileage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialogMileage.show();
            }
        });

        changeCar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), CarList.class);
                startActivity(intent);
                finish();
            }
        });

        trackRide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                trackRide.setVisibility(View.INVISIBLE);
                endTracking.setVisibility(View.VISIBLE);
                startTimer();
            }
        });

        endTracking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                endTracking.setVisibility(View.INVISIBLE);
                trackRide.setVisibility(View.VISIBLE);
                timeValue.setText("Time: " + getTimerText());
                dialogTrack.show();
                endTimer();
            }
        });

    }


    @Override
    protected void onStart() {
        super.onStart();

        resetButtons();
        resetTrackDialog();
        resetFuelRefillDialog();
        resetMileageChangeDialog();

        if(LocalStorage.newAddedCar){
            vehicleUID = LocalStorage.currentNewVehicleUID;
        }
        else{
            vehicleUID = LocalStorage.currentVehicleUID;
        }


        db.collection("vehicles")
                .document(userID)
                .collection("vehicles")
                .document(vehicleUID)
                .get(Source.SERVER)
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot document) {
                        if(document.exists()){
                            String name = (String) document.getData().get("vehicleName");
                            mileageStored = (String) document.getData().get("mileage");
                            fuelLevelStored = (String) document.getData().get("fuelLevel");

                            if(Double.parseDouble(fuelLevelStored) <= LocalStorage.lowFuelWarning){
                                fuelChange.setImageResource(R.drawable.baseline_local_gas_station_24_red);
                            }
                            else{
                                fuelChange.setImageResource(R.drawable.baseline_local_gas_station_24_white);
                            }

                            vehicleName.setText(name);
                            vehicleMileage.setText(mileageStored);

                            double fLevel = Double.parseDouble(fuelLevelStored);
                            String fLevel2digits = String.format("%.2f", fLevel);
                            vehicleFuelLevel.setText(fLevel2digits);
                        }
                        else{
                            Log.d("FireStore", "Vehicle not found.");
                        }
                    }
                });
    }

    public void resetButtons(){
        dismiss.setVisibility(View.VISIBLE);
        inactiveButton.setVisibility(View.INVISIBLE);
        acceptTrack.setVisibility(View.VISIBLE);
        goBack.setVisibility(View.INVISIBLE);
    }

    public void resetTrackDialog(){
        cost.setText("Cost: unknown, enter data");
        avgConsumptionEditText.setText("");
        mileageTrackEditText.setText("");
    }

    public void resetFuelRefillDialog(){
        fuelLevelEditTextDialog.setText("");
    }

    public void resetMileageChangeDialog(){
        mileageEditTextDialog.setText("");
    }

    private void startTimer(){

        timerTask = new TimerTask() {
            @Override
            public void run() {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        time++;
                        timerView.setTextColor(Color.parseColor("#bc422d"));
                        timerView.setText("  " + getTimerText());
                    }
                });
            }
        };
        timer.schedule(timerTask, 0, 1000);
    }

    private String getTimerText() {

        int rounded = (int)Math.round(time);

        int seconds = ((rounded % 86400) % 3600) % 60;
        int minutes = ((rounded % 86400) % 3600) / 60;
        int hours = (rounded % 86400) / 3600;

        return String.format("%02d", hours) + ":" + String.format("%02d", minutes) + ":" + String.format("%02d", seconds);

    }

    private void endTimer(){
        timerView.setTextColor(Color.parseColor("#bbbbbb"));
        timerTask.cancel();
        timerView.setText("  00:00:00");
        time = 0;
    }

}