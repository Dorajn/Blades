package com.app.blades;

import android.view.View;
import android.widget.TextView;

public class StatisticTile {

    TextView used, refueled, name;
    View tile;

    public StatisticTile(View tile, TextView name, TextView used, TextView refueled){
        this.name = name;
        this.used = used;
        this.refueled = refueled;
        this.tile = tile;
    }

}
