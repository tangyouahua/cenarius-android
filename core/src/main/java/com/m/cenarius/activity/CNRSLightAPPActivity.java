package com.m.cenarius.activity;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.GeolocationPermissions;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.m.cenarius.R;
import com.m.cenarius.event.ReStartEvent;
import com.m.cenarius.utils.Utils;
import com.m.cenarius.view.CenariusWebChromeClient;
import com.m.cenarius.view.CenariusWebViewClient;
import com.m.cenarius.view.WebViewSettings;
import com.m.cenarius.widget.ToastWidget;

import org.apache.cordova.engine.SystemWebView;
import org.crosswalk.engine.XWalkCordovaView;
import org.greenrobot.eventbus.EventBus;
import org.xutils.common.util.LogUtil;
import org.xwalk.core.XWalkView;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.regex.Pattern;

public class CNRSLightAPPActivity extends AppCompatActivity implements View.OnClickListener {

    private WebView webView;
    private TextView titleView;
    private RelativeLayout back;
    private TextView closeTv;
    private TextView refresh;
    private ProgressBar bar;
    private TextView closeBtn;

    /**
     * 是否己经执行过一次返加
     */
    private boolean isDoneReturn;

    private String origin;

    private GeolocationPermissions.Callback callback;


    /**
     * 定位权限请求码
     */
    public static final  int REQUEST_CODE_LOCATION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_light_app);

        Intent intent = getIntent();
        String title = intent.getStringExtra("title");
        String url = intent.getStringExtra("url");
//        loadUrl(url);
        titleView = (TextView) findViewById(R.id.textView);
        closeTv = (TextView) findViewById(R.id.close_tv);
        refresh = (TextView) findViewById(R.id.refresh);
        back = (RelativeLayout) findViewById(R.id.relate_back);
        closeBtn = (TextView)findViewById(R.id.close_btn);
        closeBtn.setOnClickListener(this);
        closeTv.setOnClickListener(this);
        refresh.setOnClickListener(this);
        back.setOnClickListener(this);
        webView = (WebView) findViewById(R.id.webView);
        bar = (ProgressBar) findViewById(R.id.progressBar);
        bar.setProgress(0);
        if (TextUtils.isEmpty(title)) {
            title = "";
        }
        titleView.setText(title + "");


        WebViewSettings.setupWebSettings(webView.getSettings());
        webView.setWebViewClient(new CenariusWebViewClient(webView) {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                bar.setVisibility(View.VISIBLE);
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                bar.setVisibility(View.GONE);
                bar.setProgress(0);
                super.onPageFinished(view, url);
                String title = view.getTitle();
                if (TextUtils.isEmpty(title)) {
                    title = "";
                }

                if(!title.toLowerCase().startsWith("http")){
                    titleView.setText(title + "");
                }

                if (isDoneReturn && webView.canGoBack()) {
                    setCloseBtnVisibility(View.VISIBLE);
                    if(!title.toLowerCase().startsWith("http")){
                        titleView.setText(title + "");
                    }
                    if (isDoneReturn && webView.canGoBack()) {
                        setCloseBtnVisibility(View.VISIBLE);
                    } else {
                        setCloseBtnVisibility(View.GONE);
                    }
                }
            }
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);   //在当前的webview中跳转到新的url
                return super.shouldOverrideUrlLoading(view, url);
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
                if (Utils.hasM()) {
                    if (error.getErrorCode() == -2) {
                        ToastWidget.showToast(view.getContext(), "网络请求失败，请检查您的网络");
                    }
                }
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);
                if (errorCode == -2) {
                    ToastWidget.showToast(view.getContext(), "网络请求失败，请检查您的网络");
                }
            }
        });
        webView.setWebChromeClient(new CenariusWebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                bar.setProgress(newProgress);
            }

            @Override
            public void onReceivedTitle(WebView view, String title) {
                super.onReceivedTitle(view, title);
                if(TextUtils.isEmpty(title)){
                    title = "";
                }
                if(!title.toLowerCase().startsWith("http")){
                    titleView.setText(title + "");
                }
            }


            @Override
            public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
                if (Build.VERSION.SDK_INT >= 23) {
                    int checkPermission = ContextCompat.checkSelfPermission(CNRSLightAPPActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION);
                    int checkPermission2 = ContextCompat.checkSelfPermission(CNRSLightAPPActivity.this, Manifest.permission.ACCESS_FINE_LOCATION);
                    if (checkPermission != PackageManager.PERMISSION_GRANTED || checkPermission2 != PackageManager.PERMISSION_GRANTED ) {
                        CNRSLightAPPActivity.this.origin = origin;
                        CNRSLightAPPActivity.this.callback = callback;
                        ActivityCompat.requestPermissions(CNRSLightAPPActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION},REQUEST_CODE_LOCATION);
                    } else{
                        super.onGeolocationPermissionsShowPrompt(origin, callback);
                    }
                }else{
                    super.onGeolocationPermissionsShowPrompt(origin, callback);
                }

            }
        });


        webView.loadUrl(url);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == REQUEST_CODE_LOCATION){
            if(grantResults != null &&grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                   if(callback != null){
                       callback.invoke(origin, true, false);
                   }

            }
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        EventBus.getDefault().post(new ReStartEvent());
    }

    @Override
    public void onPause() {
        super.onPause();
        webView.onPause();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            back();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public void back() {
        isDoneReturn = true;
        if (webView.canGoBack()) {
            webView.goBack();
//            closeTv.setVisibility(View.VISIBLE);
        } else {
            finish();
        }
    }

    @Override
    public void onClick(View v) {
        if (v == back) {
            back();
        } else if (v == closeTv) {
            finish();
        } else if (v == refresh) {
            webView.reload();
        }else if (v == closeBtn){
            finish();
        }
    }
    /**
     * 根据WebView是否有goBack记录设置左上角的叉按钮
     * @param visibility
     */
    public void setCloseBtnVisibility(int visibility) {
        if (closeBtn != null && closeBtn.getVisibility() != visibility) {
            closeBtn.setVisibility(visibility);
        }
    }

    @Override
    public Resources getResources() {
        Resources res = super.getResources();
        Configuration config = new Configuration();
        config.setToDefaults();
        config.fontScale = 1.0f;
        res.updateConfiguration(config,res.getDisplayMetrics());
        return res;

    }

}
