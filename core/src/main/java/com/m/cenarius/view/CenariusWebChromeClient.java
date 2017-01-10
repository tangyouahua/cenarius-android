package com.m.cenarius.view;

import android.app.Activity;
import android.net.Uri;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.GeolocationPermissions;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.ProgressBar;

import com.m.cenarius.utils.LogUtils;

import org.xutils.common.util.LogUtil;

import java.util.regex.Matcher;

import static com.m.cenarius.R.id.progressBar;

public class CenariusWebChromeClient extends WebChromeClient{
    private ProgressBar progressBar;
    private boolean isShowOver = false;
    static final String TAG = CenariusWebChromeClient.class.getSimpleName();

    public void setProgressBar(ProgressBar progressBar) {
        this.progressBar = progressBar;
    }
    @Override
    public void onReceivedTitle(WebView view, String title) {
        super.onReceivedTitle(view, title);
        if (TextUtils.isEmpty(title)) {
            return;
        }
        Matcher matcher = Patterns.WEB_URL.matcher(title);
        if (matcher.matches()) {
            return;
        }
        // 部分系统会优先用页面的location作为title，这种情况需要过滤掉
        if (Patterns.WEB_URL.matcher(title).matches()) {
            return;
        }
        // Hack：过滤掉cenarius页面
        if (title.contains(".html?uri=")) {
            return;
        }
        // 设置title
        if (view.getContext() instanceof Activity) {
            ((Activity) view.getContext()).setTitle(Uri.decode(title));
        }
    }

    @Override
    public void onGeolocationPermissionsShowPrompt(String origin,
                                                   GeolocationPermissions.Callback callback) {
        callback.invoke(origin, true, false);
    }

    @Override
    public void onProgressChanged(WebView view, int newProgress) {
        LogUtil.v("进度条加载： "+ newProgress);
        if(progressBar == null || isShowOver == true){
            return;
        }
        if (newProgress == 100) {
            isShowOver = true;
            progressBar.setProgress(100);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    progressBar.setVisibility(View.GONE);//加载完网页进度条消失
                }
            }, 800);//0.2秒后隐藏进度条
        } else {
            progressBar.setVisibility(View.VISIBLE);//开始加载网页时显示进度条
            progressBar.setProgress(newProgress);//设置进度值
        }
    }

    @Override
    public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
        LogUtils.i(TAG, consoleMessage.message());
        return super.onConsoleMessage(consoleMessage);
    }

    @Override
    public void onConsoleMessage(String message, int lineNumber, String sourceID) {
        super.onConsoleMessage(message, lineNumber, sourceID);
        LogUtils.i(TAG, message);
    }
}
