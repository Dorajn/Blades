package com.app.blades;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class CarList extends AppCompatActivity {

    Button addCarButton, logoutButton;
    View tile1, tile2, tile3, tile4, tile5;
    TextView car1, car2, car3, car4, car5;
    View[] tiles;
    TextView[] vehicleNames;
    FirebaseAuth auth;
    FirebaseFirestore db;
    String userID;
    long vehiclesCount;
    TextView nick;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car_list);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        nick = findViewById(R.id.hiText2);
        addCarButton = findViewById(R.id.addCarButton2);
        logoutButton = findViewById(R.id.logoutButton2);
        tile1 = findViewById(R.id.tile1);
        tile2 = findViewById(R.id.tile2);
        tile3 = findViewById(R.id.tile3);
        tile4 = findViewById(R.id.tile4);
        tile5 = findViewById(R.id.tile5);

        car1 = findViewById(R.id.vehicle1);
        car2 = findViewById(R.id.vehicle2);
        car3 = findViewById(R.id.vehicle3);
        car4 = findViewById(R.id.vehicle4);
        car5 = findViewById(R.id.vehicle5);

        tiles = new View[5];
        tiles[0] = tile1;
        tiles[1] = tile2;
        tiles[2] = tile3;
        tiles[3] = tile4;
        tiles[4] = tile5;

        vehicleNames = new TextView[5];
        vehicleNames[0] = car1;
        vehicleNames[1] = car2;
        vehicleNames[2] = car3;
        vehicleNames[3] = car4;
        vehicleNames[4] = car5;

        addCarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), CarAdder.class);
                startActivity(intent);
                finish();
            }
        });

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(getApplicationContext(), Login.class);
                startActivity(intent);
                finish();
            }
        });

        userID = auth.getCurrentUser().getUid();
        DocumentReference documentReference = db.collection("users").document(userID);
        documentReference.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                nick.setText("Hi, " + value.getString("nick"));
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();

        //sets number of tiles visible on screen depending on vehicle counter
        DocumentReference userData = db.collection("users").document(userID);
        userData.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                DocumentSnapshot document = task.getResult();
                if(document.exists()) {
                    Long vehicleCount = document.getLong("vehicleCount");
                    vehiclesCount = vehicleCount;

                    for (int i = 0; i < vehicleCount; i++) {
                        tiles[i].setVisibility(View.VISIBLE);
                    }
                }

            }
        });

        //changing vehicle names in tiles
        db.collection("vehicles")
                .document(userID)
                .collection("vehicles")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {

                        if(task.isSuccessful()){
                            int i = 0;
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String name = (String)document.getData().get("vehicleName");
                                vehicleNames[i].setText(name);
                                i++;
                            }
                        }
                        else{
                            Log.e("nie", "Error getting documents: ", task.getException());
                        }
                    }
                });


        tile1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LocalStorage.value = 1;
                Intent intent = new Intent(getApplicationContext(), Car.class);
                startActivity(intent);
                finish();

            }
        });
        tile2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LocalStorage.value = 2;
                Intent intent = new Intent(getApplicationContext(), Car.class);
                startActivity(intent);
                finish();

            }
        });
        tile3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LocalStorage.value = 3;
                Intent intent = new Intent(getApplicationContext(), Car.class);
                startActivity(intent);
                finish();

            }
        });
        tile4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LocalStorage.value = 4;
                Intent intent = new Intent(getApplicationContext(), Car.class);
                startActivity(intent);
                finish();

            }
        });
        tile5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LocalStorage.value = 5;
                Intent intent = new Intent(getApplicationContext(), Car.class);
                startActivity(intent);
                finish();

            }
        });

    }



}

