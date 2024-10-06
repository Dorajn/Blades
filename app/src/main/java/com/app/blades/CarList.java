package com.app.blades;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
import com.google.firebase.firestore.Source;

public class CarList extends AppCompatActivity {

    Button addCarButton, logoutButton;
    View tile1, tile2, tile3, tile4, tile5;
    TextView car1, car2, car3, car4, car5;
    ImageView war1, war2, war3, war4, war5, settings;

    View[] tiles;
    TextView[] vehicleNames;
    ImageView[] warnings;
    DataBaseMenager dbMenager;

    FirebaseAuth auth;
    FirebaseFirestore db;
    String userID;
    long vehiclesCount;
    TextView nick;
    String nickname;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car_list);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        dbMenager = new DataBaseMenager();
        dbMenager.gatherAllInfoAboutUser(CarList.this);

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

        war1 = findViewById(R.id.warning1);
        war2 = findViewById(R.id.warning2);
        war3 = findViewById(R.id.warning3);
        war4 = findViewById(R.id.warning4);
        war5 = findViewById(R.id.warning5);

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

        warnings = new ImageView[5];
        warnings[0] = war1;
        warnings[1] = war2;
        warnings[2] = war3;
        warnings[3] = war4;
        warnings[4] = war5;

        settings = findViewById(R.id.settings);

        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), Settings.class);
                startActivity(intent);
                finish();
            }
        });

        addCarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(LocalStorage.vehicleCount == 5){
                    Toast.makeText(view.getContext(), "You've reached maximum vehicles number", Toast.LENGTH_LONG).show();
                }
                else{
                    Intent intent = new Intent(getApplicationContext(), CarAdder.class);
                    intent.putExtra("nickname", nickname);
                    startActivity(intent);
                    finish();
                }
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
        db.collection("users")
                .document(userID)
                .addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                        nick.setText("Hi, " + value.getString("nick"));
                        nickname = value.getString("nick");
                        LocalStorage.userNick = nickname;
                    }
                });

    }

    @Override
    protected void onStart() {
        super.onStart();

        for (int i = 0; i < 5; i++) {
            if(i < LocalStorage.vehicleCount)
                tiles[i].setVisibility(View.VISIBLE);
            else
                tiles[i].setVisibility(View.INVISIBLE);
        }

        //sets number of tiles visible on screen depending on vehicle counter
        dbMenager.setVehiclesIDsToTiles(CarList.this, vehicleNames, warnings);


        tile1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                LocalStorage.newAddedCar = false;
                if(LocalStorage.vehicleCount > 0){
                    LocalStorage.currentVehicleUID = LocalStorage.UIDs[0];
                }

                Intent intent = new Intent(getApplicationContext(), Car.class);
                startActivity(intent);
                finish();

            }
        });
        tile2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LocalStorage.newAddedCar = false;
                if(LocalStorage.vehicleCount > 1){
                    LocalStorage.currentVehicleUID = LocalStorage.UIDs[1];
                }
                Intent intent = new Intent(getApplicationContext(), Car.class);
                startActivity(intent);
                finish();

            }
        });
        tile3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LocalStorage.newAddedCar = false;
                if(LocalStorage.vehicleCount > 2){
                    LocalStorage.currentVehicleUID = LocalStorage.UIDs[2];
                }
                Intent intent = new Intent(getApplicationContext(), Car.class);
                startActivity(intent);
                finish();

            }
        });
        tile4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LocalStorage.newAddedCar = false;
                if(LocalStorage.vehicleCount > 3){
                    LocalStorage.currentVehicleUID = LocalStorage.UIDs[3];
                }
                Intent intent = new Intent(getApplicationContext(), Car.class);
                startActivity(intent);
                finish();

            }
        });
        tile5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LocalStorage.newAddedCar = false;
                if(LocalStorage.vehicleCount > 4){
                    LocalStorage.currentVehicleUID = LocalStorage.UIDs[4];
                }
                Intent intent = new Intent(getApplicationContext(), Car.class);
                startActivity(intent);
                finish();

            }
        });

    }



}