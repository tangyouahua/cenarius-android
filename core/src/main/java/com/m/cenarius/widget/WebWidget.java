package com.m.cenarius.widget;

import android.view.View;

import com.m.cenarius.activity.CNRSViewActivity;
import com.m.cenarius.utils.GsonHelper;
import com.m.cenarius.view.CenariusWidget;

import java.util.HashMap;

public class WebWidget implements CenariusWidget {

    @Override
    public String getPath() {
        return "/widget/web";
    }

    @Override
    public boolean handle(View view, String url) {
        HashMap dataMap = GsonHelper.getDataMap(url, getPath());
        if (dataMap != null){
            if (null != view && view.getContext() instanceof CNRSViewActivity) {
                String uri = (String) dataMap.get("uri");
                ((CNRSViewActivity) view.getContext()).openWebPage(uri, dataMap);
            }
            return true;
        }
        return false;
    }
}
