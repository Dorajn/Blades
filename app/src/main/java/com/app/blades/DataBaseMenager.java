package com.app.blades;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Source;
import com.google.firebase.firestore.Transaction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataBaseMenager {

    public String userID;
    FirebaseAuth auth;
    FirebaseFirestore db;

    public DataBaseMenager(){
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        if(auth.getCurrentUser() != null)
            userID = auth.getCurrentUser().getUid();
    }

    public void getUserID(){
        userID = auth.getCurrentUser().getUid();
    }

    private void makeShortToast(Context context, String text){
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
    }

    private void makeLongToast(Context context, String text){
        Toast.makeText(context, text, Toast.LENGTH_LONG).show();
    }


    //method to gather all variables from data base
    public void gatherAllInfoAboutUser(Context context){

        DocumentReference userDocRef = db.collection("users").document(userID);

        db.runTransaction(new Transaction.Function<Map<String, Object>>() {
            @Nullable
            @Override
            public Map<String, Object> apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {

                DocumentSnapshot userDocSnapshot = transaction.get(userDocRef);

                String email = userDocSnapshot.getString("email");
                String nick = userDocSnapshot.getString("nick");
                long vehicleCount = userDocSnapshot.getLong("vehicleCount");
                long fuelPrice = userDocSnapshot.getLong("fuelPrice");

                Map<String, Object> map = new HashMap<>();
                map.put("email", email);
                map.put("nick", nick);
                map.put("vehicleCount", vehicleCount);
                map.put("fuelPrice", fuelPrice);

                return map;
            }
        }).addOnSuccessListener(new OnSuccessListener<Map<String, Object>>() {
            @Override
            public void onSuccess(Map<String, Object> stringObjectMap) {
                LocalStorage.email = (String) stringObjectMap.get("email");
                LocalStorage.userNick = (String) stringObjectMap.get("nick");
                LocalStorage.vehicleCount = (long) stringObjectMap.get("vehicleCount");
                LocalStorage.fuelPrice = (long) stringObjectMap.get("fuelPrice");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                makeShortToast(context,"Error while loading user data");
            }
        });
    }

    //CarAdder activity
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

                List<Double> list = new ArrayList<>();

                //adding relation between user and vehicle
                Map<String, Object> usersVehicle = new HashMap<>();
                usersVehicle.put("userID", userID);
                usersVehicle.put("vehicleID", vehicleDocRef.getId());
                usersVehicle.put("relationType", "owner");
                usersVehicle.put("nickname", nickname);
                usersVehicle.put("usedFuel", 0);
                usersVehicle.put("deliveredFuel", 0);
                usersVehicle.put("averageConsumptionList", list);

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

                //clear the names table
                LocalStorage.vehicleNames.clear();

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

    //Register activity
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

    //CarList activity
    public void setVehiclesIDsToTiles(Context context, TextView[] vehicleNames, ImageView[] warnings){

        setVehicleNamesFromCache(vehicleNames);

        CollectionReference vehicleCollRef = db.collection("vehicles");

        db.collection("userVehicles")
                .whereEqualTo("userID", userID)
                .get(Source.SERVER)
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                        List<String> vehicleIDs = new ArrayList<>();
                        for(QueryDocumentSnapshot document : queryDocumentSnapshots){
                            vehicleIDs.add(document.getString("vehicleID"));
                        }
    
                        db.runTransaction(new Transaction.Function<List<Map<String, Object>>>() {
                            @Nullable
                            @Override
                            public List<Map<String, Object>> apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {

                                List<Map<String, Object>> vehicleDataList = new ArrayList<>();

                                for(String vehicleID : vehicleIDs){
                                    DocumentReference vehicleDocRef = vehicleCollRef.document(vehicleID);
                                    DocumentSnapshot snapshot = transaction.get(vehicleDocRef);

                                    Map<String, Object> map = new HashMap<>();
                                    map.put("vehicleName", (String) snapshot.getString("vehicleName"));
                                    map.put("vehicleID", (String) vehicleID);
                                    map.put("fuelLevel", (String) snapshot.getString("fuelLevel"));

                                    vehicleDataList.add(map);
                                }

                                return vehicleDataList;
                            }
                        }).addOnSuccessListener(new OnSuccessListener<List<Map<String, Object>>>() {
                            @Override
                            public void onSuccess(List<Map<String, Object>> maps) {
                                int i = 0;

                                boolean addVehicleNameToTheList = LocalStorage.vehicleNames.isEmpty() ? true : false;

                                for(Map<String, Object> map : maps){
                                    String vehicleName = map.get("vehicleName").toString();
                                    vehicleNames[i].setText(vehicleName);

                                    if(addVehicleNameToTheList)
                                        LocalStorage.vehicleNames.add(vehicleName);

                                    LocalStorage.UIDs[i] = map.get("vehicleID").toString();

                                    double fuelLevel = Double.parseDouble(map.get("fuelLevel").toString());
                                    if(fuelLevel <= LocalStorage.LOW_FUEL_WARNING){
                                        warnings[i].setVisibility(View.VISIBLE);
                                    }
                                    else{
                                        warnings[i].setVisibility(View.INVISIBLE);
                                    }
                                    i++;
                                }
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                makeShortToast(context, "Error while loading vehicles");
                            }
                        });

                    }
                });

    }

    private static void setVehicleNamesFromCache(TextView[] vehicleNames) {
        if(!LocalStorage.vehicleNames.isEmpty()){
            int i = 0;
            for(String name : LocalStorage.vehicleNames){
                vehicleNames[i].setText(name);
                i++;
            }
        }
    }


    //Car activity
    public void gatherCarData(Context context, String vehicleID, TextView fuelLevel, TextView mileage, TextView name, ImageView warning){

        DocumentReference vehicleDocRef = db.collection("vehicles").document(vehicleID);

        db.runTransaction(new Transaction.Function<Map<String, Object>>() {
            @Nullable
            @Override
            public Map<String, Object> apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {

                DocumentSnapshot snapshot = transaction.get(vehicleDocRef);

                Map<String, Object> map = new HashMap<>();
                map.put("vehicleName", (String) snapshot.getString("vehicleName"));
                map.put("mileage", (String) snapshot.getString("mileage"));
                map.put("fuelLevel", (String) snapshot.getString("fuelLevel"));

                return map;
            }
        }).addOnSuccessListener(new OnSuccessListener<Map<String, Object>>() {
            @Override
            public void onSuccess(Map<String, Object> map) {

                name.setText(map.get("vehicleName").toString());
                mileage.setText("Mileage: " + map.get("mileage").toString());

                double fLevel = Double.parseDouble(map.get("fuelLevel").toString());
                String fLevel2digits = String.format("%.2f", fLevel);
                fuelLevel.setText("Fuel: " + fLevel2digits);

                Car.mileageStored =  map.get("mileage").toString();
                Car.fuelLevelStored = map.get("fuelLevel").toString();


                if(fLevel <= LocalStorage.LOW_FUEL_WARNING)
                    warning.setImageResource(R.drawable.baseline_local_gas_station_24_red);
                else
                    warning.setImageResource(R.drawable.baseline_local_gas_station_24_white);

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                makeShortToast(context, "Error while loading vehicle info.");
            }
        });

    }

    public void decideWhetherUserCanDeleteMembers(Context context, String vehicleID, Dialog dialogSettings, Dialog dialogRemoveMember){

        DocumentReference vehicleDocRef = db.collection("vehicles").document(vehicleID);

        db.collection("userVehicles")
                .whereEqualTo("userID", userID)
                .whereEqualTo("vehicleID", vehicleID)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        DocumentSnapshot userVehicleDoc = queryDocumentSnapshots.getDocuments().get(0);
                        if((userVehicleDoc.getString("relationType").toString()).equals("owner")){

                            db.collection("vehicles")
                                    .document(vehicleID)
                                    .get()
                                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                        @Override
                                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                                            if(documentSnapshot.getLong("memberCount") == 1)
                                                makeLongToast(context, "In this car there are no members besides you");
                                            else{
                                                dialogSettings.dismiss();
                                                dialogRemoveMember.show();
                                            }
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            makeShortToast(context, "Oops, something went wrong.");
                                        }
                                    });


                        }
                        else
                            makeShortToast(context, "You are not vehicle owner.");

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        makeShortToast(context, "Oops, something went wrong.");
                    }
                });
    }

    public void deleteMember(Context context, String memberNick, String vehicleID, Dialog dialog){

        db.collection("userVehicles")
                .whereEqualTo("nickname", memberNick)
                .whereEqualTo("vehicleID", vehicleID)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                        if(queryDocumentSnapshots.isEmpty()){
                            makeShortToast(context, "No user foud with this nickname");
                        }
                        else{

                            DocumentSnapshot documentSnapshot = queryDocumentSnapshots.getDocuments().get(0);
                            DocumentReference documentRef = documentSnapshot.getReference();
                            String memberID = documentSnapshot.getString("userID");

                            db.runTransaction(new Transaction.Function<Object>() {
                                @Nullable
                                @Override
                                public Object apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {

                                    //decrease vehicle count
                                    DocumentReference memberDocRef = db.collection("users").document(memberID);
                                    transaction.update(memberDocRef, "vehicleCount", FieldValue.increment(-1));

                                    //decrease member count
                                    DocumentReference vehicleDocRef = db.collection("vehicles").document(vehicleID);
                                    transaction.update(vehicleDocRef, "memberCount", FieldValue.increment(-1));

                                    transaction.delete(documentRef);

                                    return null;
                                }
                            }).addOnSuccessListener(new OnSuccessListener<Object>() {
                                @Override
                                public void onSuccess(Object o) {
                                    makeShortToast(context, memberNick + " has been removed.");
                                    dialog.dismiss();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    makeShortToast(context, "Oops, something went wrong.");
                                }
                            });

                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        makeShortToast(context, "Oops, something went wrong.");
                    }
                });

    }

    public void deleteVehicle(Context context, String vehicleID, Dialog dialog){

        CollectionReference userCollectionReference = db.collection("users");
        DocumentReference deletedVehicleRef = db.collection("vehicles").document(vehicleID);

        db.collection("userVehicles")
                .whereEqualTo("vehicleID", vehicleID)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                        List<DocumentReference> docs = new ArrayList<>();
                        for(DocumentSnapshot documentSnapshot : queryDocumentSnapshots){
                            docs.add(documentSnapshot.getReference());
                        }

                        db.runTransaction(new Transaction.Function<Object>() {
                            @Nullable
                            @Override
                            public Object apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {

                                for(DocumentReference docRef : docs){

                                    DocumentSnapshot document = transaction.get(docRef);
                                    String memberID = (String)document.getString("userID");
                                    transaction.delete(docRef);

                                    DocumentReference userDocRef = userCollectionReference.document(memberID);
                                    transaction.update(userDocRef, "vehicleCount", FieldValue.increment(-1));

                                }

                                transaction.delete(deletedVehicleRef);

                                return null;
                            }
                        }).addOnSuccessListener(new OnSuccessListener<Object>() {
                            @Override
                            public void onSuccess(Object o) {

                                makeShortToast(context, "Vehicle deleted.");

                                LocalStorage.vehicleCount--;
                                if(LocalStorage.vehicleCount > 0){
                                    Intent intent = new Intent(context, CarList.class);
                                    context.startActivity(intent);
                                    ((Activity) context).finish();
                                }
                                else{
                                    Intent intent = new Intent(context, noCarsPage.class);
                                    context.startActivity(intent);
                                    ((Activity) context).finish();
                                }

                                dialog.dismiss();

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                makeShortToast(context, "Oops, something went wrong.1");
                            }
                        });


                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });
    }

    public void setAverageFuelConsumption(Context context, String vehicleID, EditText editText){
        SharedPreferences sharedPref = context.getSharedPreferences("userSettings", Context.MODE_PRIVATE);
        if(sharedPref.getBoolean("lazyExtraFuelCalc", false))
            getAvgExtraFuelConsumption(context, vehicleID, editText);
        else if(sharedPref.getBoolean("lazyFuelCalc", true))
            getAvgFuelConsumption(context, vehicleID, editText);

    }

    private void getAvgFuelConsumption(Context context, String vehicleID, EditText editText) {

        db.collection("userVehicles")
                .whereEqualTo("userID", userID)
                .whereEqualTo("vehicleID", vehicleID)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        DocumentSnapshot document = queryDocumentSnapshots.getDocuments().get(0);
                        DocumentReference docRef = document.getReference();

                        db.runTransaction(new Transaction.Function<List<Double>>() {
                            @Nullable
                            @Override
                            public List<Double> apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {

                                DocumentSnapshot doc = transaction.get(docRef);
                                List<Double> list = (List<Double>) doc.get("averageConsumptionList");
                                return list;

                            }
                        }).addOnSuccessListener(new OnSuccessListener<List<Double>>() {
                            @Override
                            public void onSuccess(List<Double> list) {

                                if(list == null || list.isEmpty())
                                    editText.setText("");
                                else{
                                    double sum = 0;
                                    for (double elem : list){
                                        sum += elem;
                                    }

                                    editText.setText(String.format("%.2f", sum / list.size()));
                                }
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                editText.setText("blad tranzakcji");
                            }
                        });

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        editText.setText("blad dokumentu");
                    }
                });
    }

    private String getAvgExtraFuelConsumption(Context context, String vehicleID, EditText editText){
        return "";
        //not developed yet
    }

    public void addFuelConsumptionToArray(double avg, String vehicleID){
        db.collection("userVehicles")
                .whereEqualTo("userID", userID)
                .whereEqualTo("vehicleID", vehicleID)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        DocumentSnapshot document = queryDocumentSnapshots.getDocuments().get(0);
                        List<Double> list = (List<Double>) document.get("averageConsumptionList");

                        addNewDataToCyclicArray(list, avg);
                        document.getReference().update("averageConsumptionList", list);
                    }
                });
    }

    private void addNewDataToCyclicArray(List<Double> list, double value){
        if(list.size() == LocalStorage.MAX_DEQUE_SIZE){
            for(int i = 0; i < LocalStorage.MAX_DEQUE_SIZE - 1; i++){
                list.set(i, list.get(i + 1));
            }
            list.set(LocalStorage.MAX_DEQUE_SIZE - 1, value);
        }
        else{
            list.add(value);
        }
    }

