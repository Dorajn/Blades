package com.app.blades;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Register extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    EditText inputEmail, inputNick, inputPassword;
    Button registerButton;
    FirebaseAuth mAuth;
    TextView switchToLogin;
    ProgressBar progressBar;
    FirebaseFirestore db;
    String userID;
    DataBaseMenager dbMenager;

    @Override
    public void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            //dbMenager.gatherAllInfoAboutUser(Register.this);
            dbMenager.changeIntentDependingOnVehicleCount(Register.this);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        dbMenager = new DataBaseMenager();

        inputEmail = findViewById(R.id.editTextEmail);
        inputNick = findViewById(R.id.editTextNick);
        inputPassword = findViewById(R.id.editTextPass);
        registerButton =findViewById(R.id.createAccountButton);
        switchToLogin = findViewById(R.id.switchToLogin);
        progressBar = findViewById(R.id.progressBar);

        switchToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), Login.class);
                startActivity(intent);
                finish();
            }
        });


        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email, password, nick;
                email = inputEmail.getText().toString();
                password = inputPassword.getText().toString();
                nick = inputNick.getText().toString();

                if(TextUtils.isEmpty(email)){
                    Toast.makeText(Register.this, "Enter email", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(TextUtils.isEmpty(password)){
                    Toast.makeText(Register.this, "Enter password", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(TextUtils.isEmpty(nick)){
                    Toast.makeText(Register.this, "Enter your nick", Toast.LENGTH_SHORT).show();
                    return;
                }

                if(password.length() < 8){
                    Toast.makeText(Register.this, "Password should be at least 8 characters", Toast.LENGTH_LONG).show();
                    return;
                }

                progressBar.setVisibility(View.VISIBLE);
                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                progressBar.setVisibility(View.GONE);

                                if (task.isSuccessful()) {
                                    Toast.makeText(Register.this, "Account created.", Toast.LENGTH_SHORT).show();


                                    //inserting data to firebase
                                    Map<String, Object> user = new HashMap<>();
                                    user.put("nick", nick);
                                    user.put("email", email);
                                    user.put("vehicleCount", 0);
                                    user.put("fuelPrice", 6);
                                    //user.put("vehicles", new ArrayList<>());

                                    userID = mAuth.getCurrentUser().getUid();
                                    dbMenager.getUserID();
                                    db.collection("users").document(userID).set(user);

                                    Intent intent = new Intent(getApplicationContext(), noCarsPage.class);
                                    startActivity(intent);
                                    finish();
                                }
                                else {
                                    Toast.makeText(Register.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if(grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(Register.this, "This app needs location tracking permission", Toast.LENGTH_LONG).show();
            }
        }
    }
}