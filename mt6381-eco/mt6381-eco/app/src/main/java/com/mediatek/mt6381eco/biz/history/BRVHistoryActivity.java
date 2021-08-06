package com.mediatek.mt6381eco.biz.history;

import android.util.Log;
import android.util.Pair;
import android.view.View;
import com.mediatek.mt6381eco.R;
import com.mediatek.mt6381eco.biz.utlis.BizUtils;
import com.mediatek.mt6381eco.network.model.MeasureResult;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;

public class BRVHistoryActivity extends HistoryActivity {
  private final static int BRV_MAX = 60;
  private final static int BRV_MIN = 0;
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

    legendBottomLeft.setText(R.string.brv);
    legendBottomLeft.setCompoundDrawablesWithIntrinsicBounds(R.drawable.icon_history_hrv, 0, 0, 0);
    //legendBottomRight.setVisibility(View.VISIBLE);
    chartBottomYMax.setText(BRV_MAX + "");
    chartBottomYMin.setText(BRV_MIN + "");
    chartBottom.setYAxisRange(BRV_MAX, BRV_MIN);//breath rate value bewteen bottom min and top max


    webView.loadUrl(BizUtils.getHtmlFileName("file:///android_asset/htmls/history_brv_%s.html"));

    columns = HistoryPresenter.COLUMNS_HRV;
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
              //ArrayList<Integer> arrayTop = new ArrayList<>();
              ArrayList<Integer> arrayBottom = new ArrayList<>();
              for (int i = listData.size() - 1; i >= 0; i--) {
                MeasureResult measureResult = listData.get(i);
                int hr = measureResult.getHeartRate().getValue();
                //int spo2 = measureResult.getSpo2().getValue();
                Log.d("HRSpO2HistoryActivity","hr: " + hr);
                //Log.d("HRSpO2HistoryActivity","spo2: " + spo2);
                //arrayTop.add(measureResult.getHeartRate().getValue());
                //arrayBottom.add(measureResult.getSpo2().getValue());
                arrayBottom.add(hr/4);
              }
              //setTopChart(arrayTop);
              setBottomChart(arrayBottom);
            }
            break;
          }
        }
      }
    });
  }
}
