package com.app.blades;

import static android.app.PendingIntent.getActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
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

    //Settings
    SharedPreferences sharedPref;

    EditText fuelPriceEditText;
    TextView infoCurrentFuelPrice;

    ImageView applyFuelPrice;
    Button goBack;
    Switch lazyCalculations, lazyExtraCalculations;

    FirebaseAuth auth;
    FirebaseFirestore db;

    String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        sharedPref = Settings.this.getSharedPreferences("userSettings", MODE_PRIVATE);

        initializeVariables();
        initializeGoogleVariables();
        createSharedPrefFileIfNotExists();

        goBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), CarList.class);
                startActivity(intent);
                finish();
            }
        });

        applyFuelPrice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String fp = fuelPriceEditText.getText().toString();
                if(!checkFuelConstraints(fp))
                    return;

                double newPrice = Double.parseDouble(fp);

                if (newPrice > 0) {

                    userID = auth.getCurrentUser().getUid();
                    DocumentReference userData = db.collection("users").document(userID);
                    userData.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            DocumentSnapshot document = task.getResult();

                            if (document.exists()) {
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
                else{
                    Toast.makeText(view.getContext(), "Fuel price can't be zero", Toast.LENGTH_SHORT).show();
                }
            }
        });

        lazyCalculations.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putBoolean("lazyFuelCalc", lazyCalculations.isChecked());
                editor.apply();
            }
        });

        lazyExtraCalculations.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putBoolean("lazyExtraFuelCalc", lazyExtraCalculations.isChecked());
                editor.apply();
            }
        });

    }

    private void createSharedPrefFileIfNotExists() {
        if(!sharedPrefExists()){
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putBoolean("lazyFuelCalc", true);
            editor.putBoolean("lazyExtraFuelCalc", false);
            editor.apply();
        }
    }

    private boolean sharedPrefExists() {
        return sharedPref.contains("lazyFuelCalc");
    }

    private void initializeGoogleVariables() {
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        userID = auth.getCurrentUser().getUid();
    }

    private void initializeVariables() {
        fuelPriceEditText = findViewById(R.id.editTextFuelPrice);
        infoCurrentFuelPrice = findViewById(R.id.fuelPriceInfo);
        goBack = findViewById(R.id.goBackSettings);
        applyFuelPrice = findViewById(R.id.applyFuelPrice);
        lazyCalculations = findViewById(R.id.lazySwitch);
        lazyExtraCalculations = findViewById(R.id.lazyExtraSwitch);
    }

    @Override
    protected void onStart() {
        super.onStart();
        getFuelPriceFromDB();
        setSwitches();
    }

    private void setSwitches() {
        lazyCalculations.setChecked(sharedPref.getBoolean("lazyFuelCalc", true));
        lazyExtraCalculations.setChecked(sharedPref.getBoolean("lazyExtraFuelCalc", false));
    }

    private void getFuelPriceFromDB() {
        userID = auth.getCurrentUser().getUid();
        db.collection("users")
                .document(userID)
                .addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {

                        double fPrice = (double) value.getDouble("fuelPrice");
                        LocalStorage.fuelPrice = fPrice;
                        infoCurrentFuelPrice.setText("Fuel price: " + String.valueOf(fPrice));
                    }
                });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(this, CarList.class);
        startActivity(intent);
        finish();
    }

    private boolean checkFuelConstraints(String fuelPrice){

        //check if empty
        if(TextUtils.isEmpty(fuelPrice)){
            Toast.makeText(Settings.this, "Enter fuel price ", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (fuelPrice == null || !fuelPrice.matches("\\d*\\.?\\d+")) {
            Toast.makeText(Settings.this, "Fuel price must be positive number", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;

    }

}