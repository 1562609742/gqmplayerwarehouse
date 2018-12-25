package com.gq.mediaplayer;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.BuildConfig;
import android.support.v4.app.ActivityCompat;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MyApplication.getInstance().addActivity(this);
        if ((getIntent().getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != 0) {
            finish();
            return;
        }
       init();
    }
    private void getStorageReq() {
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
    }
    private void init() {
        if (getAndroidSDKVersion() >= 23) {
            getStorageReq();
            }
        Button btn = findViewById(R.id.btn);
        final EditText url_edit = findViewById(R.id.url_edit);
        final EditText subtitle = findViewById(R.id.subtitle_edit);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent in = new Intent(MainActivity.this, VideoPlayActivity.class);
                String url = url_edit.getText().toString();
                String subtitleUrl = subtitle.getText().toString().trim();
                in.putExtra("videoUri", url);
                in.putExtra("subUri", subtitleUrl);
                startActivity(in);
            }
        });
    }

    public static int getAndroidSDKVersion() {
        int version = 0;
        try {
            version = Integer.valueOf(android.os.Build.VERSION.SDK);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return version;
    }

    private void initSetting() {
        Intent intent = new Intent();
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("packageName", BuildConfig.APPLICATION_ID);
        ComponentName comp = new ComponentName("com.android.providers.settings", "com.android.providers.settings.MainActivity");
        intent.setComponent(comp);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MyApplication.getInstance().exit();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return true;
        }
        return super.onKeyDown(keyCode, event);

    }
}
