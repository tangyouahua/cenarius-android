package com.m.cenarius.activity;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;

import com.m.cenarius.Cenarius;
import com.m.cenarius.R;
import com.m.cenarius.resourceproxy.cache.AssetCache;
import com.m.cenarius.utils.LogUtils;
import com.m.cenarius.view.CenariusWidget;
import com.m.cenarius.widget.AlertDialogWidget;
import com.m.cenarius.widget.CordovaWidget;
import com.m.cenarius.widget.NativeWidget;
import com.m.cenarius.widget.PullToRefreshWidget;
import com.m.cenarius.widget.TitleWidget;
import com.m.cenarius.widget.ToastWidget;
import com.m.cenarius.widget.WebWidget;
import com.m.cenarius.widget.menu.MenuItem;
import com.m.cenarius.widget.menu.MenuWidget;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * `CNRSViewActivity` 是一个 Cenarius Container。
 * 它提供了一个使用 web 技术 html, css, javascript 开发 UI 界面的容器。
 * 其他Activity都应该继承它
 */
public class CNRSViewActivity extends AppCompatActivity {

    public static final String TAG = CNRSViewActivity.class.getSimpleName();

    /**
     * 打开本地web应用
     *
     * @param uri        相对路径
     * @param parameters 参数
     */
    public void openWebPage(String uri, HashMap parameters) {
        Intent intent = new Intent(this, CNRSWebViewActivity.class);
        intent.putExtra("uri", uri);
        intent.putExtra("parameters", parameters);
        startActivity(intent);
    }

    /**
     * 打开轻应用
     *
     * @param htmlFileURL        网址
     * @param parameters 参数
     */
    public void openLightApp(String htmlFileURL, HashMap parameters) {
        Intent intent = new Intent(this, CNRSWebViewActivity.class);
        intent.putExtra("htmlFileURL", htmlFileURL);
        intent.putExtra("parameters", parameters);
        startActivity(intent);
    }

    /**
     * 打开原生页面
     *
     * @param className  类名
     * @param parameters 参数
     */
    public void openNativePage(String className, HashMap parameters) {
        String c = getPackageName();
//        try {
//            Object test = Class.forName(className).getInterfaces();
//            if (test instanceof CNRSViewActivity) {
//            }
//        } catch (ClassNotFoundException e) {
//            e.printStackTrace();
//        }
//
//
//        try {
//            CNRSViewActivity cnrsViewActivity = (CNRSViewActivity) Class.forName(className).newInstance();
//            if (cnrsViewActivity != null) {
//                Intent intent = new Intent(this, CNRSViewActivity.class);
//                intent.putExtra("parameters", parameters);
//                startActivity(intent);
//            }
//        } catch (InstantiationException e) {
//            e.printStackTrace();
//        } catch (IllegalAccessException e) {
//            e.printStackTrace();
//        } catch (ClassNotFoundException e) {
//            e.printStackTrace();
//        }
    }

    /**
     * 打开Cordova页面
     *
     * @param uri        相对路径
     * @param parameters 参数
     */
    public void openCordovaPage(String uri, HashMap parameters) {
        Intent intent = new Intent(this, CNRSCordovaActivity.class);
        intent.putExtra("uri", uri);
        intent.putExtra("parameters", parameters);
        startActivity(intent);
    }

//    public static void startActivity(Activity activity, String uri) {
//        Intent intent = new Intent(activity, CNRSViewActivity.class);
//        intent.setData(Uri.parse(uri));
//        activity.startActivity(intent);
//    }


    /**
     * 对应的 uri。
     */
    public String uri;

    /**
     * 对应的 url。
     */
    public String htmlFileURL;

    /**
     * 传参
     */
    public HashMap cnrsDictionary;

    public ArrayList<CenariusWidget> widgets = new ArrayList<>();

    private List<MenuItem> mMenuItems = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cnrs_view_activity);

        uri = getIntent().getStringExtra("uri");
        htmlFileURL = getIntent().getStringExtra("htmlFileURL");

        // Widgets
        TitleWidget titleWidget = new TitleWidget();
        AlertDialogWidget alertDialogWidget = new AlertDialogWidget();
        ToastWidget toastWidget = new ToastWidget();
//        PullToRefreshWidget pullToRefreshWidget = new PullToRefreshWidget();
        MenuWidget menuWidget = new MenuWidget();
        NativeWidget nativeWidget = new NativeWidget();
        WebWidget webWidget = new WebWidget();
        CordovaWidget cordovaWidget = new CordovaWidget();

        widgets.add(titleWidget);
        widgets.add(alertDialogWidget);
        widgets.add(toastWidget);
//        widgets.add(pullToRefreshWidget);
        widgets.add(menuWidget);
        widgets.add(nativeWidget);
        widgets.add(webWidget);
        widgets.add(cordovaWidget);
    }

    public String htmlURL() {
        //读取sd目录
        if (Cenarius.DevelopModeEnable)
        {
            return getSDFile(uri);
        }
        return cnrs_htmlURL(uri, htmlFileURL);
    }

    private String cnrs_htmlURL(String uri, String htmlFileURL) {
        if (htmlFileURL == null) {
            try {
                URL url = new URL(uri);
                if (url.getQuery().length() != 0 || url.getRef().length() != 0)
                {
                    LogUtils.i(TAG, "local html 's format is not right! Url has query and fragment.");
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
        return htmlFileURL;
    }

    // 获取sdcard目录
    private String getSDFile(String uri){
        if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
            File sdCardDir = Environment.getExternalStorageDirectory();//获取SDCard目录
            String applicationName = getApplicationName();
            File fileDir =  new File(sdCardDir,applicationName + "/" + AssetCache.getInstance().mFilePath + "/" + uri);
            String url = "file://" + fileDir.getPath();
            return url;
        }
        return null;
    }

    private String getApplicationName() {
        PackageManager packageManager = null;
        ApplicationInfo applicationInfo;
        try {
            packageManager = getApplicationContext().getPackageManager();
            applicationInfo = packageManager.getApplicationInfo(getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            applicationInfo = null;
        }
        String applicationName =
                (String) packageManager.getApplicationLabel(applicationInfo);
        return applicationName;
    }

//    public void setMenuItems(List<MenuItem> menuItems) {
//        if (null == menuItems || menuItems.size() == 0) {
//            return;
//        }
//        mMenuItems.clear();
//        mMenuItems.addAll(menuItems);
//        invalidateOptionsMenu();
//    }
//
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        for (MenuItem menuItem : mMenuItems) {
//            menuItem.getMenuView(menu, this);
//        }
//        return super.onCreateOptionsMenu(menu);
//    }


}