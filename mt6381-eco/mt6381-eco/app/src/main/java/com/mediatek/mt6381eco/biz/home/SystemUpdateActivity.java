package com.mediatek.mt6381eco.biz.home;

import android.support.v4.content.FileProvider;
import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.mediatek.mt6381eco.BuildConfig;
import com.mediatek.mt6381eco.R;

import butterknife.ButterKnife;

import com.ads.https.AdsSession;
import com.ads.main.AdsApp;
import com.redstone.ota.app.RsAdsCheckListener;
import com.redstone.ota.app.RsAppConfigParam;
import com.redstone.ota.app.RsAppDeviceInfo;
import com.redstone.ota.app.RsAppListener;
import com.mediatek.mt6381eco.biz.home.OnClickUtil;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

public class SystemUpdateActivity extends AppCompatActivity implements View.OnClickListener {
    Button btnCheck;
    Button btnInstall;
    TextView txtVersion;
    private String TAG = "SystemUpdateActivity";
    private boolean newVersionFlag = false;
    private boolean installFlag = false;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.update_app);
        ButterKnife.bind(this);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        initView();
        initListener();
        appSdkInit();

    }

    // 初始化界面
    private void initView() {
        btnCheck = (Button) findViewById(R.id.btn_check);
        btnInstall = (Button) findViewById(R.id.btn_install);
        txtVersion = (TextView) findViewById(R.id.txt_version);

        btnCheck.setOnClickListener(this);
        btnInstall.setOnClickListener(this);
        txtVersion.setText(BuildConfig.VERSION_NAME);
    }

    private void initListener() {

        IntentFilter it = new IntentFilter();
        it.addAction(RsAppListener.CUS_REPLY_APP_INSTALL);
        it.addAction(RsAppListener.CUS_REPLY_APP_CHECK);
        this.getApplicationContext().registerReceiver(
                new OtaBroadcastListener(), it);

        IntentFilter appFilter = new IntentFilter();
        appFilter.addAction(RsAppListener.CUS_START_APP_CHECK);
        appFilter.addAction(RsAppListener.CUS_START_APP_INSTALL);
        this.getApplicationContext().registerReceiver(new RsAppListener(),
                appFilter);
    }

    // 初始化sdk
    public void appSdkInit() {
        AdsApp.getInstance().setContext(this.getApplicationContext());
        AdsApp.getInstance().create();
        AdsApp.getInstance().setLogOn(true);
        AdsApp.getInstance().setDeviceInfoCallback(
                new RsAppDeviceInfo(this.getApplicationContext()));
        AdsApp.getInstance()
                .getConfigurator()
                .setConfigStrategy(
                        new RsAppConfigParam(this.getApplicationContext()));
        AdsApp.getInstance().getCheckManager()
                .setCheckRequestCallback(new RsAdsCheckListener());
        AdsSession.getSyncRequestString();
        AdsApp.getInstance().getCheckManager().autoCheck();
    }

    @Override
    public void onClick(View v) {
        Log.d("appinstall", "onclick");
        // TODO Auto-generated method stub
        if (OnClickUtil.isTooFast()) {
            Log.d(TAG, "onclick return");
            return;
        }
        switch (v.getId()) {
            case R.id.btn_check: // 触发检测
                sendCheckBroadcast("com.mediatek.mt6381eco.bt.prod");
                break;
            case R.id.btn_install: // 触发安装
                if (checkUnknownSource() == false) {//第三方安装权限是否设置
                    return;
                }
                sendInstallBroadcast();
                break;
            default:
                break;
        }
    }

    //发送检测任务广播
    public void sendCheckBroadcast(String appName) {
        Log.d(TAG, "sendCheckBroadcast ： " + appName);
        Intent it = new Intent(RsAppListener.CUS_START_APP_CHECK);
        it.putExtra(INSTLL_PACKAGE_NAME, appName);
        this.getApplicationContext().sendBroadcast(it);
    }

    // 发送安装应用广播
    public void sendInstallBroadcast() {


        new MaterialDialog.Builder(this)
                .title(R.string.attention)
                .content(R.string.install_attention)
                .negativeText(R.string.no)
                .positiveText(R.string.yes)
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction dialogAction) {
                        //To do something
                    }
                })
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                        Log.d(TAG, "sendInstallBroadcast");
                        Intent it = new Intent(RsAppListener.CUS_START_APP_INSTALL);
                        sendBroadcast(it);

                        btnCheck.setEnabled(false);
                        String message = getString(R.string.pre_installing);
                        Toast toast = Toast.makeText(getApplicationContext(), Html.fromHtml(message), Toast.LENGTH_LONG);
                        showInstallToast(toast, 3 * 1000);

                    }
                }).show();
    }

    // 广播检测发起，需要升级的应用名称
    public static String CUS_START_APP_CHECK = "com.redstone.app.check.action";
    public static String INSTLL_PACKAGE_NAME = "package_name";

    public static String CUS_START_APP_INSTALL = "com.redstone.app.install.action";

    // 广播检测结果返回字段名称
    public static String CUS_REPLY_APP_CHECK = "com.redstone.app.check.reply";
    public static String REPLY_CHECK_RESULT = "check_result";

    // 广播安装结果返回字段名称
    public static String CUS_REPLY_APP_INSTALL = "com.redstone.app.install.reply";
    public static String REPLY_INSTALL_RESULT = "install_result";


    // 广播接收OTA应用检测和安装结果监听
    class OtaBroadcastListener extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "get action :" + intent.getAction());
            String action = intent.getAction();
            if (RsAppListener.CUS_REPLY_APP_CHECK.equals(action)) {
                // 检测结果广播返回；true为有新版本，false为无新版本；
                Boolean newVersion = intent.getBooleanExtra(RsAppListener.REPLY_CHECK_RESULT,
                        false);
                Log.d("onReceive", "check result : " + newVersion);
                newVersionFlag = newVersion;

                if (newVersionFlag) {
                    Log.d(TAG, "New Version feedBack!");
                    btnCheck.setEnabled(false);
                    txtVersion.setText(R.string.has_new_version);
                    btnCheck.setText(R.string.version_check);
                    btnInstall.setEnabled(true);

                    String mesNewVersion = getString(R.string.new_version_checked);
                    Toast toastNewVersion = Toast.makeText(getApplicationContext(), Html.fromHtml(mesNewVersion), Toast.LENGTH_LONG);
                    showInstallToast(toastNewVersion, 2000);
                } else {
                    Log.d(TAG, "Have no new version!");
                    btnCheck.setEnabled(true);
                    txtVersion.setText(R.string.latest_version);
                    btnCheck.setText(R.string.version_check);
                    btnInstall.setEnabled(false);

                    String mesNoVersion = getString(R.string.latest_version);
                    Toast toastNoVersion = Toast.makeText(getApplicationContext(), Html.fromHtml(mesNoVersion), Toast.LENGTH_LONG);
                    showInstallToast(toastNoVersion, 2000);
                }
            } else if (RsAppListener.CUS_REPLY_APP_INSTALL.equals(action)) {
                // 安装结果广播返回，true为安装成功，false为安装失败
                Boolean installResult = intent.getBooleanExtra(
                        RsAppListener.REPLY_INSTALL_RESULT, false);
                Log.d("onReceive", "check result : " + installResult);
                installFlag = installResult;

                if (installFlag) {
                    Log.d(TAG, "Install successfully  feedBack!");
                    btnCheck.setEnabled(true);
                    btnInstall.setEnabled(false);
                    btnInstall.setText(R.string.str_install);
                    txtVersion.setText(R.string.install_success);
                }
            }
        }
    }

    private boolean checkPermissionWrite() {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return true;
        }

        int permission_write = ContextCompat.checkSelfPermission(SystemUpdateActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int permission_read = ContextCompat.checkSelfPermission(SystemUpdateActivity.this,
                Manifest.permission.READ_EXTERNAL_STORAGE);
        if (permission_write != PackageManager.PERMISSION_GRANTED
                || permission_read != PackageManager.PERMISSION_GRANTED) {
            //申请权限，特征码自定义为1，可在回调时进行相关判断
            ActivityCompat.requestPermissions(SystemUpdateActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            return false;
        }
        return true;
    }

    // 配置第三方应用安装应用权限
    private boolean checkUnknownSource() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {// 拉起第三方安装应用权限框，用户设置允许后方能继续安装
            Boolean haveInstallPermission = getPackageManager().canRequestPackageInstalls();
            if (!haveInstallPermission) {//没有权限
                Uri packageURI = Uri.parse("package:" + getPackageName());
                //注意这个是8.0新API
                Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, packageURI);
                startActivityForResult(intent, 10086);
                return false;
            }
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                finish();
                break;
            }
        }
        return true;
    }

    public void showInstallToast(final Toast toast, final int cnt) {
        final Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                toast.show();
            }
        }, 0, 3000);
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                toast.cancel();
                timer.cancel();
            }
        }, cnt);
    }
}