package com.mediatek.mt6381eco.biz.history;

import android.view.View;
import com.mediatek.mt6381eco.R;
import com.mediatek.mt6381eco.biz.utlis.BizUtils;
import com.mediatek.mt6381eco.network.model.MeasureResult;
import java.util.ArrayList;
import java.util.List;

public class BPHistoryActivity extends HistoryActivity {
  private final static int SYSTOLIC_MAX = 220;
  private final static int SYSTOLIC_MIN = 40;
  private final static int SYSTOLIC_LIMIT_MAX = 140;
  private final static int SYSTOLIC_LIMIT_MIN = 90;
  private final static int DIASTOLIC_MAX = 160;
  private final static int DIASTOLIC_MIN = 40;
  private final static int DIASTOLIC_LIMIT_MAX = 90;
  private final static int DIASTOLIC_LIMIT_MIN = 60;

  @Override public void initPage() {
    super.initPage();
    
    legendTopLeft.setText(R.string.systolic_title);
    legendTopLeft.setCompoundDrawablesWithIntrinsicBounds(R.drawable.icon_history_bp, 0, 0, 0);
    //legendTopRight.setVisibility(View.VISIBLE);
    chartTopYMax.setText(SYSTOLIC_MAX + "");
    chartTopYMin.setText(SYSTOLIC_MIN + "");
    chartTop.setYAxisRange(SYSTOLIC_MAX, SYSTOLIC_MIN);
    chartTop.setYAxisLimitLine(SYSTOLIC_LIMIT_MAX, SYSTOLIC_LIMIT_MAX + "", SYSTOLIC_LIMIT_MIN, "");

    legendBottomLeft.setText(R.string.diastolic_title);
    legendBottomLeft.setCompoundDrawablesWithIntrinsicBounds(R.drawable.icon_history_bp, 0, 0, 0);
    //legendBottomRight.setVisibility(View.VISIBLE);
    chartBottomYMax.setText(DIASTOLIC_MAX + "");
    chartBottomYMin.setText(DIASTOLIC_MIN + "");
    chartBottom.setYAxisRange(DIASTOLIC_MAX, DIASTOLIC_MIN);
    chartBottom.setYAxisLimitLine(DIASTOLIC_LIMIT_MAX, DIASTOLIC_LIMIT_MAX + "",
        DIASTOLIC_LIMIT_MIN, "");

    webView.loadUrl(BizUtils.getHtmlFileName("file:///android_asset/htmls/history_bp_%s.html"));

    columns = HistoryPresenter.COLUMNS_BP;
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
                arrayTop.add(measureResult.getSystolic().getValue());
                arrayBottom.add(measureResult.getDiastolic().getValue());
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
