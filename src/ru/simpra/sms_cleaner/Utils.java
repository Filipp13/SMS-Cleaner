package ru.simpra.sms_cleaner;

import android.os.Build;

/**
 * Created by Filipp on 14.07.2016.
 */
public class Utils {
    public static boolean hasKitKat() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
    }
}
