package com.anvei.dab.mydome;

import android.app.Application;

/**
 * Created by DAB on 2016/12/8 15:13.
 */

public class MyApplication extends Application{
    private static MyApplication instance;
    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }

    public static MyApplication getInstance() {
        return instance;
    }
}
