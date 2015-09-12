package com.example.aishpr.mooooood;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Telephony;
import android.text.TextUtils;
import android.util.Log;

import com.parse.Parse;
import com.parse.ParseACL;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SignUpCallback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.net.URL;



/**
 * Created by ridhisurana on 9/12/15.
 */
public class MainActivity extends Activity {
    public static final String TAG = "MainActivity";

    public static final OkHttpClient CLIENT = new OkHttpClient();

    private ParseUser user;


    private Boolean firstTime = false;

    private SessionIdentifierGenerator randomName;


    private PendingIntent pendingIntent;

    public List getMsgList() {
        Uri uri = Telephony.Sms.Sent.CONTENT_URI;
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        List<MyMsg> msgList = new ArrayList<MyMsg>();

        if (cursor.getCount() == 0) {
            Log.d(TAG, "empty cursor");
        }
        while (cursor.moveToNext()) {
//        for(cursor.moveToFirst() ; !cursor.isAfterLast(); cursor.moveToNext()) {
            MyMsg msg = new MyMsg();
            msg.timeStamp = cursor.getLong(cursor.getColumnIndexOrThrow("date"));

            long presentUnixTime = System.currentTimeMillis() / 1000L;

            if ((presentUnixTime - 3600) < msg.timeStamp) {

                msg.body = cursor.getString(cursor.getColumnIndexOrThrow("body"));
                String query = null;
                try {
                    query = String.format("message=%s",
                            URLEncoder.encode(msg.body, "UTF-8")
                    );
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException(e);
                }
                String feeling = null;
                try {
                    Request request = new Request.Builder()
                            .get()
                            .url("https://www.wolframcloud.com/objects/f5f81803-d910-499b-9d50-15c7d801e0e5?" + query)
                            .build();
                    feeling = CLIENT.newCall(request).execute().body().string();
                    msgList.add(msg);
                    Log.d(TAG,"MSG:" + msg.body + " " + msg.timeStamp);
                    Log.d(TAG, "Feeling: " + feeling);
                    //PUsh to parsesesese

                    ParseObject message_store = new ParseObject("MoodMessage");
                    //ParseObject Sad = new ParseObject("Sad");

                    message_store.put("timestamp", msg.timeStamp);
                    message_store.put("message", msg.body);
                    message_store.put("feelz", feeling);
                    message_store.setACL(new ParseACL(user));
                    try {
                        message_store.save();
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                } catch (IOException e) {
                    Log.e(TAG, "Request failed", e);
                }
            }
        }
        return msgList;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Parse.enableLocalDatastore(this);

        Parse.initialize(this, "rFMfBygvKgsLkhk3x63sXwBkcfRHPKfzv7Ylu8eO", "qRYoszUqc3inmdMljMy0EPGCjbOkGfZlaGafUUXU");

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

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                getMsgList();
                return null;
            }
        }.execute();
        if (firstTime) {
            firstTime = false;

            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());
            calendar.set(Calendar.HOUR_OF_DAY, 14);


            Intent myIntent = new Intent(MainActivity.this, MyTextReceiver.class);

            pendingIntent = PendingIntent.getBroadcast(MainActivity.this, 0, myIntent, 0);

            AlarmManager alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);
            // With setInexactRepeating(), you have to use one of the AlarmManager interval
            // constants--in this case, AlarmManager.INTERVAL_DAY.
            alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                    AlarmManager.INTERVAL_HOUR, pendingIntent);
            //alarmManager.set(AlarmManager.RTC, calendar.getTimeInMillis(), pendingIntent);

        }


    }


}
