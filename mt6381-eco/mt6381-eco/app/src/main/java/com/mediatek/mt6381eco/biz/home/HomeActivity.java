package com.mediatek.mt6381eco.biz.home;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.OnClick;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.mediatek.mt6381.ble.MT6381Peripheral;
import com.mediatek.mt6381.ble.MT6381RawDataParserForTemp;
import com.mediatek.mt6381.ble.data.SystemInformationData;
import com.mediatek.mt6381eco.BuildConfig;
import com.mediatek.mt6381eco.R;
import com.mediatek.mt6381eco.biz.about.AboutActivity;
import com.mediatek.mt6381eco.biz.account.createAccount.CreateAccountFragment;
import com.mediatek.mt6381eco.biz.flavor.IFlavorUtils;
import com.mediatek.mt6381eco.biz.history.HistoryFeatureListActivity;
import com.mediatek.mt6381eco.biz.measure.MeasureActivity;
import com.mediatek.mt6381eco.biz.peripheral.IPeripheral;
import com.mediatek.mt6381eco.biz.peripheral.PeripheralService;
import com.mediatek.mt6381eco.biz.peripheral_info.PeripheralInfoFragment;
import com.mediatek.mt6381eco.biz.temp.TemperatureActivity;
import com.mediatek.mt6381eco.biz.profile.ProfileActivity;
import com.mediatek.mt6381eco.biz.screening.ScreeningActivity;
import com.mediatek.mt6381eco.biz.startup.StartupActivity;
import com.mediatek.mt6381eco.biz.viewmodel.AppViewModel;
import com.mediatek.mt6381eco.biz.webView.DailyDietActivity;
import com.mediatek.mt6381eco.biz.webView.ExerciseViewActivity;
import com.mediatek.mt6381eco.biz.webView.HealthWeatherActivity;
import com.mediatek.mt6381eco.biz.webView.MarketplaceActivity;
import com.mediatek.mt6381eco.ui.BasePeripheralActivity;
import com.mediatek.mt6381eco.ui.ContainerActivity;

import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import io.reactivex.disposables.CompositeDisposable;

import javax.inject.Inject;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static android.graphics.Color.parseColor;

public class HomeActivity extends BasePeripheralActivity implements HomeContract.View {
    private static final int REQUEST_CREATE_ACCOUNT = 1;
    private static final String CONNECT_ACTION = BuildConfig.APPLICATION_ID + ".connect";
    private static final String DEVICE_NAME_ACTION = BuildConfig.APPLICATION_ID + ".device_name";
    //public final String ACTION_MODEL_POWER_RECEIVER="com.mediatek.mt6381eco.biz.home.ACTION_MODEL_POWER_RECEIVER";
    @Inject
    IFlavorUtils mFlavorUtils;
    @Inject
    HomeContract.Presenter mPresenter;
    @Inject
    HomeViewModel mViewModel;
    @Inject
    AppViewModel mAppViewModel;
    public boolean flag = false;
    //@BindView(R.id.icon_lock_screen) View mImgLockScreen;//krestin remove lock screen

    private final CompositeDisposable mDisposables = new CompositeDisposable();
    PeripheralInfoFragment mPerInFragemt = new PeripheralInfoFragment();

