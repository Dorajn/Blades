package com.app.blades;

import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

public class Cars extends AppCompatActivity {

    TextView vehicleName, vehicleMileage, vehicleFuelLevel;
    FirebaseFirestore fStore;
    FirebaseAuth auth;
    String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cars);

        vehicleName = findViewById(R.id.carName);
        vehicleMileage = findViewById(R.id.mileage);
        vehicleFuelLevel = findViewById(R.id.petrolLevel);

        fStore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();


        userID = auth.getCurrentUser().getUid();
        DocumentReference documentReference = fStore.collection("vehicles").document(userID);
        documentReference.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                vehicleName.setText(value.getString("vehicleName"));
                vehicleMileage.setText("Mileage: " + value.getString("mileage"));
                vehicleFuelLevel.setText("Fuel: " + value.getString("fuelLevel"));
            }
        });

    }
}