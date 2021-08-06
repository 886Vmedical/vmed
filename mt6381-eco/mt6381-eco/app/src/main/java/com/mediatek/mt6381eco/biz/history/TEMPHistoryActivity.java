package com.mediatek.mt6381eco.biz.history;

import android.util.Log;
import android.util.Pair;
import android.view.View;
import com.mediatek.mt6381eco.R;
import com.mediatek.mt6381eco.biz.utlis.BizUtils;
import com.mediatek.mt6381eco.network.model.TemperatureResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

public class TEMPHistoryActivity extends HistoryActivity {
  private final static int TEMPF_MAX = 185;
  private final static int TEMPF_MIN = 32;
  private final static int TEMPC_MAX = 85;
  private final static int TEMPC_MIN = 0;

  @Inject HistoryViewModel mViewModel;

  @Override public void initPage() {
    super.initPage();
    /*legendTopLeft.setText(R.string.fatigue);
    legendTopLeft.setCompoundDrawablesWithIntrinsicBounds(R.drawable.icon_history_hrv, 0, 0, 0);
    legendTopRight.setVisibility(View.VISIBLE);
    chartTop.setYAxisRange(BRV_MAX, BRV_MIN);
    chartTopYMax.setText(R.string.high);
    chartTopYMin.setText(R.string.low);*/

    legendTopLeft.setVisibility(View.GONE);
    chartTopView.setVisibility(View.GONE);
    chartTopChart.setVisibility(View.GONE);
    chartTop.setVisibility(View.GONE);
    chartTopYMax.setVisibility(View.GONE);
    chartTopYMin.setVisibility(View.GONE);

    legendBottomLeft.setText(R.string.string_temp);
    legendBottomLeft.setCompoundDrawablesWithIntrinsicBounds(R.drawable.icon_history_hrv, 0, 0, 0);
    //legendBottomRight.setVisibility(View.VISIBLE);
    if(isChinese()){
      chartBottomYMax.setText(TEMPC_MAX + "℃");
      chartBottomYMin.setText(TEMPC_MIN + "℃");
      chartBottom.setYAxisRange(TEMPC_MAX, TEMPC_MIN);
    }else{
      chartBottomYMax.setText(TEMPF_MAX + "℉");
      chartBottomYMin.setText(TEMPF_MIN + "℉");
      chartBottom.setYAxisRange(TEMPF_MAX, TEMPF_MIN);
    }

    webView.loadUrl(BizUtils.getHtmlFileName("file:///android_asset/htmls/history_temp_%s.html"));

    columns = HistoryPresenter.COLUMNS_TEMPERATURE;

  }

  @Override protected void bindUI() {
    Log.d("tempListData","bindUI()");
    super.bindUI();
    //add by herman
    mViewModel.result.observe(this, resource -> {
      if (resource != null) {
        switch (resource.status) {
          case SUCCESS: {
            if (resource.data.tempListData.isEmpty()) {
              Log.d("tempListData","tempListData is empty");
              setEmptyChart();
            } else {
              List<TemperatureResult> tempListData = resource.data.tempListData;
              Log.d("tempListData","tempListData.size: " + tempListData.size());
              //ArrayList<Integer> arrayTop = new ArrayList<>();
              ArrayList<Float> arrayBottom = new ArrayList<>();
              for (int i = tempListData.size() - 1; i >= 0; i--) {
                TemperatureResult mTemperatureResult = tempListData.get(i);
                float temperatureF = mTemperatureResult.getTemperature();
                Log.d("TEMPHistoryActivity","temperatureF: " + temperatureF);
                //中文：
                float temperatureC = (float) ((temperatureF-32)/1.8);
                Log.d("TEMPHistoryActivity","temperatureC: " + temperatureC);
                //int hr = mTemperatureResult.getHeartRate().getValue();
                //arrayTop.add(mTemperatureResult.getHeartRate().getValue());
                if(isChinese()){
                  arrayBottom.add(temperatureC);
                }else{
                  arrayBottom.add(temperatureF);
                }
              }
              //setTopChart(arrayTop);
              setBottomChart2(arrayBottom);
            }
            break;
          }
        }
      }
    });
  }

  public boolean isChinese() {
    Locale locale = Locale.getDefault();
    String language = locale.getLanguage();
    //String country = locale.getCountry().toLowerCase();
    //language.endsWith("zh")
    return "zh".equals(language);
  }

}
