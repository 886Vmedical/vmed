package com.mediatek.mt6381eco.biz.history;

import android.util.Pair;
import android.view.View;
import com.mediatek.mt6381eco.R;
import com.mediatek.mt6381eco.biz.utlis.BizUtils;
import com.mediatek.mt6381eco.network.model.MeasureResult;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;

public class HRVHistoryActivity extends HistoryActivity {
  private final static int HRV_MAX = 100;
  private final static int HRV_MIN = 0;
  @Inject HistoryViewModel mViewModel;

  @Override public void initPage() {
    super.initPage();
    legendTopLeft.setText(R.string.fatigue);
    legendTopLeft.setCompoundDrawablesWithIntrinsicBounds(R.drawable.icon_history_hrv, 0, 0, 0);
    //legendTopRight.setVisibility(View.VISIBLE);
    chartTopYMax.setText(HRV_MAX + "");
    chartTopYMin.setText(HRV_MIN + "");
    chartTop.setYAxisRange(HRV_MAX, HRV_MIN);
    //chartTopYMax.setText(R.string.high);
    //chartTopYMin.setText(R.string.low);

    legendBottomLeft.setText(R.string.pressure);
    legendBottomLeft.setCompoundDrawablesWithIntrinsicBounds(R.drawable.icon_history_hrv, 0, 0, 0);
    //legendBottomRight.setVisibility(View.VISIBLE);
    chartBottomYMax.setText(HRV_MAX + "");
    chartBottomYMin.setText(HRV_MIN + "");
    chartBottom.setYAxisRange(HRV_MAX, HRV_MIN);
    //chartBottomYMax.setText(R.string.high);
    //chartBottomYMin.setText(R.string.low);

    webView.loadUrl(BizUtils.getHtmlFileName("file:///android_asset/htmls/history_hrv_%s.html"));

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
              ArrayList<Integer> arrayTop = new ArrayList<>();
              ArrayList<Integer> arrayBottom = new ArrayList<>();
              for (int i = listData.size() - 1; i >= 0; i--) {
                MeasureResult measureResult = listData.get(i);
                arrayTop.add(measureResult.getFatigue().getValue());
                arrayBottom.add(measureResult.getPressure().getValue());
              }
              setTopChart(arrayTop);
              setBottomChart(arrayBottom);
            }

            setTopYAxisLimitLine(resource.data.yTopHighLow);
            setBottomYAxisLimitLine(resource.data.yBottomHighLow);
            break;
          }
        }
      }
    });
  }

  private void setTopYAxisLimitLine(Pair<Float, Float> pair) {
    chartTop.setYAxisLimitLine(pair.first, "", pair.second, "");
  }

  private void setBottomYAxisLimitLine(Pair<Float, Float> pair) {
    chartBottom.setYAxisLimitLine(pair.first, "", pair.second, "");
  }
}
