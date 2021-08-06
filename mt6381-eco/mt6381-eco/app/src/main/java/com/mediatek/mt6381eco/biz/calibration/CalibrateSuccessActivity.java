package com.mediatek.mt6381eco.biz.calibration;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.mediatek.mt6381eco.R;

public class CalibrateSuccessActivity extends AppCompatActivity {
  public static final String INTENT_NICK_NAME = "nick_name";
  public static final int RESULT_AGAIN = 1;
  public static final int RESULT_FINISH = 2;
  @BindView(R.id.txt_content) TextView mTxtContent;

  @Override protected void onCreate(Bundle savedInstanceState) {
    Log.d("CalibrateSuccessActivity","onCreate()");
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_calibrate_success_to_calibrate);
    ButterKnife.bind(this);

    String nickName = getIntent().getStringExtra(INTENT_NICK_NAME);
    mTxtContent.setText(
        String.format(getResources().getString(R.string.calibrate_success_to_calibrate), nickName));

    setResult(RESULT_FINISH);
  }

  @OnClick({ R.id.btn_cancel, R.id.btn_next }) public void onClick(View view) {
    switch (view.getId()) {
      case R.id.btn_cancel:
        setResult(RESULT_AGAIN);
        break;
      case R.id.btn_next:
        setResult(RESULT_FINISH);
        break;
    }
    finish();
  }
}
