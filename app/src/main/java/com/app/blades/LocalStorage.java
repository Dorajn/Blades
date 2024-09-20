package com.app.blades;


import java.util.ArrayList;

public class LocalStorage {

    public static long value;
    public static long carNum;
    public static boolean newAddedCar = false;
    public static String currentVehicleUID;
    public static String currentNewVehicleUID;
    public static String[] UIDs = new String[5];

    public LocalStorage(){
        value = 1;
        carNum = 0;
    }

}
