package com.app.blades;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Source;
import com.google.firebase.firestore.Transaction;

import java.util.HashMap;
import java.util.Map;

public class DataBaseMenager {

    public String userID;
    FirebaseAuth auth;
    FirebaseFirestore db;

    public DataBaseMenager(){
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        userID = auth.getCurrentUser().getUid();
    }

    private void makeShortToast(Context context, String text){
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
    }

    private void makeLongToast(Context context, String text){
        Toast.makeText(context, text, Toast.LENGTH_LONG).show();
    }


    public void addVehicle(String vehicleName, String vehicleMileage, String vehicleFuelLevel, String nickname, ProgressBar progressBar, Context context){

        progressBar.setProgress(25);

        db.runTransaction(new Transaction.Function<String>() {
            @Nullable
            @Override
            public String apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {

                //adding vehicle
                Map<String, Object> vehicle = new HashMap<>();
                vehicle.put("vehicleName", vehicleName);
                vehicle.put("mileage", vehicleMileage);
                vehicle.put("fuelLevel", vehicleFuelLevel);
                vehicle.put("memberCount", 1);

                DocumentReference vehicleDocRef = db.collection("vehicles").document();
                transaction.set(vehicleDocRef, vehicle);


                //adding relation between user and vehicle
                Map<String, Object> usersVehicle = new HashMap<>();
                usersVehicle.put("userID", userID);
                usersVehicle.put("vehicleID", vehicleDocRef.getId());
                usersVehicle.put("relationType", "owner");
                usersVehicle.put("nickname", nickname);
                usersVehicle.put("usedFuel", 0);
                usersVehicle.put("deliveredFuel", 0);

                DocumentReference usersVehiclesDocRef = db.collection("userVehicles").document();
                transaction.set(usersVehiclesDocRef, usersVehicle);


                //incrementing vehicle count
                DocumentReference userDocRef = db.collection("users").document(userID);
                userDocRef.update("vehicleCount", FieldValue.increment(1));


                return vehicleDocRef.getId();
            }
        }).addOnSuccessListener(new OnSuccessListener<String>() {
            @Override
            public void onSuccess(String vehicleDocID) {

                progressBar.setProgress(100);
                makeShortToast(context, "Created vehicle.");

                LocalStorage.currentNewVehicleUID = vehicleDocID;
                LocalStorage.vehicleCount += 1;
                LocalStorage.newAddedCar = true;

                Intent intent = new Intent(context, Car.class);
                context.startActivity(intent);
                ((Activity) context).finish();

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                makeShortToast(context, "Oops, something went wrong.");
                progressBar.setProgress(0);
            }
        });

    }

    public void changeIntentDependingOnVehicleCount(Context context){

        //this method is being called from register activity so this is essential
        userID = auth.getCurrentUser().getUid();

        db.collection("users")
                .document(userID)
                .get(Source.SERVER)
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot document) {
                        if(document.exists()){
                            Long vehicleCount = document.getLong("vehicleCount");
                            LocalStorage.vehicleCount = vehicleCount;

                            if(vehicleCount > 0){
                                Intent intent = new Intent(context, CarList.class);
                                context.startActivity(intent);
                                ((Activity) context).finish();
                            }
                            else{
                                Intent intent = new Intent(context, noCarsPage.class);
                                context.startActivity(intent);
                                ((Activity) context).finish();
                            }
                        }
                    }
                });
    }

}
