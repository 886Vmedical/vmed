package com.mediatek.mt6381eco.biz.history;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.mediatek.mt6381eco.R;
import com.mediatek.mt6381eco.db.TempDataBaseOperation;
import java.util.ArrayList;
import java.util.List;


public class LocalTempHistoryActivity extends AppCompatActivity {

  ListView tempListView;
  private List<String> tempRecordsList;
  private List<String> dateRecordsList;
  private List<String> tpList;//临时列表
  private List<String> dtList;//临时列表

  private TempDataBaseOperation mtpDBOperation;



  @Override
  public void onCreate(Bundle savedInstanceState){
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_temp_history_record);
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    initTempView();
  }

  public void initTempView(){
    tempListView = (ListView)findViewById(R.id.list_tempview);

    mtpDBOperation = new TempDataBaseOperation(this,"tempDT");
    tempRecordsList = new ArrayList<>();
    dateRecordsList = new ArrayList<>();
    tpList = new ArrayList<>();
    dtList = new ArrayList<>();
    tpList.addAll(mtpDBOperation.getRecordsList());
    dtList.addAll(mtpDBOperation.getDateList());
    reversedList();
    reversedDateList();
    final TempRecordAdapter adapteter = new TempRecordAdapter(this,dateRecordsList, tempRecordsList);
    tempListView.setAdapter(adapteter);
    if(adapteter.isEmpty() == true){
      tempListView.setEmptyView(findViewById(R.id.empty_imageview));
    }
    adapteter.notifyDataSetChanged();
  }

  private void reversedList(){
    tempRecordsList.clear();
    for(int i = tpList.size() - 1 ; i >= 0 ; i --){
      tempRecordsList.add(tpList.get(i));
    }
  }

  private void reversedDateList(){
    dateRecordsList.clear();
    for(int j = dtList.size() - 1 ; j >= 0 ; j --){
      dateRecordsList.add(dtList.get(j));
    }
  }

  @Override
  public void onBackPressed() {
    setResult(Activity.RESULT_CANCELED);
    finish();
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case android.R.id.home:
        onBackPressed();
        break;
    }
    return true;
  }

}
