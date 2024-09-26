package com.app.blades;

import static android.content.ContentValues.TAG;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
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
import androidx.annotation.Nullable;
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
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Source;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class Car extends AppCompatActivity {

    TextView vehicleName, vehicleMileage, vehicleFuelLevel, cost, timeValue, avgSpeed, stat;
    FirebaseFirestore db;
    FirebaseAuth auth;
    String userID;
    Button changeCar, changeCarInactive, trackRide, endTracking, inactiveButton;
    ImageView fuelChange, mileage, member, settings;
    EditText fuelLevelEditTextDialog, mileageEditTextDialog, mileageTrackEditText, avgConsumptionEditText, RefuelLevelEditTextDialog, addMemberEditText, removeMemberEditText;

    Dialog dialogFuel, dialogMileage, dialogSettings, dialogTrack, dialogRefuelOption, dialogRefuel, dialogAddMember, dialogRemoveMember, alertDeletion;
    Button acceptFuelChange, acceptMileageChange, acceptTrack, dismiss, goBack, optionRefuel, optionEnterFuel, acceptFuelChangeRefuel, addMemberButton;
    Button deleteVehicle, removeMember, acceptRemoval, yesDeletion, noDeletion;
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
    double timeSaved = 0;


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
        changeCarInactive = findViewById(R.id.changeCarButtonInactive);
        member = findViewById(R.id.addMember);
        stat = findViewById(R.id.statistics);
        settings = findViewById(R.id.carSettings);


        timer = new Timer();

        //dialogs
        dialogMileage = new Dialog(Car.this);
        dialogMileage.setContentView(R.layout.mileage_change_dialog);
        dialogMileage.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialogMileage.getWindow().setBackgroundDrawable(getDrawable(R.drawable.dialogs_backgroud));

        dialogRefuelOption = new Dialog(Car.this);
        dialogRefuelOption.setContentView(R.layout.fuel_refil_options_dialog);
        dialogRefuelOption.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialogRefuelOption.getWindow().setBackgroundDrawable(getDrawable(R.drawable.dialogs_backgroud));

        dialogRefuel = new Dialog(Car.this);
        dialogRefuel.setContentView(R.layout.fuel_refill_add_dialog);
        dialogRefuel.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialogRefuel.getWindow().setBackgroundDrawable(getDrawable(R.drawable.dialogs_backgroud));

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

        dialogAddMember = new Dialog(Car.this);
        dialogAddMember.setContentView(R.layout.add_member_dialog);
        dialogAddMember.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialogAddMember.getWindow().setBackgroundDrawable(getDrawable(R.drawable.dialogs_backgroud));

        dialogSettings = new Dialog(Car.this);
        dialogSettings.setContentView(R.layout.car_settings_dialog);
        dialogSettings.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialogSettings.getWindow().setBackgroundDrawable(getDrawable(R.drawable.dialogs_backgroud));

        dialogRemoveMember = new Dialog(Car.this);
        dialogRemoveMember.setContentView(R.layout.remove_member_dialog);
        dialogRemoveMember.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialogRemoveMember.getWindow().setBackgroundDrawable(getDrawable(R.drawable.dialogs_backgroud));

        alertDeletion = new Dialog(Car.this);
        alertDeletion.setContentView(R.layout.alert_dialog_deletion);
        alertDeletion.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        alertDeletion.getWindow().setBackgroundDrawable(getDrawable(R.drawable.dialogs_backgroud));


        Window window = alertYesNo.getWindow();
        if (window != null) {
            window.setDimAmount(0.8f);
        }

        Window window6 = alertDeletion.getWindow();
        if (window6 != null) {
            window6.setDimAmount(0.8f);
        }

        Window window5 = dialogRemoveMember.getWindow();
        if (window5 != null) {
            window5.setDimAmount(0.8f);
        }

        Window window4 = dialogSettings.getWindow();
        if (window4 != null) {
            window4.setDimAmount(0.8f);
        }

        Window window2 = dialogTrack.getWindow();
        if (window2 != null) {
            window2.setDimAmount(0.8f);
        }

        Window window3 = dialogAddMember.getWindow();
        if (window3 != null) {
            window3.setDimAmount(0.8f);
        }

        fuelLevelEditTextDialog = dialogFuel.findViewById(R.id.fuelLevelEditTextDialog);
        acceptFuelChange = dialogFuel.findViewById(R.id.fuelAcceptButton);

        RefuelLevelEditTextDialog = dialogRefuel.findViewById(R.id.fuelLevelEditTextDialogRefuel);
        acceptFuelChangeRefuel = dialogRefuel.findViewById(R.id.fuelAcceptButtonRefuel);

        optionEnterFuel = dialogRefuelOption.findViewById(R.id.changeValueFuelButton);
        optionRefuel = dialogRefuelOption.findViewById(R.id.addFuelButton);


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
        avgSpeed = dialogTrack.findViewById(R.id.avgSpeed);

        addMemberEditText = dialogAddMember.findViewById(R.id.addMemberEditTextDialog);
        addMemberButton = dialogAddMember.findViewById(R.id.addMemberAcceptButton);

        deleteVehicle = dialogSettings.findViewById(R.id.deleteVehicle);
        removeMember = dialogSettings.findViewById(R.id.removeMember);

        acceptRemoval = dialogRemoveMember.findViewById(R.id.acceptRemoval);
        removeMemberEditText = dialogRemoveMember.findViewById(R.id.removeMemberEditText);

        yes = alertYesNo.findViewById(R.id.yes);
        no = alertYesNo.findViewById(R.id.no);

        yesDeletion = alertDeletion.findViewById(R.id.yesDeletion);
        noDeletion = alertDeletion.findViewById(R.id.noDeletion);

        //firebase variables
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        userID = auth.getCurrentUser().getUid();

        yesDeletion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteThisVehicle(view);
                alertDeletion.dismiss();
            }
        });

        noDeletion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialogSettings.show();
                alertDeletion.dismiss();
            }
        });

        removeMember.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                db.collection("userVehicles")
                        .whereEqualTo("userID", userID)
                        .whereEqualTo("vehicleID", vehicleUID)
                        .get(Source.SERVER)
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if(task.isSuccessful()){
                                    QuerySnapshot querySnapshot = task.getResult();
                                    if (querySnapshot != null && !querySnapshot.isEmpty()) {
                                        DocumentSnapshot document = querySnapshot.getDocuments().get(0);
                                        String type = document.getString("relationType");

                                        db.collection("vehicles").document(vehicleUID)
                                                .get(Source.SERVER)
                                                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                        if (task.isSuccessful()) {
                                                            DocumentSnapshot document = task.getResult();
                                                            if (document.exists()) {
                                                                Long memberCount = document.getLong("memberCount");

                                                                if(memberCount == 1){
                                                                    Toast.makeText(Car.this, "In this car there are no members besides you", Toast.LENGTH_LONG).show();
                                                                }
                                                                else{
                                                                    if(type.equals("owner")){
                                                                        dialogSettings.dismiss();
                                                                        dialogRemoveMember.show();
                                                                    }
                                                                    else{
                                                                        Toast.makeText(Car.this, "You need to be vehicle owner to remove members", Toast.LENGTH_LONG).show();
                                                                    }
                                                                }

                                                            }
                                                        }
                                                    }
                                                });
                                    }

                                }
                            }
                        });
            }
        });

        acceptRemoval.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String nickname = removeMemberEditText.getText().toString();
                if(nickname == LocalStorage.userNick){
                    Toast.makeText(Car.this, "You can't remove yourself", Toast.LENGTH_SHORT).show();
                    return;
                }
                deleteMember(nickname, view);
            }
        });

        deleteVehicle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDeletion.show();
            }
        });

        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialogSettings.show();
            }
        });

        stat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), Statistics.class);
                intent.putExtra("vehicleID", vehicleUID);
                startActivity(intent);
                finish();
            }
        });

        member.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                db.collection("userVehicles")
                        .whereEqualTo("userID", userID)
                        .whereEqualTo("vehicleID", vehicleUID)
                        .get(Source.SERVER)
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if(task.isSuccessful()){
                                    QuerySnapshot querySnapshot = task.getResult();
                                    if (querySnapshot != null && !querySnapshot.isEmpty()) {
                                        DocumentSnapshot document = querySnapshot.getDocuments().get(0);
                                        String type = document.getString("relationType");

                                        if(type.equals("owner")){
                                            dialogAddMember.show();
                                        }
                                        else{
                                            Toast.makeText(Car.this, "You need to be vehicle owner to add members", Toast.LENGTH_LONG).show();
                                        }

                                    }

                                }
                            }
                        });
            }
        });

        addMemberButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String nickname = addMemberEditText.getText().toString();

                db.collection("users")
                        .whereEqualTo("nick", nickname)
                        .get(Source.SERVER)
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    QuerySnapshot querySnapshot = task.getResult();
                                    if (querySnapshot != null && !querySnapshot.isEmpty()) {
                                        DocumentSnapshot document = querySnapshot.getDocuments().get(0);
                                        String newMemberID = document.getId();

                                        //changing vehicle count in member record
                                        DocumentReference userData = db.collection("users").document(newMemberID);
                                        userData.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                DocumentSnapshot document = task.getResult();
                                                if(document.exists()){

                                                    Long vehicleCount = document.getLong("vehicleCount");

                                                    if(vehicleCount == 5){
                                                        Toast.makeText(Car.this, nickname + " has reached vehicle limit!", Toast.LENGTH_LONG).show();
                                                    }
                                                    else{
                                                        //adding record to relation table
                                                        Map<String, Object> userVehicle = new HashMap<>();
                                                        userVehicle.put("userID", newMemberID);
                                                        userVehicle.put("vehicleID", vehicleUID);
                                                        userVehicle.put("relationType", "member");
                                                        userVehicle.put("nickname", nickname);
                                                        userVehicle.put("usedFuel", 0);
                                                        userVehicle.put("deliveredFuel", 0);
                                                        db.collection("userVehicles").add(userVehicle);

                                                        //changing vehicle count
                                                        userData.update("vehicleCount", (vehicleCount + 1))
                                                                .addOnSuccessListener(aVoid -> {
                                                                    Log.d(TAG, "Liczba pojazdów zaktualizowana pomyślnie.");
                                                                })
                                                                .addOnFailureListener(e -> {
                                                                    Log.e(TAG, "Błąd przy aktualizacji liczby pojazdów: ", e);
                                                                });


                                                        DocumentReference vehicleData = db.collection("vehicles").document(vehicleUID);
                                                        vehicleData.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<DocumentSnapshot> task2) {
                                                                DocumentSnapshot document2 = task2.getResult();
                                                                if(document2.exists()){

                                                                    Long memberCount = document2.getLong("memberCount");
                                                                    Log.d("Pizda", String.valueOf(memberCount));

                                                                    vehicleData.update("memberCount", (memberCount + 1))
                                                                            .addOnSuccessListener(aVoid -> {
                                                                                Log.d(TAG, "Liczba pojazdów zaktualizowana pomyślnie.");
                                                                            })
                                                                            .addOnFailureListener(e -> {
                                                                                Log.e(TAG, "Błąd przy aktualizacji liczby pojazdów: ", e);
                                                                            });

                                                                }

                                                            }
                                                        });


                                                        Toast.makeText(Car.this, nickname + " added as member", Toast.LENGTH_SHORT).show();
                                                        dialogAddMember.dismiss();
                                                    }


                                                }

                                            }
                                        });
                                    } else {
                                        Toast.makeText(Car.this, "No user foud with this nickname", Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    Toast.makeText(Car.this, "Oops, something went wrong...", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });

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
                endTimer();
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

                if(!checkTrackConstraints(currentMileageAfter, avgFuelCon))
                    return;

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

                addFuelUsageRecordToDataBase(String.valueOf(fuelLeft));

                cost.setText("Cost: " + String.format("%.2f", fuelUsed * LocalStorage.fuelPrice));
                cost.setTextColor(Color.parseColor("#2DBC95"));

                double averageSpeed = (double)kmDriven / (timeSaved / 3600);
                avgSpeed.setText("Average speed: " + String.format("%.2f", averageSpeed) + " km/h");;

                userID = auth.getCurrentUser().getUid();
                DocumentReference userData = db.collection("vehicles")
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

                        vehicleMileage.setText("Mileage: " + currentMileageAfter);
                        mileageStored = currentMileageAfter;
                        fuelLevelStored = String.valueOf(fuelLeft);
                        String fLevel2digits = String.format("%.2f", fuelLeft);
                        vehicleFuelLevel.setText("Fuel: " + String.valueOf(fLevel2digits));
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

                if(!checkMileageConstraints(newMileage))
                    return;

                userID = auth.getCurrentUser().getUid();
                DocumentReference userData = db.collection("vehicles")
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

                        vehicleMileage.setText("Mileage: " + newMileage);
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

                if(!checkFuelConstraints(newFuelLevel))
                    return;

                if(Double.parseDouble(newFuelLevel) < 0){
                    Toast.makeText(Car.this, "Fuel level can not be negative value!", Toast.LENGTH_LONG).show();
                    return;
                }

                addFuelUsageRecordToDataBase(newFuelLevel);
                changeFuelLevelInDataBase(newFuelLevel, view);

            }
        });

        acceptFuelChangeRefuel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String refilledFuel = RefuelLevelEditTextDialog.getText().toString();
                resetRefuelRefillDialog();

                if(!checkFuelConstraints(refilledFuel))
                    return;

                if(Double.parseDouble(refilledFuel) <= 0){
                    Toast.makeText(Car.this, "Refilled amount must be greater than 0", Toast.LENGTH_LONG).show();
                    return;
                }

                double acctualFuelLevel = Double.parseDouble(fuelLevelStored);
                double refilledNumber = Double.parseDouble(refilledFuel);
                String newLevel = String.valueOf(acctualFuelLevel + refilledNumber);

                addFuelUsageRecordToDataBase(newLevel);
                changeFuelLevelInDataBase(newLevel, view);

            }
        });

        fuelChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialogRefuelOption.show();
            }
        });

        optionRefuel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialogRefuelOption.dismiss();
                dialogFuel.show();
            }
        });

        optionEnterFuel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialogRefuelOption.dismiss();
                dialogRefuel.show();
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
                changeCar.setVisibility(View.INVISIBLE);
                changeCarInactive.setVisibility(View.VISIBLE);
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

                changeCarInactive.setVisibility(View.INVISIBLE);
                changeCar.setVisibility(View.VISIBLE);

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
        resetRefuelRefillDialog();
        resetMileageChangeDialog();

        if(LocalStorage.newAddedCar){
            vehicleUID = LocalStorage.currentNewVehicleUID;
        }
        else{
            vehicleUID = LocalStorage.currentVehicleUID;
        }


        db.collection("vehicles")
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
                            vehicleMileage.setText("Mileage: " + mileageStored);

                            double fLevel = Double.parseDouble(fuelLevelStored);
                            String fLevel2digits = String.format("%.2f", fLevel);
                            vehicleFuelLevel.setText("Fuel: " + fLevel2digits);
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
        cost.setTextColor(Color.parseColor("#BBBBBB"));
        avgSpeed.setText("Average speed: unknown");
        avgConsumptionEditText.setText("");
        mileageTrackEditText.setText("");
    }

    public void resetFuelRefillDialog(){
        fuelLevelEditTextDialog.setText("");
    }

    public void resetRefuelRefillDialog(){
        RefuelLevelEditTextDialog.setText("");
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
                        timerView.setText(getTimerText());
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
        timerView.setText("00:00:00");
        timeSaved = time;
        time = 0;
    }

    private void changeFuelLevelInDataBase(String newFuelLevel, View view){

        userID = auth.getCurrentUser().getUid();
        DocumentReference userData = db.collection("vehicles")
                .document(vehicleUID);

        userData.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                DocumentSnapshot document = task.getResult();

                double temp = Double.parseDouble(newFuelLevel);

                Toast.makeText(view.getContext(), "Fuel level changed", Toast.LENGTH_SHORT).show();
                vehicleFuelLevel.setText("Fuel: " + String.format("%.2f", temp));

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
                dialogRefuel.dismiss();

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(view.getContext(), "Error occured", Toast.LENGTH_SHORT).show();
                dialogFuel.dismiss();
            }
        });


    }

    private void addFuelUsageRecordToDataBase(String newFuelLevel){

        double newFL = Double.parseDouble(newFuelLevel);
        double fuelS = Double.parseDouble(fuelLevelStored);
        double volumen = Math.abs(newFL - fuelS);

        boolean delivered;
        if(newFL > fuelS){
            delivered = true;
        }
        else if(newFL < fuelS){
            delivered = false;
        }
        else {
            return;
        }


        db.collection("userVehicles")
                .whereEqualTo("userID", userID)
                .whereEqualTo("vehicleID", vehicleUID)
                .get(Source.SERVER)
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful() && !task.getResult().isEmpty()) {
                            QueryDocumentSnapshot document = (QueryDocumentSnapshot) task.getResult().getDocuments().get(0);
                            String documentId = document.getId();

                            double uFuel = document.getDouble("usedFuel");
                            double dFuel = document.getDouble("deliveredFuel");

                            if (delivered) {
                                db.collection("userVehicles").document(documentId)
                                        .update("deliveredFuel", dFuel + volumen);
                            } else {
                                db.collection("userVehicles").document(documentId)
                                        .update("usedFuel", uFuel + volumen);
                            }

                        }
                    }
                });

    }

    private boolean checkTrackConstraints(String mileage, String avgFuel){

        //check if empty
        if(TextUtils.isEmpty(mileage)){
            Toast.makeText(Car.this, "Enter vehicle mileage", Toast.LENGTH_SHORT).show();
            return false;
        }
        if(TextUtils.isEmpty(avgFuel)){
            Toast.makeText(Car.this, "Enter average fuel consumption ", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (mileage == null || !mileage.matches("\\d+")) {
            Toast.makeText(Car.this, "Mileage must be positive integer", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (avgFuel == null || !avgFuel.matches("\\d*\\.?\\d+")) {
            Toast.makeText(Car.this, "Fuel consumption must be positive number", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;

    }

    private boolean checkFuelConstraints(String fuel){

        if(TextUtils.isEmpty(fuel)){
            Toast.makeText(Car.this, "Enter data", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (fuel == null || !fuel.matches("\\d*\\.?\\d+")) {
            Toast.makeText(Car.this, "Must be positive number", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private boolean checkMileageConstraints(String mileage){

        //check if empty
        if(TextUtils.isEmpty(mileage)){
            Toast.makeText(Car.this, "Enter vehicle mileage", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (mileage == null || !mileage.matches("\\d+")) {
            Toast.makeText(Car.this, "Mileage must be positive integer", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;

    }

    private void deleteThisVehicle(View view){

        //deleting all userVehicles documents
        db.collection("userVehicles")
                .whereEqualTo("vehicleID", vehicleUID)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                DocumentReference docRef = document.getReference();

                                String memberID = document.getString("userID");

                                if(memberID == userID)
                                    continue;

                                //decreasing vehicle count in every record
                                DocumentReference memberDocument = db.collection("users").document(memberID);
                                memberDocument.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        DocumentSnapshot document1 = task.getResult();
                                        if(document1.exists()){
                                            Long vehicleCount = document1.getLong("vehicleCount");
                                            memberDocument.update("vehicleCount", (vehicleCount - 1))
                                                    .addOnSuccessListener(aVoid -> {
                                                        Log.d(TAG, "Liczba pojazdów zaktualizowana pomyślnie.");
                                                    })
                                                    .addOnFailureListener(e -> {
                                                        Log.e(TAG, "Błąd przy aktualizacji liczby pojazdów: ", e);
                                                    });

                                        }
                                    }
                                });
                                //deleting documents
                                docRef.delete();
                            }

                            //decreasing vehicle count in every record
                            DocumentReference userDocument = db.collection("users").document(userID);
                            userDocument.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    DocumentSnapshot document1 = task.getResult();
                                    if(document1.exists()){
                                        Long vehicleCount = document1.getLong("vehicleCount");
                                        userDocument.update("vehicleCount", (vehicleCount - 1))
                                                .addOnSuccessListener(aVoid -> {
                                                    Log.d(TAG, "Liczba pojazdów zaktualizowana pomyślnie.");
                                                    DocumentReference vehicleDocument = db.collection("vehicles").document(vehicleUID);
                                                    vehicleDocument.delete();

                                                    //changing some variables
                                                    Toast.makeText(Car.this, "Vehicle deleted", Toast.LENGTH_SHORT).show();
                                                    dialogSettings.dismiss();


                                                    LocalStorage.vehicleCount--;
                                                    if(LocalStorage.vehicleCount > 0){
                                                        Intent intent = new Intent(getApplicationContext(), CarList.class);
                                                        startActivity(intent);
                                                        finish();
                                                    }
                                                    else{
                                                        Intent intent = new Intent(getApplicationContext(), noCarsPage.class);
                                                        startActivity(intent);
                                                        finish();
                                                    }

                                                })
                                                .addOnFailureListener(e -> {
                                                    Log.e(TAG, "Błąd przy aktualizacji liczby pojazdów: ", e);
                                                });

                                    }
                                }
                            });

                        } else {
                            Toast.makeText(Car.this, "Oops, something went wrong!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });




    }

    private void deleteMember(String membernick, View view){

        db.collection("userVehicles")
                .whereEqualTo("nickname", membernick)
                .whereEqualTo("vehicleID", vehicleUID)
                .get(Source.SERVER)
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful() && !task.getResult().isEmpty()){
                            QueryDocumentSnapshot document = (QueryDocumentSnapshot) task.getResult().getDocuments().get(0);

                            String memberID = document.getString("userID");
                            DocumentReference docRef = document.getReference();
                            docRef.delete();


                            //decreasing vehicle count in member document
                            DocumentReference memberDocument = db.collection("users").document(memberID);
                            memberDocument.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    DocumentSnapshot document1 = task.getResult();
                                    if(document1.exists()){
                                        Long vehicleCount = document1.getLong("vehicleCount");
                                        memberDocument.update("vehicleCount", (vehicleCount - 1));
                                    }
                                }
                            });

                            //decreasing member count in vehicle document
                            DocumentReference vehicleDoc = db.collection("vehicles").document(vehicleUID);
                            vehicleDoc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    DocumentSnapshot document1 = task.getResult();
                                    if(document1.exists()){
                                        Long memberCount = document1.getLong("memberCount");
                                        vehicleDoc.update("memberCount", (memberCount - 1));
                                    }
                                }
                            });

                            Toast.makeText(Car.this, membernick + " has been removed", Toast.LENGTH_SHORT).show();
                            dialogRemoveMember.dismiss();

                        }
                        else {
                            Toast.makeText(Car.this, "No user foud with this nickname", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });


    }
}