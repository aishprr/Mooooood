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

import com.parse.ParseObject;
import com.parse.ParseQuery;
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

    private Boolean firstTime = false;

    private PendingIntent pendingIntent;

    public MainActivity() {

        firstTime = true;

    }

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

            } catch (IOException e) {
                Log.e(TAG, "Request failed", e);
            }
        }
        return msgList;
    }

    private static String getStringFromInputStream(InputStream is) {

        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();

        String line;
        try {

            br = new BufferedReader(new InputStreamReader(is));
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return sb.toString();

    }
    private String readStream(InputStream is) {
        try {
            ByteArrayOutputStream bo = new ByteArrayOutputStream();
            int i = is.read();
            while(i != -1) {
                bo.write(i);
                i = is.read();
            }
            return bo.toString();
        } catch (IOException e) {
            return "";
        }
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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

        ParseObject Happy = new ParseObject("Happy");

        Happy.put("Media", "text");
        Happy.put("Time Stamp", "8:12");
        ParseObject Sad = new ParseObject("Sad");
        Sad.put("Media", "facebook status");
        Sad.put("Time Stamp", "8:12");
        Happy.saveInBackground();
        Sad.saveInBackground();
        ParseQuery SadQuery = new ParseQuery("SadMedia");

    }


}
