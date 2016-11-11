package com.m.cenarius.widget;

import android.net.Uri;
import android.text.TextUtils;
import android.view.View;
import android.webkit.WebView;

import com.m.cenarius.view.CenariusWebView;
import com.m.cenarius.view.CenariusWidget;

public class PullToRefreshWidget implements CenariusWidget {

    static final String KEY = "action";
    // 启用下拉刷新
    static final String ACTION_ENABLE = "enable";
    // 下拉刷新完成
    static final String ACTION_COMPLETE = "complete";

    @Override
    public String getPath() {
        return "/widget/pull_to_refresh";
    }

    @Override
    public boolean handle(View view, String url) {
        if (TextUtils.isEmpty(url)) {
            return false;
        }
        Uri uri = Uri.parse(url);
        String path = uri.getPath();
        if (TextUtils.equals(path, getPath())) {
            String action = uri.getQueryParameter(KEY);
            if (TextUtils.equals(action, ACTION_ENABLE)) {
                ((CenariusWebView)view.getParent().getParent()).enableRefresh(true);
            } else if (TextUtils.equals(action, ACTION_COMPLETE)) {
                ((CenariusWebView)view.getParent().getParent()).setRefreshing(false);
            }
            return true;
        }
        return false;
    }
}
