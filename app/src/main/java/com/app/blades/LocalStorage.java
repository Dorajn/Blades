package com.app.blades;


import java.util.ArrayList;

public class LocalStorage {

    public static long value;
    public static long carNum;
    public static ArrayList<String> tileMenager;

    public LocalStorage(){
        value = 1;
        carNum = 0;
        tileMenager = new ArrayList<>();
    }

}
