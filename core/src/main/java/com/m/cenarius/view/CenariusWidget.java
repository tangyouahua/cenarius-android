package com.m.cenarius.view;

import android.view.View;

public interface CenariusWidget {

    /**
     * a special path for the widget
     *
     * @return
     */
    String getPath();

    /**
     * whether we can handle the url
     * @param view
     * @param url
     * @return
     */
    boolean handle(View view, String url);

}
