package com.mediatek.mt6381eco.biz.temp;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsSeekBar;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.mediatek.blenativewrapper.StateInfo;
import com.mediatek.mt6381.ble.MT6381Peripheral;
import com.mediatek.mt6381.ble.MT6381RawDataParserForTemp;
import com.mediatek.mt6381eco.R;
import com.mediatek.mt6381eco.biz.temp.CountDownActivity;
import com.mediatek.mt6381eco.biz.peripheral.IBlePeripheral;
import com.mediatek.mt6381eco.biz.peripheral.IPeripheral;
import com.mediatek.mt6381eco.biz.peripheral.PeripheralService;
import com.mediatek.mt6381eco.db.TempDataBaseOperation;
import com.mediatek.mt6381eco.db.TemperatureSQLiteHelper;
import com.mediatek.mt6381eco.ui.BasePeripheralActivity;
import com.mediatek.mt6381eco.utils.ServiceBinding;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.BrokenBarrierException;

import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.disposables.CompositeDisposable;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import timber.log.Timber;
import javax.inject.Inject;


public class TemperatureActivity extends BasePeripheralActivity implements TempContract.View ,View.OnClickListener{

    private final MT6381Peripheral mMt6381Peripheral = PeripheralService.mMt6381Peripheral;
    private double doubletempData = MT6381RawDataParserForTemp.doubletempData;
    public double doubletempDataF;
    public double double2TempForUpload;

    private String temperatureC, temperatureF;
    private TextView temperatureTV;
    private TextView testModeTV;
    private Button testButton;
    private Button calibrationButton;
    private final Handler handler = new Handler();
    private static final String KEY_Switch = "hasTempbutton";
    private boolean virtualMode = false;
    private boolean newTemp = false;
    private boolean tempVirtual = false;

    private ServiceBinding.Unbind mServiceUnBinder;
    //private IPeripheral  mIBlePeripheral;
    private IPeripheral mIPeripheral;
    private final CompositeDisposable mDisposables = new CompositeDisposable();


    //add by herman for network
    @Inject TempContract.Presenter mPresenter;

    public final static int count = 0;
    TextView  mCountTV;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("TemperatureActivity","onCreate");
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        initView();

        //父类已调用。
        //bindService(new Intent(this, PeripheralService.class), this, Context.BIND_AUTO_CREATE);

       /* mServiceUnBinder = ServiceBinding.bindService(this, PeripheralService.class,
                service -> mIPeripheral = (IPeripheral) service);*/