    SystemInformationData mSystemInfoData;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        bindViewModel();
        mFlavorUtils.onHomeStart(this);
        if (savedInstanceState == null) {
            //delete by herman
            //guestConfirmCreateAccount();
        }
        IntentFilter filter = new IntentFilter("com.mediatek.mt6381eco.biz.home.ACTION_MODEL_POWER_RECEIVER");
        registerReceiver(modelBroadcastReceiver, filter);
    }

    BroadcastReceiver modelBroadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("com.mediatek.mt6381eco.biz.home.ACTION_MODEL_POWER_RECEIVER")) {
                int powerData = intent.getIntExtra("pData", 0);
                Log.d("HomeActivity", "ModelPowerBroadCastReceiver--Data：" + powerData);
                if (powerData <= 15) {
                    String message = context.getString(R.string.power_low_warning);
                    Toast toast = Toast.makeText(context, Html.fromHtml(message), Toast.LENGTH_LONG);
                    LinearLayout linearLayout = (LinearLayout) toast.getView();
                    TextView messageTextView = (TextView) linearLayout.getChildAt(0);
                    messageTextView.setTextSize(16);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    showMyToast(toast, 4 * 1000);

                }
            }
        }

        //krestin add to make long toast time start
        public void showMyToast(final Toast toast, final int cnt) {
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
        //krestin add to make long toast time end
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDisposables.clear();
        mPresenter.destroy();
        unregisterReceiver(modelBroadcastReceiver);
    }

    private void guestConfirmCreateAccount() {
        mAppViewModel.account.observe(this, account -> {
            if (account != null && account.isGuest) {
                new MaterialDialog.Builder(this).title(R.string.attention)
                        .content(R.string.guest_attention)
                        .positiveText(R.string.yes)
                        .negativeText(R.string.no)
                        .onPositive((dialog, which) -> onActionCreateAccount())
                        .show();
            }
        });
    }

    @Override
    protected void attach(IPeripheral peripheral) {
        super.attach(peripheral);
        mPresenter.attach(peripheral);
        mViewModel.getConnectionState().setValue(peripheral.getConnectionState());
        mDisposables.add(peripheral.onConnectionChange()
                .subscribe(state -> mViewModel.getConnectionState().postValue(state)));
    }

    private void bindViewModel() {
        mViewModel.getConnectionState().observe(this, state -> invalidateOptionsMenu());
        mAppViewModel.account.observe(this, account -> {
            //krestin remove lock screen 20200926
            //mImgLockScreen.setVisibility(account.permission.screening ? View.GONE : View.VISIBLE);
            invalidateOptionsMenu();
        });

        mViewModel.deleteCalibrationResource.observe(this, resource -> {
            switch (resource.status) {
                case LOADING: {
                    startLoading(getString(R.string.delete_calibration));
                    break;
                }
                case SUCCESS: {
                    stopLoading();
                    break;
                }
                case ERROR: {
                    stopLoading();
                    Toast.makeText(this, resource.throwable.getMessage(), Toast.LENGTH_LONG).show();
                    break;
                }
            }
        });
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        boolean isGuest =
                mAppViewModel.account.getValue() != null && mAppViewModel.account.getValue().isGuest;

        //herman 屏蔽-版本降级
        menu.findItem(R.id.action_downgrade).setVisible(false);
        //herman 访客模式下屏蔽-校准
        //menu.findItem(R.id.action_delete_calibration).setVisible(!isGuest);
        //herman 访客模式下屏蔽-创建账户 set isGuest to false
        menu.findItem(R.id.action_create_account).setVisible(false);

        menu.findItem(R.id.action_sign_out).setVisible(!isGuest);
        setActionBluetooth(menu.findItem(R.id.action_bluetooth));
        //krestin add to save the guest situation start
        flag = isGuest;
        SharedPreferences mTempSperf = getSharedPreferences("isGuest", MODE_APPEND);
        SharedPreferences.Editor mTempEdit = mTempSperf.edit();
        mTempEdit.putBoolean("flag", flag);
        mTempEdit.commit();
        //krestin add to save the guest situation end
        return super.onPrepareOptionsMenu(menu);
    }

    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.action_power: {
                this.findViewById(R.id.action_power).setEnabled(false);
                //findViewById(R.id.action_power).setTooltipText("50");
                break;
            }
            case R.id.action_profile: {
                startActivity(new Intent(this, ProfileActivity.class));
                break;
            }
            case android.R.id.home: {
                finish();
                break;
            }
            case R.id.action_bluetooth: {
                if (mViewModel.getConnectionState().getValue() == IPeripheral.STATE_CONNECTED) {
                    startActivity(new Intent(DEVICE_NAME_ACTION));
                } else {
                    startActivity(new Intent(CONNECT_ACTION));
                }

                break;
            }

            case R.id.action_sign_out: {
                if (mViewModel.getConnectionState().getValue() == IPeripheral.STATE_CONNECTED) {
                    mPresenter.disconnect();
                }
                subscribe(R.string.sign_out, mPresenter.requestSignOut());
                break;
            }
            case R.id.action_about: {
                startActivity(new Intent(this, AboutActivity.class));
                break;
            }
            case R.id.action_downgrade: {
                mPresenter.downgrade();
                break;
            }
            case R.id.action_upgrade: {
                startActivity(new Intent(this, SystemUpdateActivity.class));
                break;
            }
            //delete calibration menu start by krestin
            /*case R.id.action_delete_calibration:{
               mPresenter.deleteCalibration();
               break;
            }*/
            //delete calibration menu end by krestin
            case R.id.action_create_account: {
                onActionCreateAccount();
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void onActionCreateAccount() {
        startActivityForResult(ContainerActivity.makeIntent(this, CreateAccountFragment.class),
                REQUEST_CREATE_ACCOUNT);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    //krestin add to make long toast time start
    public void showMyToast(final Toast toast, final int cnt) {
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

    //krestin add to make long toast time end
    @OnClick({R.id.measure, R.id.health_journal, R.id.temperature, R.id.screening, R.id.consulting, R.id.ranking, R.id.health_report, R.id.exercise, R.id.daily_diet})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.measure:
                if (IPeripheral.STATE_CONNECTED != mViewModel.getConnectionState().getValue()) {
                    //更改字体颜色 by herman
                    //showError(getString(R.string.connection_lost));
                    //方法一
                    String message = "<font color='#FF0000'>" + getString(R.string.connection_lost) + "</font>";
                    Toast toast1 = Toast.makeText(this, Html.fromHtml(message), Toast.LENGTH_LONG);
                    toast1.show();    // 展示toast
                    //方法二
                    TextView tview = new TextView(this);
                    tview.setText(R.string.connection_lost);
                    tview.setTextColor(getResources().getColor(R.color.red));    // 文本颜色

                    Toast toast2 = new Toast(this);
                    toast2.setDuration(Toast.LENGTH_LONG);
                    toast2.setView(tview); // 将文本插入到toast里
                    //toast2.show();	// 展示toast
                } else {
                    startActivity(new Intent(HomeActivity.this, MeasureActivity.class));
                }
                break;

            case R.id.health_journal:
                //这里添加弹窗提示 add by herman
                new MaterialDialog.Builder(this)
                        .title(R.string.attention)
                        .content(R.string.journal_attention)
                        .positiveText(R.string.ok2)
                        .onAny(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                startActivity(new Intent(HomeActivity.this, HistoryFeatureListActivity.class));
                            }
                        })
                        .show();
                //end
                break;

            case R.id.temperature:
                //当蓝牙连接时：
                if (mViewModel.getConnectionState().getValue() == IPeripheral.STATE_CONNECTED) {
                    startActivity(new Intent(this, TemperatureActivity.class));
                } else {
                    //这里添加弹窗提示蓝牙连接 add by herman
                    new MaterialDialog.Builder(this)
                            .title(R.string.attention)
                            .content(R.string.temperature_attention2)
                            .positiveText(R.string.ok2)
                            .show();
                }
                break;

            case R.id.screening:
                if (!isChinese()) {
                    startActivity(new Intent(this, HealthWeatherActivity.class));
                } else {
                    //krestin modify to remind covid-19 start
                    new MaterialDialog.Builder(this)
                            .title(R.string.attention)
                            .content(R.string.string_covidText)
                            .negativeText(R.string.no)
                            .positiveText(R.string.yes)
                            .onNegative(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction dialogAction) {
                                    String message = getString(R.string.string_toast);
                                    Toast toast = Toast.makeText(getBaseContext(), Html.fromHtml(message), Toast.LENGTH_LONG);
                                    LinearLayout linearLayout = (LinearLayout) toast.getView();
                                    TextView messageTextView = (TextView) linearLayout.getChildAt(0);
                                    messageTextView.setTextSize(20);
                                    toast.setGravity(Gravity.CENTER, 0, 0);
                                    showMyToast(toast, 6 * 1000);
                                }
                            })
                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                                    onLayoutScreenClick();
                                }
                            }).show();
                }
                //krestin modify to remind covid-19 end
                break;
            //todo by herman

            case R.id.consulting:
                startActivity(new Intent(this, ConsultingActivity.class));
                break;

            case R.id.ranking:
                startActivity(new Intent(this, RankingActivity.class));
                break;

            case R.id.health_report:
                startActivity(new Intent(this, MarketplaceActivity.class));
                //startActivity(new Intent(this, HealthReportActivity.class));
                break;

            case R.id.exercise:
                startActivity(new Intent(this, ExerciseViewActivity.class));
                //startActivity(new Intent(this, ExerciseActivity.class));
                break;

            case R.id.daily_diet:
