package com.app.blades;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Source;

public class Car extends AppCompatActivity {

    TextView vehicleName, vehicleMileage, vehicleFuelLevel;
    FirebaseFirestore db;
    FirebaseAuth auth;
    String userID;
    Button changeCar, trackRide, endTracking;

    @Override
    protected void onStart() {
        super.onStart();

        String vehicleUID;
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
                            String mileage = (String) document.getData().get("mileage");
                            String fuelLevel = (String) document.getData().get("fuelLevel");

                            vehicleName.setText(name);
                            vehicleMileage.setText(mileage);
                            vehicleFuelLevel.setText(fuelLevel);
                        }
                        else{
                            Log.d("FireStore", "Vehicle not found.");
                        }
                    }
                });


    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car);

        vehicleName = findViewById(R.id.carName);
        vehicleMileage = findViewById(R.id.mileage);
        vehicleFuelLevel = findViewById(R.id.petrolLevel);
        trackRide = findViewById(R.id.trackRideButton);
        endTracking = findViewById(R.id.endOfTracking);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        changeCar = findViewById(R.id.changeCarButton);

        userID = auth.getCurrentUser().getUid();

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
}