        /*mServiceUnBinder = ServiceBinding.bindService(this, PeripheralService.class,
                service -> mIBlePeripheral = (IBlePeripheral) service);*/
    }


    private void initView(){
        setContentView(R.layout.activity_temperature);
        temperatureTV = findViewById(R.id.temperature_tv);
        temperatureTV.setTextSize(50);
        temperatureTV.setText(R.string.temp_string_data);
        //krestin remove bottom text start
        //testModeTV = findViewById(R.id.testMode);
        //krestin remove bottom text end
        testButton = findViewById(R.id.test_button);
        calibrationButton = findViewById(R.id.calibration_button);
        //绑定监听
        testButton.setOnClickListener(this);
        calibrationButton.setOnClickListener(this);

    }
    @Override
    protected void onResume() {
        Log.d("TemperatureActivity","onResume");
        super.onResume();
        updateSettingsInof();
        //其实就是生成一个what为0的Message
        handler.post(runner);
    }


    private void updateSettingsInof() {
        SharedPreferences mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        virtualMode = mSharedPreferences.getBoolean(KEY_Switch, true);
        if(virtualMode){
            //krestin remove bottom text start
            //testModeTV.setText(R.string.virtualMode);
            //krestin remove bottom text end
            testButton.setVisibility(View.VISIBLE);
        }else{
            //krestin remove bottom text start
            //testModeTV.setText(R.string.physicalMode);
            //krestin remove bottom text end
            testButton.setVisibility(View.INVISIBLE);
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_temperature, menu);
        return true;
    }

    @Override public boolean onPrepareOptionsMenu(Menu menu) {

        return super.onPrepareOptionsMenu(menu);
    }

    @Override public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                finish();
                break;
            }

            case R.id.menu_settings: {
                startActivity(new Intent(this,SettingsActivity.class));
                break;
            }

        }
        return true;
    }


    private final Runnable runner = new Runnable() {
        @Override
        public void run() {
            //方法二：延迟1秒更新，并让它循环handler 第二次post
            handler.postDelayed(this, 1000);
            Log.d("TemperatureActivity","runner...");
            Log.d("TemperatureActivity","mIPeripheral.getConnectionState(): " + mIPeripheral.getConnectionState());
            if (mIPeripheral.getConnectionState() == IPeripheral.STATE_CONNECTED) {
                updateTemp();
            }else{
                //终止线程
                closeAll();
                showBTError();
            }
        }
    };

    private void showBTError(){
            MaterialDialog.Builder builder = new MaterialDialog.Builder(TemperatureActivity.this).cancelable(false);
            builder.title(R.string.bp_measure_exception_title)
                    .content(R.string.connection_lost)
                    .positiveText(R.string.exit)
                    .dismissListener(dialog -> TemperatureActivity.this.finish());
            builder.show();
            //toast 方式
            // showError(getString(R.string.connection_lost));
    }

    public boolean isChinese() {
        Locale locale = Locale.getDefault();
        String language = locale.getLanguage();
        //String country = locale.getCountry().toLowerCase();
        //language.endsWith("zh")
        return "zh".equals(language);
    }

    public double getOneUploadTemp(){

        double2TempForUpload = (double) Math.round(doubletempDataF * 100) / 100;
        Log.d("TemperatureActivity","double2TempForUpload: " + double2TempForUpload);//32.0
        return double2TempForUpload;

    }

    public void updateTemp(){

        doubletempData = MT6381RawDataParserForTemp.doubletempData;
        Log.d("TemperatureActivity","doubletempData: " + doubletempData);
        temperatureC = String.format("%.2f", doubletempData);//会自动四舍五入
        //对应的华氏度，上传服务端用
        doubletempDataF = doubletempData*1.8 + 32 ;//这里可能是三位小数
        temperatureF = String.format("%.2f", doubletempDataF);
        Log.d("TemperatureActivity","temperatureF: " + temperatureF);
        Log.d("TemperatureActivity","newTemp: " + newTemp);

        if(temperatureC.equals("219.30")){//AA55
                newTemp = true;
                //这里for 物理按键模式
                testButton.setVisibility(View.GONE);
                temperatureTV.setTextSize(50);
                temperatureTV.setText(R.string.temp_measuring);//正在测量

        }else{
                if(newTemp){
                    testButton.setVisibility(View.VISIBLE);
                    //startActivity(new Intent(this, CountDownActivity.class));
                    if(doubletempDataF > 185.0 || doubletempDataF <= 32.0 ){
                        newTemp = false;
                        temperatureTV.setTextSize(30);
                        temperatureTV.setText(R.string.temp_remeasure);
                        Toast toastTempView = Toast.makeText(this,R.string.temp_out_of_range, Toast.LENGTH_SHORT);
                        LinearLayout linearLayout = (LinearLayout) toastTempView.getView();
                        TextView messageTextView = (TextView) linearLayout.getChildAt(0);
                        messageTextView.setTextSize(20);
                        toastTempView.setGravity(Gravity.CENTER,0,0);
                        toastTempView.show();

                    }else {
                        newTemp = false;
                        showTemp();
                        mPresenter.onUploadtemp();
                        saveNewTempToSP();
                        saveToLocalDB();
                    }
                }
                return;
        }

    }

    private void saveToLocalDB(){
        //krestin add to confirm to get the last temperature user measured start
        SharedPreferences mFlagSperf = getSharedPreferences("isGuest",MODE_APPEND);
        boolean flagGuest = mFlagSperf.getBoolean("flag",false);
        Log.d("TemperatureActivity", "temperatureFRecord: " + temperatureF);
        if(flagGuest) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");// HH:mm:ss
            Date date = new Date(System.currentTimeMillis());
            String fullDate = simpleDateFormat.format(date);
            TempDataBaseOperation mTempOper = new TempDataBaseOperation(this, "tempDT");
            mTempOper.addRecords(fullDate, temperatureF);
        }
        //krestin add to confirm to get the last temperature user measured end
    }

    //krestin存数据 for COVID
    private void saveNewTempToSP(){
        //krestin get temperature data from measure when people complete temperature test start
        SharedPreferences mTempSperf = getSharedPreferences("vemdtemp",MODE_APPEND);
        SharedPreferences.Editor mTempEdit  = mTempSperf.edit();
        mTempEdit.putString("temperature", temperatureF);
        mTempEdit.commit();
        //krestin get temperature data from measure when people complete temperature test end
    }

    private void showTemp(){
        temperatureTV.setTextSize(100);
        if(isChinese()){
            temperatureTV.setText(temperatureC);
        }else{
            temperatureTV.setText(temperatureF);
        }
    }


