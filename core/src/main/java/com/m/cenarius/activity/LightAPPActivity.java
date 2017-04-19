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
import com.m.cenarius.view.CenariusWebChromeClient;
import com.m.cenarius.view.CenariusWebView;
import com.m.cenarius.view.CenariusWebViewClient;

public class LightAPPActivity extends CNRSWebViewActivity implements View.OnClickListener{

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
        cenariusWebView = (CenariusWebView) findViewById(R.id.webView);
        bar = (ProgressBar) findViewById(R.id.progressBar);
        bar.setProgress(0);
        titleView.setText(title + "");

//        WebSettings webSettings = webviewddd.getSettings();
//        webSettings.setJavaScriptEnabled(true);
//        // 设置可以访问文件
//        webSettings.setAllowFileAccess(true);
//        // 设置可以支持缩放
//        webSettings.setSupportZoom(true);
//        // 设置默认缩放方式尺寸是far
//        webSettings.setDefaultZoom(WebSettings.ZoomDensity.MEDIUM);
//        // 设置出现缩放工具
//        webSettings.setBuiltInZoomControls(false);
//        webSettings.setDefaultFontSize(20);
//        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
//        // 访问assets目录下的文件
//        webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
//        webSettings.setUseWideViewPort(true);
//        webSettings.setLoadWithOverviewMode(true);
//
//        // enable navigator.geolocation
//        webSettings.setGeolocationEnabled(true);
//        webSettings.setGeolocationDatabasePath("/data/data/" + this.getPackageName() + "/databases/");
//        // enable Web Storage: localStorage, sessionStorage
//        webSettings.setDomStorageEnabled(true);

        cenariusWebView.loadUrl(url);

        cenariusWebView.setWebViewClient(new CenariusWebViewClient(cenariusWebView.getWebView()) {
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
        cenariusWebView.setWebChromeClient(new CenariusWebChromeClient() {
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
        cenariusWebView.onPause();
    }

    @Override
    public void onClick(View v) {
        if (v == back){
            if (cenariusWebView.canGoBack()) {
                cenariusWebView.goBack();
            }
        }
        else if (v == x)
        {
            finish();
        }
        else if (v == refresh) {
            cenariusWebView.reload();
        }
    }
}