//    public void addMember(Context context, String nick, String vehicleID){
//
//        db.runTransaction(new Transaction.Function<Object>() {
//            @Nullable
//            @Override
//            public Object apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {
//
//                QuerySnapshot userQuerySnapshot = db.collection("users")
//                        .whereEqualTo("nick", nick)
//                        .get()
//                        .getResult();
//
//                //checking if it is user with this nickname
//                if(userQuerySnapshot != null && !userQuerySnapshot.isEmpty()){
//                    DocumentSnapshot document = userQuerySnapshot.getDocuments().get(0);
//                    String memberID = document.getString("userID");
//                    long userVehicleCount = document.getLong("vehicleCount");
//
//                    QuerySnapshot userVehiclesQuerySnapshot = db.collection("userVehicles")
//                            .whereEqualTo("userId", memberID)
//                            .whereEqualTo("vehicleID", vehicleID)
//                            .get()
//                            .getResult();
//
//                    //checking if user is not already in vehicle
//                    if(userVehiclesQuerySnapshot != null && !userVehiclesQuerySnapshot.isEmpty()){
//                        if(userVehicleCount == 5){
//
//                            //adding record to relation table
//                            Map<String, Object> userVehicle = new HashMap<>();
//                            userVehicle.put("userID", newMemberID);
//                            userVehicle.put("vehicleID", vehicleUID);
//                            userVehicle.put("relationType", "member");
//                            userVehicle.put("nickname", nickname);
//                            userVehicle.put("usedFuel", 0);
//                            userVehicle.put("deliveredFuel", 0);
//                            db.collection("userVehicles").add(userVehicle);
//
//                        }
//                        else{
//                            makeShortToast(context, nick + " reached vehicle limit.");
//                        }
//                    }
//                    else{
//                        makeShortToast(context, "User is already a member.");
//                    }
//                }
//                else{
//                    makeShortToast(context, "No username found with this nickname.");
//                }
//
//                return null;
//            }
//        });
//
//    }
}
