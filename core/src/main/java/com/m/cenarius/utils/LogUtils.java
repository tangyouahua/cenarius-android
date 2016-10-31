package com.m.cenarius.utils;

import android.util.Log;

import com.m.cenarius.Cenarius;

public class LogUtils {

    public static void i(String subTag, String message) {
        if (Cenarius.DEBUG) {
            Log.i(Cenarius.TAG, String.format("[%1$s] %2$s", subTag, message));
        }
    }

    public static void e(String subTag, String message) {
        if (Cenarius.DEBUG) {
            Log.e(Cenarius.TAG, String.format("[%1$s] %2$s", subTag, message));
        }
    }
}
