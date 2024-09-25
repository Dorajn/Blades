package com.app.blades;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Source;

import java.util.Map;

public class Statistics extends AppCompatActivity {

    Button goBack;
    long memberCount = 1;
    String userID;
    String vehicleID;

    FirebaseAuth auth;
    FirebaseFirestore db;

    //tile variables
    StatisticTile[] statTiles = new StatisticTile[5];

    View tile1, tile2, tile3, tile4, tile5;
    TextView member1, used1, refueled1;
    TextView member2, used2, refueled2;
    TextView member3, used3, refueled3;
    TextView member4, used4, refueled4;
    TextView member5, used5, refueled5;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        userID = auth.getCurrentUser().getUid();

        Intent intent = getIntent();
        vehicleID = intent.getStringExtra("vehicleID");

        goBack = findViewById(R.id.goBackStat);

        //tiles variables
        tile1 = findViewById(R.id.memberTile1);
        tile2 = findViewById(R.id.memberTile2);
        tile3 = findViewById(R.id.memberTile3);
        tile4 = findViewById(R.id.memberTile4);
        tile5 = findViewById(R.id.memberTile5);

        member1 = findViewById(R.id.memberNick1);
        member2 = findViewById(R.id.memberNick2);
        member3 = findViewById(R.id.memberNick3);
        member4 = findViewById(R.id.memberNick4);
        member5 = findViewById(R.id.memberNick5);

        used1 = findViewById(R.id.used1);
        used2 = findViewById(R.id.used2);
        used3 = findViewById(R.id.used3);
        used4 = findViewById(R.id.used4);
        used5 = findViewById(R.id.used5);

        refueled1 = findViewById(R.id.refueled1);
        refueled2 = findViewById(R.id.refueled2);
        refueled3 = findViewById(R.id.refueled3);
        refueled4 = findViewById(R.id.refueled4);
        refueled5 = findViewById(R.id.refueled5);


        statTiles[0] = new StatisticTile(tile1, member1, used1, refueled1);
        statTiles[1] = new StatisticTile(tile2, member2, used2, refueled2);
        statTiles[2] = new StatisticTile(tile3, member3, used3, refueled3);
        statTiles[3] = new StatisticTile(tile4, member4, used4, refueled4);
        statTiles[4] = new StatisticTile(tile5, member5, used5, refueled5);


        goBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), Car.class);
                startActivity(intent);
                finish();
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d("StatDev", vehicleID);
        db.collection("vehicles")
                .document(vehicleID)
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                        memberCount = value.getLong("memberCount");
                        Log.d("StatDev", String.valueOf(memberCount));

                        for(int i = 1; i < memberCount; i++){
                            statTiles[i].tile.setVisibility(View.VISIBLE);
                        }
                    }
                });

        db.collection("userVehicles")
                .whereEqualTo("vehicleID", vehicleID)
                .get(Source.SERVER)
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {

                            int i = 0;
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Map<String, Object> data = document.getData();
                                statTiles[i].name.setText((String) data.get("nickname"));
//                                statTiles[i].used.setText("Used: " + String.format("%.2f", (long)data.get("usedFuel")));
//                                statTiles[i].refueled.setText("Refueled: " + String.format("%.2f", (long)data.get("deliveredFuel")));
                                i++;
                            }
                        } else {
                            Log.w("Firestore", "Error getting documents.", task.getException());
                        }
                    }
                });



    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(this, Car.class);
        startActivity(intent);
        finish();
    }

}