package com.m.cenarius.example;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.m.cenarius.activity.CNRSViewActivity;
import com.m.cenarius.utils.QueryUtil;
import com.m.cenarius.utils.XutilsInterceptor;
import com.m.cenarius.widget.LoginWidget;

import org.xutils.common.Callback;
import org.xutils.http.HttpMethod;
import org.xutils.http.RequestParams;
import org.xutils.x;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends CNRSViewActivity {

    public static final String TAG = MainActivity.class.getSimpleName();
    private Bitmap captcha = null;

    public TextView mCenariusButton;
    public EditText editText;
    public ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        mCenariusButton = (TextView) findViewById(R.id.open_light_app);
        editText = (EditText) findViewById(R.id.editText);
        imageView = (ImageView)findViewById(R.id.imageView);

        // 请求验证码
        getCaptchaId();
    }

    public void openLight(View view) {
        openLightApp("https://wechat-dev.infinitus.com.cn/wechat-front/html5/build/magazineAddress/index.html?staffNum=220023706#modify", null);
    }

    public void openH5(View view){
        openWebPage("build/index.html", null);
    }

    public void openCordova(View view){
        openCordovaPage("build/index.html", null);
    }

    // 获取验证码随机码
    static String captchaId = "";
    public void getCaptchaId() {
        RequestParams requestParams = new RequestParams("https://uim-test.infinitus.com.cn/captchaid");
        x.http().get(requestParams, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                captchaId = result;
                getCaptcha();
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                System.out.println(ex);
            }

            @Override
            public void onCancelled(CancelledException cex) {
            }

            @Override
            public void onFinished() {
            }
        });
    }

    /**
     * 获取应用下的缓存目录
     *
     * @param context
     * @return
     */
    private static File getCacheDir(Context context) {
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED))
            return context.getExternalCacheDir();
        return context.getCacheDir();
    }

    // 获取bitmap
    private Bitmap showAuthCodeImage(String fileName) {
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inSampleSize = 2;
        try {
            Bitmap bmp = BitmapFactory.decodeFile(fileName, opts);
            return bmp;

        } catch (OutOfMemoryError err) {

        }
        return null;
    }

    // 或者验证码图片
    public void getCaptcha() {
        RequestParams requestParams = new RequestParams("https://uim-test.infinitus.com.cn/captcha");
        requestParams.addQueryStringParameter("id", captchaId);
        File cacheDir = getCacheDir(this);

        final String tempName = cacheDir.getAbsolutePath() +
                File.separator + captchaId + "authCode.temp";
        requestParams.setSaveFilePath(tempName);
        x.http().get(requestParams, new Callback.ProgressCallback<File>() {

            @Override
            public void onSuccess(File result) {
                captcha=showAuthCodeImage(result.toString());
                mHandler.sendEmptyMessage(0);
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {

            }

            @Override
            public void onCancelled(CancelledException cex) {

            }

            @Override
            public void onFinished() {

            }

            @Override
            public void onWaiting() {

            }

            @Override
            public void onStarted() {

            }

            @Override
            public void onLoading(long total, long current, boolean isDownloading) {

            }
        });
    }

    public void login(View view){
        LoginWidget.login("337304000", "123444", captchaId, editText.getText().toString(), new LoginWidget.LoginCallback() {
            @Override
            public void onSuccess(String accessToken) {

            }

            @Override
            public void onFail(String errorMessage) {

            }
        });
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            imageView.setImageBitmap(captcha);
        }
    };

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
