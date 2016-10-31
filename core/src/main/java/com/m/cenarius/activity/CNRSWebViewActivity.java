package com.m.cenarius.activity;

import android.os.Bundle;

import com.m.cenarius.R;
import com.m.cenarius.view.CenariusWebView;
import com.m.cenarius.view.CenariusWidget;


public class CNRSWebViewActivity extends CNRSViewActivity {

    public static final String TAG = CNRSWebViewActivity.class.getSimpleName();

    public CenariusWebView mCenariusWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cnrs_web_view_activity);


        mCenariusWebView = (CenariusWebView) findViewById(R.id.webView);

        // add widget
        for(CenariusWidget widget: widgets){
            mCenariusWebView.addCenariusWidget(widget);
        }

        String htmlUrl = htmlURL();
        mCenariusWebView.loadUrl(htmlUrl);
    }

//    private void loadUrl(String url)
//    {
//        // load uri
//        if (htmlFileURL != null) {
//            mCenariusWebView.loadUrl(htmlFileURL);
//        } else if (uri != null) {
//            mCenariusWebView.loadUri(uri);
//        }
//    }

}
