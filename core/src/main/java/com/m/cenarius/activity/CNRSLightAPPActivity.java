package com.m.cenarius.activity;

import android.os.Bundle;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.m.cenarius.R;
import com.m.cenarius.view.CenariusWebChromeClient;
import com.m.cenarius.view.CenariusWebViewClient;
import com.m.cenarius.view.WebViewSettings;

public class CNRSLightAPPActivity extends AppCompatActivity implements View.OnClickListener{

    private WebView webView;
    private TextView titleView;
    private RelativeLayout back;
    private TextView closeTv;
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
        closeTv = (TextView) findViewById(R.id.closeTv);
        refresh = (TextView) findViewById(R.id.refresh);
        back = (RelativeLayout) findViewById(R.id.relate_back);
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
                 titleView.setText(title + "");
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);   //在当前的webview中跳转到新的url
                return super.shouldOverrideUrlLoading(view, url);
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
                titleView.setText(title + "");
            }
        });


        webView.loadUrl(url);
    }

    @Override
    public void onPause() {
        super.onPause();
        webView.onPause();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK){
            back();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public void back(){
        if (webView.canGoBack()) {
            webView.goBack();
            closeTv.setVisibility(View.VISIBLE);
        }else{
            finish();
        }
    }

    @Override
    public void onClick(View v) {
        if (v == back){
           back();
        }
        else if (v == closeTv)
        {
            finish();
        }
        else if (v == refresh) {
            webView.reload();
        }
    }
}
