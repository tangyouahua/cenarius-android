package com.m.cenarius.widget;

import android.content.Context;
import android.view.View;
import android.widget.Toast;

import com.m.cenarius.utils.GsonHelper;
import com.m.cenarius.utils.LogUtils;
import com.m.cenarius.view.CenariusWidget;

import java.util.HashMap;

public class ToastWidget implements CenariusWidget {

    static final String TAG = ToastWidget.class.getSimpleName();

    static final String KEY_MESSAGE = "message";
    static final String KEY_LEVEL = "level";

    @Override
    public String getPath() {
        return "/widget/toast";
    }

    @Override
    public boolean handle(View view, String url) {
        HashMap dataMap = GsonHelper.getDataMap(url, getPath());
        if (dataMap != null) {
            String message = (String) dataMap.get(KEY_MESSAGE);
            String level = (String) dataMap.get(KEY_LEVEL);
            showToast(view.getContext(), message);
            LogUtils.i(TAG, String.format("handle toast success, message = %1$s ", message));
            return true;
        }
        return false;
    }

    public static void showToast(Context context, String text) {
        Toast.makeText(context, text, Toast.LENGTH_LONG).show();
    }

}
