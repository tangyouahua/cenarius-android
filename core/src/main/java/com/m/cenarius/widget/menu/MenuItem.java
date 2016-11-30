package com.m.cenarius.widget.menu;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.text.TextUtils;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.m.cenarius.R;

public class MenuItem {

    public String type;
    public String title;
    public String icon;
    public String uri;
    public String color;

    public void getMenuView(Menu menu, final Context context) {
        if (null == menu) {
            return;
        }
        android.view.MenuItem menuItem = menu.add(title);
        menuItem.setShowAsAction(android.view.MenuItem.SHOW_AS_ACTION_ALWAYS);
        menuItem.setActionView(R.layout.view_button_menu);
        View actionView = menuItem.getActionView();
        final TextView titleView = (TextView) menuItem.getActionView().findViewById(R.id.title);
        if (!TextUtils.isEmpty(color)) {
            try {
                titleView.setTextColor(Color.parseColor(Uri.decode(color)));
                titleView.setText(title);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        actionView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // dispatch uri
                Toast.makeText(context, "click menu, dispatch uri : " + uri, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }
}
