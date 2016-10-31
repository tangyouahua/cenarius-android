package com.m.cenarius.widget;

import android.app.Activity;
import android.net.Uri;
import android.text.TextUtils;
import android.webkit.WebView;

import com.google.gson.reflect.TypeToken;
import com.m.cenarius.utils.GsonHelper;
import com.m.cenarius.view.CenariusWidget;

import java.util.HashMap;

public class TitleWidget implements CenariusWidget {

    static final String KEY_TITLE = "title";

    @Override
    public String getPath() {
        return "/widget/nav_title";
    }

    @Override
    public boolean handle(WebView view, String url) {
        HashMap dataMap = GsonHelper.getDataMap(url, getPath());
        if (dataMap != null){
            if (null != view && view.getContext() instanceof Activity) {
                ((Activity) view.getContext()).setTitle((String) dataMap.get(KEY_TITLE));
            }
            return true;
        }
        return false;
    }
}
