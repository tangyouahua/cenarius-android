package com.m.cenarius.widget;

import android.webkit.WebView;

import com.m.cenarius.activity.CNRSViewActivity;
import com.m.cenarius.utils.GsonHelper;
import com.m.cenarius.view.CenariusWidget;

import java.util.HashMap;

public class NativeWidget implements CenariusWidget {

    @Override
    public String getPath() {
        return "/widget/native";
    }

    @Override
    public boolean handle(WebView view, String url) {
        HashMap dataMap = GsonHelper.getDataMap(url, getPath());
        if (dataMap != null){
            if (null != view && view.getContext() instanceof CNRSViewActivity) {
                String className = (String) dataMap.get("className");
                ((CNRSViewActivity) view.getContext()).openNativePage(className, dataMap);
            }
            return true;
        }
        return false;
    }
}
