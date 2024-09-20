package com.app.blades;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.os.LocaleList;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class CarAdder extends AppCompatActivity {

    EditText inputVehicleName;
    EditText inputVehicleMileage;
    EditText inputVehicleCurrFuelLevel;
    Button createVehicle, goBack;
    String userID;

    FirebaseAuth mAuth;
    FirebaseFirestore db;

    @Override
    protected void onStart() {
        super.onStart();

        createVehicle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String vName = inputVehicleName.getText().toString();
                String mileage = inputVehicleMileage.getText().toString();
                String petrol = inputVehicleCurrFuelLevel.getText().toString();

                addDataToDataBase(vName, mileage, petrol);

            }
        });

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car_adder);

        inputVehicleName = findViewById(R.id.editTextVehicleName);
        inputVehicleMileage = findViewById(R.id.editTextVehicleMileage);
        inputVehicleCurrFuelLevel = findViewById(R.id.editTextVehiclePetrol);
        createVehicle = findViewById(R.id.createVB);
        goBack = findViewById(R.id.goBack);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();


        goBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(LocalStorage.carNum > 0){
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

    public void addDataToDataBase(String vehicleName, String vehicleMileage, String vehicleFuelLevel){

        Map<String, Object> vehicle = new HashMap<>();
        vehicle.put("vehicleName", vehicleName);
        vehicle.put("mileage", vehicleMileage);
        vehicle.put("fuelLevel", vehicleFuelLevel);


        userID = mAuth.getCurrentUser().getUid();
        db.collection("vehicles").document(userID).collection("vehicles").add(vehicle)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Toast.makeText(CarAdder.this, "Vehicle created.", Toast.LENGTH_SHORT).show();
                        LocalStorage.currentNewVehicleUID = documentReference.getId();


                        DocumentReference userData = db.collection("users").document(userID);
                        userData.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                DocumentSnapshot document = task.getResult();
                                if(document.exists()){

                                    Long vehicleCount = document.getLong("vehicleCount");

                                    userData.update("vehicleCount", (vehicleCount + 1))
                                            .addOnSuccessListener(aVoid -> {
                                                Log.d(TAG, "Liczba pojazdów zaktualizowana pomyślnie.");
                                            })
                                            .addOnFailureListener(e -> {
                                                Log.e(TAG, "Błąd przy aktualizacji liczby pojazdów: ", e);
                                            });

                                    LocalStorage.value = 1;
                                }

                            }
                        });

                        LocalStorage.carNum += 1;
                        LocalStorage.value = 1;
                        LocalStorage.newAddedCar = true;

                        Intent intent = new Intent(getApplicationContext(), Car.class);
                        startActivity(intent);
                        finish();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(CarAdder.this, "Oops, something went wrong.", Toast.LENGTH_SHORT).show();
                    }
                });



    }

}