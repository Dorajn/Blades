package com.app.blades;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.FrameStats;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class CarAdder extends AppCompatActivity {

    EditText inputVehicleName;
    EditText inputVehicleMileage;
    EditText inputVehicleCurrFuelLevel;
    Button createVehicle;
    String userID;

    FirebaseAuth mAuth;
    FirebaseFirestore fStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car_adder);

        inputVehicleName = findViewById(R.id.editTextVehicleName);
        inputVehicleMileage = findViewById(R.id.editTextVehicleMileage);
        inputVehicleCurrFuelLevel = findViewById(R.id.editTextVehiclePetrol);
        createVehicle = findViewById(R.id.createVB);

        mAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();


        createVehicle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                String vName = inputVehicleName.getText().toString();
                String mileage = inputVehicleMileage.getText().toString();
                String petrol = inputVehicleCurrFuelLevel.getText().toString();

                //inserting data to firebase
                userID = mAuth.getCurrentUser().getUid();
                DocumentReference documentReference = fStore.collection("vehicles").document(userID);
                Map<String, Object> vehicle = new HashMap<>();

                vehicle.put("vehicleName", vName);
                vehicle.put("mileage", mileage);
                vehicle.put("fuelLevel", petrol);

                documentReference.set(vehicle);
                Toast.makeText(CarAdder.this, "Vehicle created", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(getApplicationContext(), Cars.class);
                startActivity(intent);
                finish();

            }
        });

    }
}