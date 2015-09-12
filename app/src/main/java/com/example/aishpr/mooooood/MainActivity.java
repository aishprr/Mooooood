package com.example.aishpr.mooooood;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Telephony;
import android.text.TextUtils;
import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ridhisurana on 9/12/15.
 */
public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        List<MyMsg> texts = getMsgList();
        for (MyMsg text : texts) {
            Log.d("MSG", text.timeStamp + "" + text.body);
        }
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
        for(cursor.moveToFirst() ; !cursor.isAfterLast(); cursor.moveToNext()) {
            MyMsg msg = new MyMsg();
            msg.timeStamp = cursor.getLong(cursor.getColumnIndexOrThrow("date"));
            String body = cursor.getString(cursor.getColumnIndexOrThrow("body"));
            try {
                byte[] bytes = body.getBytes("UTF8");
                msg.body = TextUtils.htmlEncode(new String(bytes, "UTF8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            msgList.add(msg);

        }

        return msgList;
    }
}
