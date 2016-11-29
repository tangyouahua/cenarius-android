package com.m.cenarius.example;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.m.cenarius.activity.CNRSViewActivity;
import com.m.cenarius.widget.LoginWidget;

public class MainActivity extends CNRSViewActivity {

    public static final String TAG = MainActivity.class.getSimpleName();

    public TextView mCenariusButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        mCenariusButton = (TextView) findViewById(R.id.open_light_app);
    }

    public void openLight(View view) {
        openLightApp("https://www.baidu.com/", null);
    }

    public void openH5(View view){
        openWebPage("sign/sign.html", null);
    }

    public void openCordova(View view){
        openCordovaPage("sign/sign.html", null);
    }

    public void login(View view){
        login(this, "0009704", "Admin123", new LoginWidget.LoginCallback() {
            @Override
            public void onSuccess(String accessToken) {

            }

            @Override
            public void onFail(String errorMessage) {

            }
        });
    }
}
