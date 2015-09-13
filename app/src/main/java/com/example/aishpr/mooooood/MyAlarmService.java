package com.example.aishpr.mooooood;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.IBinder;
import android.provider.Telephony;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.parse.ParseACL;
import com.parse.ParseException;
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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class MyAlarmService extends Service {

    public static final String TAG = "MyAlarmService";

    public static final OkHttpClient CLIENT = new OkHttpClient();

    public ParseUser user;

    @Override
    public IBinder onBind(Intent arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();
        user = ParseUser.getCurrentUser();
        Toast.makeText(this, "Started!", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "Alarmed");
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int returnVal = super.onStartCommand(intent, flags, startId);

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                facebookStatusList();
                List<MyMsg> texts = getMsgList();

                for (MyMsg text : texts) {

                    Log.d("MSG", text.timeStamp + "" + text.body);
                }

            }
        });
        thread.start();
        return returnVal;

    }

    public void facebookStatusList() {
        FacebookSdk.sdkInitialize(getApplicationContext());
        GraphRequest getFBStatus = new GraphRequest(
                AccessToken.getCurrentAccessToken(),
                "/me/posts",
                null,
                HttpMethod.GET,
                new GraphRequest.Callback() {
                    public void onCompleted(GraphResponse response) {
                        try {
                            JSONObject allData = response.getJSONObject();
                            JSONArray data = allData.getJSONArray("data");
                            for (int i = 0; i < data.length(); i++) {
                                JSONObject post = data.getJSONObject(i);

                                if (post.has("message")) {
                                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ssZZZZ");
                                    Date date = formatter.parse(post.getString("created_time"));
                                    long epoch = System.currentTimeMillis();

                                    if (epoch - date.getTime() < 3600000) {
                                        Log.d(TAG, post.getString("message"));
                                        Log.d(TAG, date.getTime() + " ");

                                        //put in the db
                                        String query = null;
                                        try {
                                            query = String.format("message=%s",
                                                    URLEncoder.encode(post.getString("message"), "UTF-8")
                                            );
                                        } catch (UnsupportedEncodingException e) {
                                            throw new RuntimeException(e);
                                        }
                                        String feeling = null;
                                        try {
                                            Request request = new Request.Builder()
                                                    .get()
                                                    .url("https://www.wolframcloud.com/objects/1231d8f1-eec5-46ad-810d-697662e465cd?" + query)
                                                    .build();
                                            feeling = CLIENT.newCall(request).execute().body().string();



                                            ParseObject message_store = new ParseObject("MoodMessage");
                                            //ParseObject Sad = new ParseObject("Sad");

                                            message_store.put("timestamp", date.getTime());
                                            message_store.put("message", post.getString("message"));
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
                                } else {
                                    continue;
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        } catch (java.text.ParseException e) {
                            e.printStackTrace();
                        }
                    }
                }
        );
    }

    public List getMsgList() {
        Uri uri = Telephony.Sms.Sent.CONTENT_URI;
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        List<MyMsg> msgList = new ArrayList<MyMsg>();

        if (cursor.getCount() == 0) {
            Log.d(TAG, "empty cursor");
        }
        while (cursor.moveToNext()) {
            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                MyMsg msg = new MyMsg();
                msg.timeStamp = cursor.getLong(cursor.getColumnIndexOrThrow("date"));

                long epoch = System.currentTimeMillis();

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
                            .url("https://www.wolframcloud.com/objects/1231d8f1-eec5-46ad-810d-697662e465cd?" + query)
                            .build();
                    feeling = CLIENT.newCall(request).execute().body().string();


                    if ((epoch - msg.timeStamp) < 3600000) {
                        Log.d(TAG, "MSGPOI:" + msg.body + " " + msg.timeStamp);
                        msgList.add(msg);
                        ParseObject message_store = new ParseObject("MoodMessage");

                        message_store.put("timestamp", msg.timeStamp);
                        message_store.put("message", msg.body);
                        message_store.put("feelz", feeling);
                        message_store.setACL(new ParseACL(user));
                        try {
                            message_store.save();
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }


                } catch (IOException e) {
                    Log.e(TAG, "Request failed", e);
                }
            }
        }
        return msgList;
    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
    }

}