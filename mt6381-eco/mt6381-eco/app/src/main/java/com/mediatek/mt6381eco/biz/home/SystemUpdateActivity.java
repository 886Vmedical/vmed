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

    // ???????????????
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

    // ?????????sdk
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
            case R.id.btn_check: // ????????????
                sendCheckBroadcast("com.mediatek.mt6381eco.bt.prod");
                break;
            case R.id.btn_install: // ????????????
                if (checkUnknownSource() == false) {//?????????????????????????????????
                    return;
                }
                sendInstallBroadcast();
                break;
            default:
                break;
        }
    }

    //????????????????????????
    public void sendCheckBroadcast(String appName) {
        Log.d(TAG, "sendCheckBroadcast ??? " + appName);
        Intent it = new Intent(RsAppListener.CUS_START_APP_CHECK);
        it.putExtra(INSTLL_PACKAGE_NAME, appName);
        this.getApplicationContext().sendBroadcast(it);
    }

    // ????????????????????????
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

    // ????????????????????????????????????????????????
    public static String CUS_START_APP_CHECK = "com.redstone.app.check.action";
    public static String INSTLL_PACKAGE_NAME = "package_name";

    public static String CUS_START_APP_INSTALL = "com.redstone.app.install.action";

    // ????????????????????????????????????
    public static String CUS_REPLY_APP_CHECK = "com.redstone.app.check.reply";
    public static String REPLY_CHECK_RESULT = "check_result";

    // ????????????????????????????????????
    public static String CUS_REPLY_APP_INSTALL = "com.redstone.app.install.reply";
    public static String REPLY_INSTALL_RESULT = "install_result";


    // ????????????OTA?????????????????????????????????
    class OtaBroadcastListener extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "get action :" + intent.getAction());
            String action = intent.getAction();
            if (RsAppListener.CUS_REPLY_APP_CHECK.equals(action)) {
                // ???????????????????????????true??????????????????false??????????????????
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
                // ???????????????????????????true??????????????????false???????????????
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
            //????????????????????????????????????1????????????????????????????????????
            ActivityCompat.requestPermissions(SystemUpdateActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            return false;
        }
        return true;
    }

    // ???????????????????????????????????????
    private boolean checkUnknownSource() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {// ??????????????????????????????????????????????????????????????????????????????
            Boolean haveInstallPermission = getPackageManager().canRequestPackageInstalls();
            if (!haveInstallPermission) {//????????????
                Uri packageURI = Uri.parse("package:" + getPackageName());
                //???????????????8.0???API
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