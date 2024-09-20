package com.app.blades;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class LocalStorage {

    public static long vehicleCount;

    public static boolean newAddedCar = false;
    public static String currentVehicleUID;
    public static String currentNewVehicleUID;
    public static String[] UIDs = new String[5];

    public static final long lowFuelWarning = 3;


}
