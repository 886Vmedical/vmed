package com.mediatek.mt6381eco.biz.calibration;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.mediatek.mt6381eco.R;

public class CalibrateSuccess2MeasureActivity extends AppCompatActivity {
  public static final String INTENT_NICK_NAME = "nick_name";
  @BindView(R.id.txt_content) TextView mTxtContent;

  @Override protected void onCreate(Bundle savedInstanceState) {
    Log.d("CalibrateSuccess2MeasureActivity","onCreate()");
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_calibrate_success_to_measure);
    ButterKnife.bind(this);
    setResult(Activity.RESULT_CANCELED);
    String nickName = getIntent().getStringExtra(INTENT_NICK_NAME);
    mTxtContent.setText(getString(R.string.calibrate_success_to_measure, nickName));
  }

  @OnClick({ R.id.btn_cancel, R.id.btn_next }) public void onClick(View view) {
    switch (view.getId()) {
      case R.id.btn_cancel:
        setResult(Activity.RESULT_CANCELED);
        break;
      case R.id.btn_next:
        setResult(Activity.RESULT_OK);
        break;
    }
    finish();
  }

}