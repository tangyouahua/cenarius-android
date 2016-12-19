package com.m.cenarius.example;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;

import com.m.cenarius.activity.CNRSViewActivity;

import org.apache.cordova.CordovaFragment;

public class FragmentActivity extends CNRSViewActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment);

        CordovaFragment fragment = new CordovaFragment();
        Bundle bundle = new Bundle();
        bundle.putString("uri", uri);
        bundle.putString("htmlFileURL", htmlFileURL);
        fragment.setArguments(bundle);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment, fragment);
        transaction.commit();
    }
}
