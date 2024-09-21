package com.app.blades;

import static android.content.ContentValues.TAG;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

public class Car extends AppCompatActivity {

    TextView vehicleName, vehicleMileage, vehicleFuelLevel;
    FirebaseFirestore db;
    FirebaseAuth auth;
    String userID;
    Button changeCar, trackRide, endTracking;
    ImageView fuelChange, mileage;
    EditText fuelLevelEditTextDialog, mileageEditTextDialog;

    Dialog dialogFuel, dialogMileage, dialogMore;
    Button acceptFuelChange, acceptMileageChange;
    String vehicleUID;
    String mileageStored = "0";


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

        //dialogs
        dialogMileage = new Dialog(Car.this);
        dialogMileage.setContentView(R.layout.mileage_change_dialog);
        dialogMileage.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialogMileage.getWindow().setBackgroundDrawable(getDrawable(R.drawable.dialogs_backgroud));

        dialogFuel = new Dialog(Car.this);
        dialogFuel.setContentView(R.layout.fuel_refill_dialog);
        dialogFuel.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialogFuel.getWindow().setBackgroundDrawable(getDrawable(R.drawable.dialogs_backgroud));

        fuelLevelEditTextDialog = dialogFuel.findViewById(R.id.fuelLevelEditTextDialog);
        acceptFuelChange = dialogFuel.findViewById(R.id.fuelAcceptButton);

        mileageEditTextDialog = dialogMileage.findViewById(R.id.mileageEditTextDialog);
        acceptMileageChange = dialogMileage.findViewById(R.id.mileageChangeButtonDialog);

        //firebase variables
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        userID = auth.getCurrentUser().getUid();

        acceptMileageChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String newMileage = mileageEditTextDialog.getText().toString();

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
                            userData.update("mileage", newMileage);
                        }

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

                        vehicleMileage.setText(newMileage);
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
                        if(document.exists()){
                            userData.update("fuelLevel", newFuelLevel);
                        }

                        Toast.makeText(view.getContext(), "Fuel level changed", Toast.LENGTH_SHORT).show();
                        vehicleFuelLevel.setText(newFuelLevel);

                        if(Long.parseLong(newFuelLevel) <= LocalStorage.lowFuelWarning){
                            fuelChange.setImageResource(R.drawable.baseline_local_gas_station_24_red);
                        }
                        else{
                            fuelChange.setImageResource(R.drawable.baseline_local_gas_station_24_white);
                        }

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
            }
        });

        endTracking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                endTracking.setVisibility(View.INVISIBLE);
                trackRide.setVisibility(View.VISIBLE);
            }
        });

    }


    @Override
    protected void onStart() {
        super.onStart();

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
                            String fuelLevel = (String) document.getData().get("fuelLevel");

                            if(Long.parseLong(fuelLevel) <= LocalStorage.lowFuelWarning){
                                fuelChange.setImageResource(R.drawable.baseline_local_gas_station_24_red);
                            }
                            else{
                                fuelChange.setImageResource(R.drawable.baseline_local_gas_station_24_white);
                            }

                            vehicleName.setText(name);
                            vehicleMileage.setText(mileageStored);
                            vehicleFuelLevel.setText(fuelLevel);
                        }
                        else{
                            Log.d("FireStore", "Vehicle not found.");
                        }
                    }
                });


    }

}