package com.mediatek.mt6381eco.biz.history;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.widget.ToggleButton;
import android.support.v7.widget.ToggleGroup;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.TextView;
import android.widget.Toast;
import butterknife.BindView;
import butterknife.OnClick;
import com.github.mikephil.charting.data.Entry;
import com.mediatek.mt6381eco.R;
import com.mediatek.mt6381eco.biz.history.chart.CareLineChart;
import com.mediatek.mt6381eco.ui.BaseActivity;
import com.mediatek.mt6381eco.ui.ContextUtils;
import com.mediatek.mt6381eco.utils.MTimeUtils;
import java.util.ArrayList;
import javax.inject.Inject;
import timber.log.Timber;

import static com.mediatek.mt6381eco.biz.history.HistoryPresenter.SPAN_DAY;
import static com.mediatek.mt6381eco.biz.history.HistoryPresenter.SPAN_MONTH;
import static com.mediatek.mt6381eco.biz.history.HistoryPresenter.SPAN_WEEK;

public abstract class HistoryActivity extends BaseActivity implements HistoryContract.View {

  private static final String SP_NAME = "HistorySpan";
  private static final String SPAN = "SPAN";
  protected String columns;
  @Inject HistoryContract.Presenter mPresenter;
  @BindView(R.id.chartTop) CareLineChart chartTop;
  @BindView(R.id.chartBottom) CareLineChart chartBottom;
  @BindView(R.id.button_day) ToggleButton buttonDay;
  @BindView(R.id.button_week) ToggleButton buttonWeek;
  @BindView(R.id.button_month) ToggleButton buttonMonth;
  @BindView(R.id.button_group) ToggleGroup buttonGroup;
  @BindView(R.id.top_legend_left) TextView legendTopLeft;
  @BindView(R.id.top_legend_right) TextView legendTopRight;
  @BindView(R.id.bottom_legend_left) TextView legendBottomLeft;
  @BindView(R.id.bottom_legend_right) TextView legendBottomRight;
  @BindView(R.id.top_chart_y_max) TextView chartTopYMax;
  @BindView(R.id.top_chart_y_min) TextView chartTopYMin;
  @BindView(R.id.bottom_chart_y_max) TextView chartBottomYMax;
  @BindView(R.id.bottom_chart_y_min) TextView chartBottomYMin;
  @BindView(R.id.x_min) TextView chartBottomXMin;
  @BindView(R.id.x_max) TextView chartBottomXMax;
  @BindView(R.id.web_view) WebView webView;
  @BindView(R.id.chartTopView) View chartTopView;
  @BindView(R.id.chartTopChart) View chartTopChart;


  @Inject HistoryViewModel mViewModel;
  private SharedPreferences mSharedPreferences;
  private boolean isLoading;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_history);

    initPage();
    bindUI();

    setToggleButton();
  }

  @Override protected void onDestroy() {
    super.onDestroy();
    mPresenter.destroy();
  }

  public void initPage() {
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    chartTop.setScrollX(30);
    chartBottom.setScrollX(30);
  }

  protected void bindUI() {
    Log.d("tempListData Activity","spuer.bindUI()");
    mViewModel.result.observe(this, resource -> {
      if (resource != null) {
        switch (resource.status) {
          case LOADING: {
            startLoading(getString(R.string.loading));
            break;
          }
          case SUCCESS: {
            stopLoading();
            //for temp : to do by herman
            Log.d("tempListData Activity","resource.data.listData: " + resource.data.listData);
            Log.d("tempListData Activity","resource.data.tempListData: " + resource.data.tempListData);
            if (resource.data.listData == null) {
              Log.d("tempListData Activity","resource.data.listData: " + "listData is null");
            }
            if (resource.data.tempListData == null) {
              Log.d("tempListData Activity","resource.data.tempListData: " + "tempListData is null");
            }

            /*if (!resource.data.listData.isEmpty()) {
              chartBottomXMin.setText(getXTime(resource.data.xMin));
              chartBottomXMax.setText(getXTime(resource.data.xMax));
            }*/
            break;
          }
          case ERROR: {
            Toast.makeText(this, ContextUtils.getErrorMessage(resource.throwable),
                Toast.LENGTH_LONG).show();
            stopLoading();
            break;
          }
        }
      }
    });
  }

  private void setToggleButton() {
    mSharedPreferences = getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);

    switch (mSharedPreferences.getString(SPAN, SPAN_DAY)) {
      case SPAN_DAY:
        buttonDay.performClick();
        break;
      case SPAN_WEEK:
        buttonWeek.performClick();
        break;
      case SPAN_MONTH:
        buttonMonth.performClick();
        break;
    }
  }

  @OnClick(R.id.button_day) public void onBtnDayClicked() {
    toRetrieveMeasurements(SPAN_DAY);
  }

  @OnClick(R.id.button_week) public void onBtnWeekClicked() {
    toRetrieveMeasurements(SPAN_WEEK);
  }

  @OnClick(R.id.button_month) public void onBtnMonthClicked() {
    toRetrieveMeasurements(SPAN_MONTH);
  }

  private void toRetrieveMeasurements(String span) {
    if (isLoading) {
      return;
    }
    isLoading = true;

    Timber.i("span:%s", span);
    mSharedPreferences.edit().putString(SPAN, span).apply();
    mPresenter.requestRetrieveMeasurements(span, columns);
  }

  @Override public void stopLoading() {
    super.stopLoading();
    isLoading = false;
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

  @Override public void setEmptyChart() {
    uiAction(() -> {
      chartTop.clear();
      chartBottom.clear();
      chartBottomXMin.setText("");
      chartBottomXMax.setText("");
    });
  }

  protected void setTopChart(ArrayList<Integer> arrayData) {
    ArrayList<Entry> arrayEntry = new ArrayList<>();
    for (int i = 0; i < arrayData.size(); i++) {
      arrayEntry.add(new Entry(i, arrayData.get(i)));
    }
    chartTop.setChartData(arrayEntry);
  }

  public void setBottomChart(ArrayList<Integer> arrayData) {
    ArrayList<Entry> arrayEntry = new ArrayList<>();
    for (int i = 0; i < arrayData.size(); i++) {
      arrayEntry.add(new Entry(i, arrayData.get(i)));
    }
    chartBottom.setChartData(arrayEntry);
  }

  //add by herman
  public void setBottomChart2(ArrayList<Float> arrayData) {
    ArrayList<Entry> arrayEntry = new ArrayList<>();
    for (int i = 0; i < arrayData.size(); i++) {
      arrayEntry.add(new Entry(i, arrayData.get(i)));
    }
    chartBottom.setChartData(arrayEntry);
  }

  private String getXTime(long timeStamp) {
    if (mSharedPreferences.getString(SPAN, SPAN_DAY).equals(SPAN_DAY)) {
      return MTimeUtils.getTime(timeStamp);
    } else {
      return MTimeUtils.getDateMMdd(timeStamp);
    }
  }
}
