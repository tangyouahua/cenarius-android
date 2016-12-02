package com.m.cenarius.widget.menu;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;
import com.alibaba.fastjson.JSON;
import com.m.cenarius.R;
import com.m.cenarius.activity.CNRSViewActivity;
import com.m.cenarius.view.CenariusWidget;
import com.mcxiaoke.next.task.SimpleTaskCallback;
import com.mcxiaoke.next.task.TaskBuilder;
import java.util.List;
import java.util.concurrent.Callable;

public class MenuWidget implements CenariusWidget {

    static final String TAG = MenuWidget.class.getSimpleName();

    static final String KEY_DATA = "data";

    @Override
    public String getPath() {
        return "/widget/nav_menu";
    }

    @Override
    public boolean handle(final View view, String url) {
        if (TextUtils.isEmpty(url)) {
            return false;
        }
        final Uri uri = Uri.parse(url);
        if (TextUtils.equals(uri.getPath(), getPath())) {
            TaskBuilder.create(new Callable<List<MenuItem>>() {
                @Override
                public List<MenuItem> call() throws Exception {
                    String data = uri.getQueryParameter(KEY_DATA);
                    if (TextUtils.isEmpty(data)) {
                        return null;
                    }

                    // get the menus
                    return JSON.parseArray(data, MenuItem.class);
                }
            }, new SimpleTaskCallback<List<MenuItem>>(){
                @Override
                public void onTaskSuccess(List<MenuItem> menuItems, Bundle extras) {
                    if (null != menuItems && !menuItems.isEmpty()) {
                        // show the menus and tilte
                        if (null != view && view.getContext() instanceof CNRSViewActivity) {
//                            ((CNRSViewActivity) view.getContext()).setMenuItems(menuItems);
                        } else {
                            Toast.makeText(view.getContext(), R.string.error_partial_cenarius_menu, Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }, TAG).start();
            return true;
        }
        return false;
    }
}
