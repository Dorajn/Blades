package com.app.blades;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;

public class CarAdder extends AppCompatActivity {

    EditText inputVehicleName;
    EditText inputVehicleMileage;
    EditText inputVehicleCurrFuelLevel;
    Button createVehicle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car_adder);

        inputVehicleName = findViewById(R.id.editTextVehicleName);
        inputVehicleMileage = findViewById(R.id.editTextVehicleMileage);
        inputVehicleCurrFuelLevel = findViewById(R.id.editTextVehiclePetrol);
        createVehicle = findViewById(R.id.createVB);


        createVehicle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Toast.makeText(CarAdder.this, "Clicked", Toast.LENGTH_SHORT).show();

                String vName = inputVehicleName.getText().toString();
                String mileage = inputVehicleMileage.getText().toString();
                String petrol = inputVehicleCurrFuelLevel.getText().toString();



            }
        });

    }
}