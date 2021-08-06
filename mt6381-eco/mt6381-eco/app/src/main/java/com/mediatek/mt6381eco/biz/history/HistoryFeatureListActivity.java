package com.mediatek.mt6381eco.biz.history;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.mediatek.mt6381eco.R;
import com.mediatek.mt6381eco.biz.historyrecord.HistoryRecordActivity;


import butterknife.ButterKnife;
import butterknife.OnClick;

public class HistoryFeatureListActivity extends AppCompatActivity {

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_history_feature_list);
    ButterKnife.bind(this);
  }

  @Override public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case android.R.id.home: {
        finish();
        break;
      }
    }
    return true;
  }

  @OnClick({ R.id.hr_spo2, R.id.hrv, R.id.bp, R.id.brv, R.id.temp, R.id.measure_record })
  public void onViewClicked(View view) {
    SharedPreferences spflag = getSharedPreferences("isGuest", MODE_APPEND);
    boolean guestFlag = spflag.getBoolean("flag", false);
    switch (view.getId()) {
      case R.id.hr_spo2:
        startActivity(new Intent(this, HRSpO2HistoryActivity.class));
        break;
      case R.id.hrv:
        startActivity(new Intent(this, HRVHistoryActivity.class));
        break;
      case R.id.brv:
        startActivity(new Intent(this, BRVHistoryActivity.class));
        break;
      case R.id.temp:
        if (!guestFlag) {
          startActivity(new Intent(this, TEMPHistoryActivity.class));
        } else {
          startActivity(new Intent(this, LocalTempHistoryActivity.class));
        }
        break;
      case R.id.bp:
        startActivity(new Intent(this, BPHistoryActivity.class));
        break;
      case R.id.measure_record:
        if (!guestFlag) {
          startActivity(new Intent(this, HistoryRecordActivity.class));
        } else {
          startActivity(new Intent(this, OfflineRecordActivity.class));
        }
        break;
    }
  }

  public void destroy() {
    super.onDestroy();
  }
}
