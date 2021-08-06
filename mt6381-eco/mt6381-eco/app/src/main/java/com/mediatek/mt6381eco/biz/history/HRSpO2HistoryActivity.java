package com.mediatek.mt6381eco.biz.history;

import android.util.Log;

import com.mediatek.mt6381eco.R;
import com.mediatek.mt6381eco.biz.utlis.BizUtils;
import com.mediatek.mt6381eco.network.model.MeasureResult;
import java.util.ArrayList;
import java.util.List;

public class HRSpO2HistoryActivity extends HistoryActivity {
  private final static int HR_MAX = 140;
  private final static int HR_MIN = 20;
  private final static int SPO2_MAX = 100;
  private final static int SPO2_MIN = 75;

  @Override public void initPage() {
    super.initPage();
    legendTopLeft.setCompoundDrawablesWithIntrinsicBounds(R.drawable.icon_history_hr, 0, 0, 0);
    legendTopLeft.setText(R.string.heart_rate_title);
    chartTopYMax.setText(HR_MAX + "");
    chartTopYMin.setText(HR_MIN + "");
    chartTop.setYAxisRange(HR_MAX, HR_MIN);

    legendBottomLeft.setCompoundDrawablesWithIntrinsicBounds(R.drawable.icon_history_spo2, 0, 0, 0);
    legendBottomLeft.setText(R.string.spo2_title);
    chartBottomYMax.setText(SPO2_MAX + "");
    chartBottomYMin.setText(SPO2_MIN + "");
    chartBottom.setYAxisRange(SPO2_MAX, SPO2_MIN);

    webView.loadUrl(
        BizUtils.getHtmlFileName("file:///android_asset/htmls/history_hr_spo2_%s.html"));

    columns = HistoryPresenter.COLUMNS_HR_SPO2;
  }

  @Override protected void bindUI() {
    super.bindUI();

    mViewModel.result.observe(this, resource -> {
      if (resource != null) {
        switch (resource.status) {
          case SUCCESS: {
            if (resource.data.listData.isEmpty()) {
              setEmptyChart();
            } else {
              List<MeasureResult> listData = resource.data.listData;
              ArrayList<Integer> arrayTop = new ArrayList<>();
              ArrayList<Integer> arrayBottom = new ArrayList<>();
              for (int i = listData.size() - 1; i >= 0; i--) {
                MeasureResult measureResult = listData.get(i);
                int hr = measureResult.getHeartRate().getValue();
                int spo2 = measureResult.getSpo2().getValue();
                Log.d("HRSpO2HistoryActivity", "hr: " + hr);
                Log.d("HRSpO2HistoryActivity", "spo2: " + spo2);
                arrayTop.add(measureResult.getHeartRate().getValue());
                arrayBottom.add(measureResult.getSpo2().getValue());
              }
              setTopChart(arrayTop);
              setBottomChart(arrayBottom);
            }
            break;
          }
        }
      }
    });
  }
}