//                startActivity(new Intent(this, DietActivity.class));
                startActivity(new Intent(this, DailyDietActivity.class));
                break;

        }
    }

    private void onLayoutScreenClick() {
        /*krestin remove pre_screening lock icon 20200926 start*/
        //if (!mAppViewModel.account.getValue().permission.screening) {
        //  new MaterialDialog.Builder(this).content(
        //      getString(R.string.alert_screen_purchase_confirm1) + getString(
        //          R.string.alert_screen_purchase_confirm2))
        //      .negativeText(R.string.no)
        //     .positiveText(R.string.yes)
        //      .onPositive((dialog, which) -> mPresenter.upgrade())
        //      .show();
        //} else {
        /*krestin remove pre_screening lock icon 20200926 end*/
        startActivity(new Intent(this, ScreeningActivity.class));
        //}//krestin remove
    }

    private void setActionBluetooth(MenuItem actionBluetooth) {
        if (new Intent(CONNECT_ACTION).resolveActivityInfo(getPackageManager(), 0) == null) {
            actionBluetooth.setVisible(false);
        } else {
            int state = -1;
            if (mViewModel.getConnectionState().getValue() != null) {
                state = mViewModel.getConnectionState().getValue();
            }
            switch (state) {
                case IPeripheral.STATE_CONNECTED: {
                    actionBluetooth.setIcon(R.drawable.btn_home_device_connected);
                    break;
                }
                case IPeripheral.STATE_DISCONNECTED: {
                    actionBluetooth.setIcon(R.drawable.btn_home_device_disconnected);
                    break;
                }
            }
        }
    }

    @Override
    public void navToStartup() {
        uiAction(() -> {
            finish();
            startActivity(new Intent(this, StartupActivity.class));
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CREATE_ACCOUNT: {
                if (resultCode == Activity.RESULT_OK) {
                    new MaterialDialog.Builder(this).title(R.string.create_account_success)
                            .content(R.string.guest_create_account_success_tip)
                            .positiveText(R.string.ok)
                            .show();
                }
                break;
            }
        }
    }

    public boolean isChinese() {
        Locale locale = Locale.getDefault();
        String language = locale.getLanguage();
        return "zh".equals(language);
    }
}
