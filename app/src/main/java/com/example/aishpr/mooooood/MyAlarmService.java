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
import java.util.ArrayList;
import java.util.List;


public class MyAlarmService extends Service
{

    //private NotificationManager mManager;


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
    public IBinder onBind(Intent arg0)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void onCreate()
    {
        // TODO Auto-generated method stub
        super.onCreate();
    }



    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        int returnVal = super.onStartCommand(intent, flags , startId);

        List<MyMsg> texts = getMsgList();
        for (MyMsg text : texts) {
            Log.d("MSG", text.timeStamp + "" + text.body);
        }
        return returnVal;

    }

    public List getMsgList() {
        Uri uri = Telephony.Sms.Sent.CONTENT_URI;
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        List<MyMsg> msgList = new ArrayList<MyMsg>();
        /*cursor.moveToFirst();
        List<MyMsg> msgList = new ArrayList<MyMsg>();
        MyMsg msg = new MyMsg();
        msg.timeStamp = cursor.getLong(cursor.getColumnIndexOrThrow("timeStamp"));
        String body = cursor.getString(cursor.getColumnIndexOrThrow("body"));
        try {
            byte[] bytes = body.getBytes("UTF8");
            msg.body = TextUtils.htmlEncode(new String(bytes, "UTF8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        msgList.add(msg);*/
        while (cursor.moveToNext()) {
//        for(cursor.moveToFirst() ; !cursor.isAfterLast(); cursor.moveToNext()) {
            MyMsg msg = new MyMsg();
            msg.timeStamp = cursor.getLong(cursor.getColumnIndexOrThrow("date"));
            String body = cursor.getString(cursor.getColumnIndexOrThrow("body"));
            String feeling = "";
            InputStream in;
            try {
                byte[] bytes = body.getBytes("UTF8");
                msg.body = TextUtils.htmlEncode(new String(bytes, "UTF8"));

                String url = "https://www.wolframcloud.com/objects/f5f81803-d910-499b-9d50-15c7d801e0e5";
                String charset = "UTF-8";  // Or in Java 7 and later, use the constant: java.nio.charset.StandardCharsets.UTF_8.name()
                String param1 = msg.body;

                String query = String.format("message=%s",
                        URLEncoder.encode(param1, charset)
                );

                URLConnection connection = new URL(url + "?" + query).openConnection();
                connection.setRequestProperty("Accept-Charset", charset);
                in = new BufferedInputStream(connection.getInputStream());
                readStream(in);
                feeling = getStringFromInputStream(in);
                in.close();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            Log.d("FEELZ", feeling);
            msgList.add(msg);

        }

        return msgList;
    }
    @Override
    public void onDestroy()
    {
        // TODO Auto-generated method stub
        super.onDestroy();
    }

}