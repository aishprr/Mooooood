package com.example.aishpr.mooooood;

import android.app.Application;

import com.parse.Parse;

/**
 * Created by Tushita on 9/12/2015.
 */
public class MoodApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Parse.enableLocalDatastore(this);

        Parse.initialize(this, "rFMfBygvKgsLkhk3x63sXwBkcfRHPKfzv7Ylu8eO", "qRYoszUqc3inmdMljMy0EPGCjbOkGfZlaGafUUXU");

    }
}