/*
    @Override
    protected void onNewIntent(Intent intent)
    {
        super.onNewIntent(intent);
        setIntent(intent); //如果更新intent的话，这一句必须的，否则Intent无法获得最新的数据
    }
*/

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {

        switch (v.getId()){

            case R.id.test_button:
                startActivity(new Intent(this, CountDownActivity.class));
                findViewById(R.id.menu_settings).setVisibility(View.INVISIBLE);
                sendAndReadTemperature();
                initView();
                temperatureTV.setTextSize(50);
                setTitle(R.string.temperature);
                findViewById(R.id.menu_settings).setVisibility(View.VISIBLE);
                updateSettingsInof();


                break;

            case R.id.calibration_button:
                //发送校准命令
                sendToCalibrationTemperature();
                break;

            default:
                break;
        }


    }


    private void countdown(){
        //setContentView(R.layout.countdown_data);
        //setTitle(R.string.countdown);
        //findViewById(R.id.menu_settings).setVisibility(View.INVISIBLE);
        //mCountTV = findViewById(R.id.tvTime);
        //这里添加倒计时
        //startActivity(new Intent(this, CountDownActivity.class));
        //mCountDownTimer.start();
    }


    private CountDownTimer mCountDownTimer= new CountDownTimer(3*1000, 1000) {
        public void onTick(long millisUntilFinished) {
            mCountTV.setText("" + ((int)millisUntilFinished / 1000 + 1));
        }

        /**
         * Callback fired when the time is up.
         */
        @Override
        public void onFinish() {
            mCountDownTimer.cancel();
        }
    };


    //mMt6381Peripheral.sendCommand(new AskTemperatureCommand());

        /*mDisposables.add(mIBlePeripheral.readTemperature()
                .doOnSubscribe(disposable -> Resource.loading(null))
                .subscribe(() -> Resource.success(null),
                        throwable -> Resource.error(throwable, null)));*/


        public void sendAndReadTemperature(){
            mDisposables.add(mIPeripheral.readTemperature()
                .subscribe(() -> { Timber.d("start readTemperature success"); },
                        throwable -> {
                            Timber.d("start readTemperature success has exception");
                            Timber.w(throwable); }
                ));
            Toast.makeText(this, getString(R.string.wait_temperature), Toast.LENGTH_SHORT).show();
         }

    public void sendToCalibrationTemperature(){
        mDisposables.add(mIPeripheral.calibrationTemperature()
                .subscribe(() -> { Timber.d("start readTemperature success"); },
                        throwable -> {
                            Timber.d("start readTemperature success has exception");
                            Timber.w(throwable); }
                ));
        Toast.makeText(this, getString(R.string.temperature_calibration), Toast.LENGTH_SHORT).show();
    }


    @Override protected void attach(IPeripheral peripheral) {
        super.attach(peripheral);
        Log.d("TemperatureActivity","attach...");
        mIPeripheral = peripheral;
        Log.d("TemperatureActivity","mIPeripheral.getConnectionState(): " + mIPeripheral.getConnectionState());//3
    }


    @Override public void onBackPressed() {
        Log.d("TemperatureActivity","onBackPressed");
        setResult(Activity.RESULT_CANCELED);
        //在这里上传温度  没运行
        //mPresenter.onUploadtemp();
        finish();
    }



    @Override
    protected void onPause() {
        Log.d("TemperatureActivity","onPause");
        super.onPause();
        testButton.setVisibility(View.VISIBLE);
        temperatureTV.setTextSize(50);
        temperatureTV.setText(R.string.default_no_measure);
    }

    @Override
    protected void onRestart() {
        Log.d("TemperatureActivity","onRestart");
        super.onRestart();
    }

    @Override
    protected void onDestroy() {
        Log.d("TemperatureActivity","onDestroy");
        super.onDestroy();
        closeAll();
        finish();
    }

    private void closeAll(){
        //if(mCountDownTimer != null )
        mCountDownTimer.cancel();
        //停止handler 方法一
        handler.removeCallbacks(runner);
        //停止handler 方法二
        handler.removeMessages(0);
        handler.removeCallbacksAndMessages(null);
        mDisposables.clear();
    }



    /*
//参考代码
    private void openSensor(boolean downSample) {
        mDisposables.add(mPeripheral.startMeasure(downSample)
        .subscribe(() -> {
            Timber.d("start sensor success");
            sendMessage(EVT_START_MEASURE_SUCCESS);
            mBaseViewModel.remeasure.postValue(Resource.success(null));
        }, throwable -> {
            Timber.d("start sensor fail");
            Timber.w(throwable);
            mBaseViewModel.remeasure.postValue(Resource.error(throwable, null));
            sendMessage(EVT_START_MEASURE_FAIL, new Object[] { throwable });
        }));
    }
*/

}