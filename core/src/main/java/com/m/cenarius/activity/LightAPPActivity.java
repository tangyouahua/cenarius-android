package com.m.cenarius.activity;

import android.os.Bundle;
import android.content.Intent;
import android.graphics.Bitmap;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.m.cenarius.R;

public class LightAPPActivity extends CNRSViewActivity implements View.OnClickListener{

    private WebView webviewddd;
    private TextView titleView;
    private TextView back;
    private TextView x;
    private TextView refresh;
    private ProgressBar bar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_light_app);

        Intent intent = getIntent();
        String title = intent.getStringExtra("title");
        String url = intent.getStringExtra("url");
//        loadUrl(url);
        titleView = (TextView) findViewById(R.id.textView);
        x = (TextView) findViewById(R.id.xx);
        refresh = (TextView) findViewById(R.id.refresh);
        back = (TextView) findViewById(R.id.back);
        x.setOnClickListener(this);
        refresh.setOnClickListener(this);
        back.setOnClickListener(this);
        webviewddd = (WebView) findViewById(R.id.webviewddd);
        bar = (ProgressBar) findViewById(R.id.progressBar);
        bar.setProgress(0);
        titleView.setText(title + "");

        WebSettings webSettings = webviewddd.getSettings();
        webSettings.setJavaScriptEnabled(true);
        // 设置可以访问文件
        webSettings.setAllowFileAccess(true);
        // 设置可以支持缩放
        webSettings.setSupportZoom(true);
        // 设置默认缩放方式尺寸是far
        webSettings.setDefaultZoom(WebSettings.ZoomDensity.MEDIUM);
        // 设置出现缩放工具
        webSettings.setBuiltInZoomControls(false);
        webSettings.setDefaultFontSize(20);
        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        // 访问assets目录下的文件
        webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        // webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NARROW_COLUMNS);
        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);
        // webSettings.setSavePassword(true);
        // webSettings.setSaveFormData(true);

        // enable navigator.geolocation
        webSettings.setGeolocationEnabled(true);
        webSettings.setGeolocationDatabasePath("/data/data/" + this.getPackageName() + "/databases/");
        // enable Web Storage: localStorage, sessionStorage
        webSettings.setDomStorageEnabled(true);
        webviewddd.loadUrl(url);

        webviewddd.setWebViewClient(new WebViewClient() {
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
                if (title == null) {
                    title = "";
                }
                // titleView.setText(title + "");
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);   //在当前的webview中跳转到新的url
                return super.shouldOverrideUrlLoading(view, url);
            }
        });
        webviewddd.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                bar.setProgress(newProgress);
            }

//            @Override
//            public void onReceivedTitle(WebView view, String title) {
//                super.onReceivedTitle(view, title);
//                titleView.setText(title + "");
//            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        webviewddd.onPause();
    }

    @Override
    public void onClick(View v) {
        if (v == back){
            if (webviewddd.canGoBack()) {
                webviewddd.goBack();
            }
        }
        else if (v == x)
        {
            finish();
        }
        else if (v == refresh) {
            webviewddd.reload();
        }
    }
}
