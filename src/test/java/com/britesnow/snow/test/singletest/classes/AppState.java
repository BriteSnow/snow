package com.britesnow.snow.test.singletest.classes;

import java.util.Random;

public class AppState {

    public int     id = new Random().nextInt();
    
    public boolean init = false;
    public boolean shutdown = false;

}
