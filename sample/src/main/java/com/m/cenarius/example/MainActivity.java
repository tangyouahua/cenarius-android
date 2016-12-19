package com.m.cenarius.example;

import android.content.Intent;
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
        openWebPage("build/index.html", null);
    }

    public void openCordova(View view){
        openCordovaPage("sign/sign.html", null);
    }

    public void login(View view){
        LoginWidget.login("337304000", "123444", new LoginWidget.LoginCallback() {
            @Override
            public void onSuccess(String accessToken) {

            }

            @Override
            public void onFail(String errorMessage) {

            }
        });
    }

    public void openFragment(View view){
        Intent intent = new Intent(this, FragmentActivity.class);
        intent.putExtra("uri", "build/index.html");
        startActivity(intent);
    }
}
