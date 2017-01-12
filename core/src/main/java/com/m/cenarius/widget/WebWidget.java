package com.m.cenarius.widget;

import android.text.TextUtils;
import android.view.View;

import com.m.cenarius.activity.CNRSViewActivity;
import com.m.cenarius.utils.GsonHelper;
import com.m.cenarius.view.CenariusWidget;

import org.w3c.dom.Text;

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
                String htmlFileURL = (String) dataMap.get("url");
                if(!TextUtils.isEmpty(htmlFileURL)){
                    ((CNRSViewActivity) view.getContext()).openLightApp(htmlFileURL, dataMap);
                }else{
                    String uri = (String) dataMap.get("uri");
                    if (!TextUtils.isEmpty(uri)){
                        ((CNRSViewActivity) view.getContext()).openWebPage(uri, dataMap);
                    }
                }
            }
            return true;
        }
        return false;
    }
}
