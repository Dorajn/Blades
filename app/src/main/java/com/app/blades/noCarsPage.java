package com.app.blades;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

public class noCarsPage extends AppCompatActivity {

    FirebaseFirestore db;
    FirebaseAuth auth;

    Button logoutButton;
    FirebaseUser user;
    Button addCarButton;
    String userID;
    String nickname;

    TextView nick;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.no_car_page);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        logoutButton = findViewById(R.id.logoutButton);
        addCarButton = findViewById(R.id.addCarButton);
        nick = findViewById(R.id.hiText);

        user = auth.getCurrentUser();

        if(user == null){
            Intent intent = new Intent(getApplicationContext(), Login.class);
            startActivity(intent);
            finish();
        }
        else{
            //pass
        }

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



        addCarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), CarAdder.class);
                intent.putExtra("nickname", nickname);
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
    }

}