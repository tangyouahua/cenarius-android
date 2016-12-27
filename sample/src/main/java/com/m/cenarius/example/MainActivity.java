package com.m.cenarius.example;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.m.cenarius.activity.CNRSViewActivity;
import com.m.cenarius.utils.QueryUtil;
import com.m.cenarius.utils.XutilsInterceptor;
import com.m.cenarius.widget.LoginWidget;

import org.xutils.http.HttpMethod;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.util.HashMap;
import java.util.Map;

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

//    public void login(View view){
//        LoginWidget.login("337304000", "123444", new LoginWidget.LoginCallback() {
//            @Override
//            public void onSuccess(String accessToken) {
//
//            }
//
//            @Override
//            public void onFail(String errorMessage) {
//
//            }
//        });
//    }

    public void openFragment(View view){
        Intent intent = new Intent(this, FragmentActivity.class);
        intent.putExtra("htmlFileURL", "https://www.baidu.com/");
        startActivity(intent);
    }

    public void openApi(View view){
        final String url = "https://www.baidu.com?A=B";

        new Thread(new Runnable() {
            @Override
            public void run() {
                // 由于 xutils 不能自动从 url？ 后面取出参数，这里手动取出
                RequestParams requestParams = new RequestParams(QueryUtil.baseUrlFromUrl(url));
                QueryUtil.addQueryForRequestParams(requestParams, url);

                // 业务代码
                requestParams.addHeader("X-Requested-With","OpenAPIRequest");
                requestParams.addBodyParameter("BodyParameter","BodyParameter");
                requestParams.setAsJsonContent(true);
                Map m = new HashMap();
                m.put("JSON","JSON");
                requestParams.setBodyContent(JSON.toJSONString(m));

//                // 最后设置 OpenApi 拦截器
//                XutilsInterceptor.openApiForRequestParams(requestParams);


                try {
                    x.http().requestSync(HttpMethod.POST, requestParams, byte[].class);
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
            }
        }).start();
    }

}
