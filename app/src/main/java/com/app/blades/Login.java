package com.app.blades;

import android.content.Intent;
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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class Login extends AppCompatActivity {

    EditText inputEmail, inputPassword;
    Button loginButton;
    FirebaseAuth mAuth;
    TextView switchToRegister;
    String userID;
    FirebaseFirestore db;
    ProgressBar progressBar;

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            changeIntentWithCarNumCheck();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        inputEmail = findViewById(R.id.editTextEmail);
        inputPassword = findViewById(R.id.editTextPass);
        loginButton =findViewById(R.id.loginButton);
        switchToRegister = findViewById(R.id.switchToRegister);
        progressBar = findViewById(R.id.progressBar);
        db = FirebaseFirestore.getInstance();

        switchToRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), Register.class);
                startActivity(intent);
                finish();
            }
        });


        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                progressBar.setVisibility(View.VISIBLE);

                String email, password;
                email = inputEmail.getText().toString();
                password = inputPassword.getText().toString();

                if(TextUtils.isEmpty(email)){
                    Toast.makeText(Login.this, "Enter email", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    return;
                }
                if(TextUtils.isEmpty(password)){
                    Toast.makeText(Login.this, "Enter password", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    return;
                }

                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                progressBar.setVisibility(View.GONE);
                                if (task.isSuccessful()) {
                                    Toast.makeText(Login.this, "Login successful.", Toast.LENGTH_SHORT).show();
                                    changeIntentWithCarNumCheck();

                                } else {
                                    Toast.makeText(Login.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });


            }
        });

    }


    public void changeIntentWithCarNumCheck(){
        userID = mAuth.getCurrentUser().getUid();

        DocumentReference userData = db.collection("users").document(userID);
        userData.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                DocumentSnapshot document = task.getResult();
                if(document.exists()){
                    Long vehicleCount = document.getLong("vehicleCount");

                    if(vehicleCount > 0){
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

            }
        });
    }
}