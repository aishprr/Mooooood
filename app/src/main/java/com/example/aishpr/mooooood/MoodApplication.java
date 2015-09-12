package com.example.aishpr.mooooood;

import android.app.Application;

import com.parse.Parse;

<<<<<<< HEAD

/**
 * Created by Tushita on 9/12/2015.
 */
public class MoodApplication extends Application{
=======
/**
 * Created by Tushita on 9/12/2015.
 */
public class MoodApplication extends Application {

>>>>>>> master
    @Override
    public void onCreate() {
        super.onCreate();
        Parse.enableLocalDatastore(this);
<<<<<<< HEAD
        Parse.initialize(this, "rFMfBygvKgsLkhk3x63sXwBkcfRHPKfzv7Ylu8eO", "qRYoszUqc3inmdMljMy0EPGCjbOkGfZlaGafUUXU");
=======

        Parse.initialize(this, "rFMfBygvKgsLkhk3x63sXwBkcfRHPKfzv7Ylu8eO", "qRYoszUqc3inmdMljMy0EPGCjbOkGfZlaGafUUXU");

>>>>>>> master
    }
}
