package com.app.blades;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class LocalStorage {

    //user variables
    public static String userNick;
    public static long vehicleCount;
    public static double fuelPrice = 5.99;
    public static String email;

    public static boolean newAddedCar = false;
    public static String currentVehicleUID;
    public static String currentVehicleName;
    public static String currentNewVehicleUID;
    public static String[] UIDs = new String[5];
    public static List<String> vehicleNames = new ArrayList<>();

    public static int MAX_DEQUE_SIZE = 100;
    public static final long lowFuelWarning = 3;
    public static final long maxVehicleNameLenght = 20;


}
