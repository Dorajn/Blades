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
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Source;

public class Car extends AppCompatActivity {

    TextView vehicleName, vehicleMileage, vehicleFuelLevel;
    FirebaseFirestore db;
    FirebaseAuth auth;
    String userID;
    Button changeCar;

    @Override
    protected void onStart() {
        super.onStart();

        db.collection("vehicles")
                .document(userID)
                .collection("vehicles")
                .get(Source.SERVER)
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {

                        if(task.isSuccessful()){

                            int i = 1;

                            for (QueryDocumentSnapshot document : task.getResult()) {

                                if(i == CurrentCar.value) {

                                    String name = (String) document.getData().get("vehicleName");
                                    String mileage = (String) document.getData().get("mileage");
                                    String fuelLevel = (String) document.getData().get("fuelLevel");

                                    vehicleName.setText(name);
                                    vehicleMileage.setText(mileage);
                                    vehicleFuelLevel.setText(fuelLevel);

                                    break;
                                }
                                i++;

                            }
                        }
                        else{
                            Log.e("nie", "Error getting documents: ", task.getException());
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

    }
}