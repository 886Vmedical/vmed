package com.mediatek.mt6381eco.biz.splash;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.mediatek.mt6381eco.R;
import com.mediatek.mt6381eco.biz.startup.StartupActivity;

import java.util.concurrent.atomic.AtomicBoolean;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;


public class VmedSplashActivity extends Activity {
    private static int SPLASH_DISPLAY_LENGTH= 800;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().requestFeature(Window.FEATURE_NO_TITLE);//去掉标题
        setContentView(R.layout.splash);
        new Handler().postDelayed(new Runnable() {
            public void run() {
                Intent intent = new Intent(VmedSplashActivity.this, StartupActivity.class);
                startActivity(intent);
                VmedSplashActivity.this.finish();
            }
        }, SPLASH_DISPLAY_LENGTH);
    }
}
