package com.mediatek.mt6381eco.biz.temp;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.text.method.LinkMovementMethod;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import com.mediatek.jni.mt6381.Utils;
import com.mediatek.mt6381eco.BuildConfig;
import com.mediatek.mt6381eco.R;
import com.mediatek.mt6381eco.ui.BaseActivity;
import com.mediatek.mt6381eco.ui.URLSpanNoUnderline;
import java.util.Locale;

public class CountDownActivity extends AppCompatActivity {
  TextView mCountDownData;
  Button mComBtn;
  CountDownTimer mCountDownTimer1;
  private final int countDownDefault = 3000;
  //private static final String DEVICE_NAME_ACTION = BuildConfig.APPLICATION_ID + ".device_name";

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.countdown_data);
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    mCountDownData = findViewById(R.id.tvTime);

    mCountDownTimer1 = new CountDownTimer(countDownDefault, 1000) {
      public void onTick(long millisUntilFinished) {
        mCountDownData.setText("" + (millisUntilFinished / 1000 + 1));
      }
      public void onFinish() {
        mCountDownTimer1.cancel();
        CountDownActivity.this.finish();
      }
    }.start();
  }

  @Override public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case android.R.id.home: {
        if(mCountDownTimer1 != null ){
          mCountDownTimer1.cancel();
        }
        CountDownActivity.this.finish();
        break;
      }
    }
    return true;
  }

  @Override
  protected void onResume() {
    super.onResume();
  }

  protected void onPause() {
    super.onPause();
    if(mCountDownTimer1 != null ){
      mCountDownTimer1.cancel();
    }
    CountDownActivity.this.finish();
  }

  protected void onDestroy() {
    super.onDestroy();
    if(mCountDownTimer1 != null ){
      mCountDownTimer1.cancel();
    }
    CountDownActivity.this.finish();
  }
}
