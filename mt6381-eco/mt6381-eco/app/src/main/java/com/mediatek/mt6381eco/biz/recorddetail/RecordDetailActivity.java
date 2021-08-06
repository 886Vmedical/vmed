package com.mediatek.mt6381eco.biz.recorddetail;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import com.mediatek.mt6381eco.R;
import com.mediatek.mt6381eco.utils.MTextUtils;
import java.util.Date;

public class RecordDetailActivity extends AppCompatActivity {
  public static final String TIMESTAMP = "TIMESTAMP";
  public static final String MEASUREMENT_ID = "MEASUREMENT_ID";
  public static final String PROFILE_ID = "PROFILE_ID";
  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_record);
    if(getSupportActionBar() != null) {
      getSupportActionBar().setDisplayHomeAsUpEnabled(true);
      getSupportActionBar().setDisplayShowHomeEnabled(true);
      Date date = (Date) getIntent().getSerializableExtra(TIMESTAMP);
      getSupportActionBar().setTitle(MTextUtils.formatDateTime(date));
    }
  }

  @Override public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()){
      case android.R.id.home:{
        finish();
        break;
      }
      default:
        return false;
    }
    return true;
  }
}
