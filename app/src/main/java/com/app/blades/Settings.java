package com.app.blades;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Source;

public class Settings extends AppCompatActivity {

    EditText fuelPriceEditText;
    TextView infoCurrentFuelPrice;

    Button goBack, apply;

    FirebaseAuth auth;
    FirebaseFirestore db;

    String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        fuelPriceEditText = findViewById(R.id.editTextFuelPrice);
        infoCurrentFuelPrice = findViewById(R.id.fuelPriceInfo);
        goBack = findViewById(R.id.goBackSettings);
        apply = findViewById(R.id.applyButton2);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        userID = auth.getCurrentUser().getUid();

        goBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), CarList.class);
                startActivity(intent);
                finish();
            }
        });

        apply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                double newPrice = Double.parseDouble(fuelPriceEditText.getText().toString());

                if(newPrice > 0){

                    userID = auth.getCurrentUser().getUid();
                    DocumentReference userData = db.collection("users").document(userID);
                    userData.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            DocumentSnapshot document = task.getResult();

                            if(document.exists()){
                                Toast.makeText(view.getContext(), "Fuel price changed", Toast.LENGTH_SHORT).show();
                                infoCurrentFuelPrice.setText("Fuel price: " + String.valueOf(newPrice));
                                userData.update("fuelPrice", newPrice);
                                LocalStorage.fuelPrice = newPrice;
                            }

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(view.getContext(), "Error occured", Toast.LENGTH_SHORT).show();
                        }
                    });

                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        userID = auth.getCurrentUser().getUid();
        db.collection("users")
                .document(userID)
                .addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {

                        double fPrice = (double)value.getDouble("fuelPrice");
                        Log.d("witj", String.valueOf(fPrice));
                        LocalStorage.fuelPrice = fPrice;
                        infoCurrentFuelPrice.setText("Fuel price: " + String.valueOf(fPrice));
                    }
                });
    }
}