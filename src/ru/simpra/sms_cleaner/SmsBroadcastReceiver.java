package ru.simpra.sms_cleaner;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ContentValues;
import android.os.Environment;
import android.provider.Telephony;
import android.telephony.SmsMessage;
import android.telephony.gsm.SmsManager;
import android.net.Uri;

import android.os.Bundle;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by Filipp on 08.01.2016.
 */
public class SmsBroadcastReceiver extends BroadcastReceiver {

    public static final String SMS_BUNDLE = "pdus";

    public void onReceive(Context context, Intent intent) {

        Bundle intentExtras = intent.getExtras();
        if (intentExtras != null) {
            Object[] sms = (Object[]) intentExtras.get(SMS_BUNDLE);
            String address = "";
            String smsBody = "";

            for (int i = 0; i < sms.length; ++i) {
                SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) sms[i]);
                smsBody = smsMessage.getMessageBody().toString();
                address = smsMessage.getOriginatingAddress();
            }

            ContentValues values = new ContentValues();
            values.put("address", address);
            values.put("body", smsBody);
            values.put("date", System.currentTimeMillis());
            values.put("type", "1");
            values.put("read", Integer.valueOf(0));
            values.put("status", Integer.valueOf(-1));
            values.put("service_center", "123456789");
            context.getContentResolver().insert(Uri.parse("content://sms/inbox"), values);
        }

    }
}