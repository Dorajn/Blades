package com.app.blades;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class CarAdder extends AppCompatActivity {

    EditText inputVehicleName;
    EditText inputVehicleMileage;
    EditText inputVehicleCurrFuelLevel;
    Button createVehicle, goBack;
    String userID;
    String nickname;
    ProgressBar progressBar;
    DataBaseMenager dbMenager;

    FirebaseAuth mAuth;
    FirebaseFirestore db;

    @Override
    protected void onStart() {
        super.onStart();
        progressBar.setProgress(0);
    }

    private boolean checkConstraints(String name, String mileage, String petrol){

        //check if empty
        if(TextUtils.isEmpty(name)){
            Toast.makeText(CarAdder.this, "Enter vehicle name", Toast.LENGTH_SHORT).show();
            return false;
        }
        if(TextUtils.isEmpty(mileage)){
            Toast.makeText(CarAdder.this, "Enter vehicle mileage", Toast.LENGTH_SHORT).show();
            return false;
        }
        if(TextUtils.isEmpty(petrol)){
            Toast.makeText(CarAdder.this, "Enter vehicle fuel level", Toast.LENGTH_SHORT).show();
            return false;
        }
        if(name.length() > LocalStorage.MAX_VEHICLE_NAME_LENGHT){
            Toast.makeText(CarAdder.this, "Vehicle name is too long", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (mileage == null || !mileage.matches("\\d+")) {
            Toast.makeText(CarAdder.this, "Mileage must be positive integer", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (petrol == null || !petrol.matches("\\d*\\.?\\d+")) {
            Toast.makeText(CarAdder.this, "Fuel level must be positive number", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car_adder);

        Intent intent = getIntent();
        nickname = intent.getStringExtra("nickname");

        inputVehicleName = findViewById(R.id.editTextVehicleName);
        inputVehicleMileage = findViewById(R.id.editTextVehicleMileage);
        inputVehicleCurrFuelLevel = findViewById(R.id.editTextVehiclePetrol);
        createVehicle = findViewById(R.id.createVB);
        goBack = findViewById(R.id.goBack);
        progressBar = findViewById(R.id.progressBarAddCar);

        dbMenager = new DataBaseMenager();

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        createVehicle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String vName = inputVehicleName.getText().toString();
                String mileage = inputVehicleMileage.getText().toString();
                String petrol = inputVehicleCurrFuelLevel.getText().toString();

                if(checkConstraints(vName, mileage, petrol)) {
                    dbMenager.addVehicle(vName, mileage, petrol, nickname, progressBar, CarAdder.this);
                }

            }
        });

        goBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

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
            }
        });

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

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
    }

}