package com.example.aishpr.mooooood;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Telephony;
import android.util.Log;
import android.view.View;
import android.widget.Button;



import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.parse.Parse;
import com.parse.ParseACL;
import com.parse.ParseException;

import android.widget.TextView;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


/**
 * Created by ridhisurana on 9/12/15.
 */



public class MainActivity extends Activity {

    private TextView info;
    private LoginButton loginButton;
    public CallbackManager callbackManager = CallbackManager.Factory.create();


    public static final String TAG = "MainActivity";

    private ParseUser user;

    private Boolean firstTime = false;

    private SessionIdentifierGenerator randomName;

    private PendingIntent pendingIntent;

    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());

        setContentView(R.layout.facebook_signin);
        info = (TextView) findViewById(R.id.info);
        loginButton = (LoginButton) findViewById(R.id.login_button);

        Button graphButton = (Button) findViewById(R.id.graphbutton);
        graphButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, MoodSwingGraph.class);
                startActivity(intent);
            }
        });
        info = (TextView) findViewById(R.id.info);
        loginButton = (LoginButton) findViewById(R.id.login_button);

        SharedPreferences prefs = getSharedPreferences("Mood", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        if(!prefs.getBoolean("first", false)) {
            Utils.setAlarm(this);
            editor.putBoolean("first", true);
            editor.commit();
        }


        firstTime = true;

        user = ParseUser.getCurrentUser();
        if (user == null) {
            user = new ParseUser();

            randomName = new SessionIdentifierGenerator();
            user.setUsername(randomName.nextSessionId());
            user.setPassword(randomName.nextSessionId());
            try {
                user.signUp();
                user.setACL(new ParseACL(user));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        try {
            user.save();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        loginButton.setReadPermissions(Arrays.asList("user_posts"));
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {

            }

            @Override
            public void onCancel() {
                info.setText("Login attempt canceled.");

            }


            @Override
            public void onError(FacebookException e) {
                info.setText("Login attempt failed.");

            }
        });
    }

    @Override
        protected void onActivityResult(int requestCode, int resultCode, Intent data) {
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }
}
