package com.mediatek.mt6381eco.biz.history.chart;

import android.content.Context;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.Utils;
import com.mediatek.mt6381eco.R;
import java.util.ArrayList;
import timber.log.Timber;

public class CareLineChart extends LineChart
    implements OnChartGestureListener, OnChartValueSelectedListener {
  Context context;

  public CareLineChart(Context context) {
    this(context, null);
  }

  public CareLineChart(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public CareLineChart(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    this.context = context;
    setChart();
  }

  private void setChart() {
    setLegend();
    //setChartMarker();
    setChartSettings();

    //setYAxisRange(200f, 0f);
    //setYAxisLimitLine(150f);
    //ArrayList<Entry> values = new ArrayList<Entry>();
    //for (int i = 0; i < 30; i++) {
    //  float val = (float) (Math.random() * 100) + 3;
    //  values.add(new Entry(i, val, context.getDrawable(R.drawable.star)));
    //}
    //
    //setChartData(values);
    //invalidate();
  }

  private void setChartSettings() {
    //chart.setOnChartGestureListener(this);
    //chart.setOnChartValueSelectedListener(this);
    this.setDrawGridBackground(false);
    // no description text
    this.getDescription().setEnabled(false);
    // enable touch gestures
    this.setTouchEnabled(true);
    // enable scaling and dragging
    this.setDragEnabled(true);
    this.setScaleEnabled(false);
    // if disabled, scaling can be done on x- and y-axis separately
    //this.setPinchZoom(true);
    this.animateX(1000);
    //this.setBackgroundColor(Color.GRAY);
    // this.setScaleXEnabled(true);
    // this.setScaleYEnabled(true);
    //this.getViewPortHandler().setMaximumScaleY(2f);
    //this.getViewPortHandler().setMaximumScaleX(2f);
    //        this.setVisibleXRange(20);
    //        this.setVisibleYRange(20f, AxisDependency.LEFT);
    //        this.centerViewTo(20, 50, AxisDependency.LEFT);
  }

  private void setLegend() {
    Legend l = this.getLegend();
    //l.setForm(Legend.LegendForm.LINE);
    l.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
    l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
    l.setEnabled(false);
  }

  public void setChartData(ArrayList<Entry> values) {

    LineDataSet set1;

    if (this.getData() != null && this.getData().getDataSetCount() > 0) {
      set1 = (LineDataSet) this.getData().getDataSetByIndex(0);
      set1.setValues(values);
      this.getData().notifyDataChanged();
      this.notifyDataSetChanged();
    } else {
      // create a dataset and give it a type
      set1 = new LineDataSet(values, "Systolic");
      set1.setDrawIcons(false);
      setDataLine(set1);

      if (Utils.getSDKInt() >= 18) {
        // fill drawable only supported on api level 18 and above
        Drawable drawable = ContextCompat.getDrawable(context, R.drawable.fade_red);
        set1.setFillDrawable(drawable);
      } else {
        set1.setFillColor(Color.BLACK);
      }
      set1.setFillAlpha(30);

      ArrayList<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
      dataSets.add(set1); // add the datasets

      // create a data object with the datasets
      LineData data = new LineData(dataSets);

      // set data
      this.setData(data);
    }

    invalidate();
  }

  public void setYAxisLimitLine(float yValHigh, String yTitleHigh, float yValLow,
      String yTitleLow) {
    float lineWidth = 2f;
    LimitLine limit_1 = new LimitLine(yValHigh, yTitleHigh);
    limit_1.setLineWidth(lineWidth);
    limit_1.setLineColor(getContext().getResources().getColor(R.color.gs_green));
    limit_1.setTextSize(14f);
    limit_1.setTextColor(getContext().getResources().getColor(R.color.gs_red));
    limit_1.enableDashedLine(10f, 10f, 0f);

    LimitLine limit_2 = new LimitLine(yValLow, yTitleLow);
    limit_2.setLineWidth(lineWidth);
    limit_2.setTextSize(14f);
    limit_2.setTextColor(getContext().getResources().getColor(R.color.gs_red));
    limit_2.setLineColor(getContext().getResources().getColor(R.color.gs_green));
    limit_2.enableDashedLine(10f, 10f, 0f);
    //limit_1.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_TOP);
    //limit_1.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_BOTTOM);

    YAxis leftAxis = this.getAxisLeft();
    leftAxis.removeAllLimitLines(); // reset all limit lines to avoid overlapping lines
    leftAxis.addLimitLine(limit_1);
    leftAxis.addLimitLine(limit_2);
  }

  public void setYAxisRange(float max, float min) {
    YAxis leftAxis = this.getAxisLeft();

    leftAxis.setAxisMaximum(max);
    leftAxis.setAxisMinimum(min);
    //leftAxis.setYOffset(20f);
    leftAxis.enableGridDashedLine(10f, 10f, 0f);
    leftAxis.setDrawZeroLine(false);

    // limit lines are drawn behind data (and not on top)
    leftAxis.setDrawLimitLinesBehindData(true);
    //leftAxis.setEnabled(false);

    //this.getAxisLeft().setEnabled(false);
    leftAxis.setSpaceTop(0);
    //this.getAxisLeft().setSpaceBottom(0);
    this.getAxisRight().setEnabled(false);
    this.getXAxis().setEnabled(false);
    //leftAxis.setEnabled(false);
    this.getAxisLeft().setDrawLabels(false);

    this.getAxisLeft().setDrawGridLines(false);

    leftAxis.setAxisLineColor(Color.alpha(0));
  }

  private void setDataLine(LineDataSet set1) {
    // set the line to be drawn like this "- - - - - -"
    //set1.enableDashedLine(10f, 5f, 0f);
    //set1.enableDashedHighlightLine(10f, 5f, 0f);

    set1.setColor(Color.WHITE);
    set1.setCircleColor(Color.WHITE);
    set1.setHighLightColor(R.color.colorWhite);
    set1.setDrawHighlightIndicators(false);
    set1.setDrawValues(false);
    set1.setValueTextColor(Color.WHITE);
    //set1.setColor(Color.GRAY);
    //set1.setCircleColor(Color.BLACK);
    set1.setLineWidth(2f);
    set1.setCircleRadius(2f);
    set1.setDrawCircleHole(false);
    set1.setValueTextSize(9f);
    set1.setDrawFilled(true);
    //set1.setFillColor(Color.WHITE);
    set1.setFormLineWidth(1f);
    set1.setFormLineDashEffect(new DashPathEffect(new float[] { 10f, 5f }, 0f));
    set1.setFormSize(15.f);
  }

  private void setChartMarker() {
    // create a custom MarkerView (extend MarkerView) and specify the layout
    // to use for it
    MyMarkerView mv = new MyMarkerView(context, R.layout.view_custom_marker);
    mv.setChartView(this); // For bounds control
    this.setMarker(mv); // Set the marker to the chart
  }

  @Override public void onChartGestureStart(MotionEvent me,
      ChartTouchListener.ChartGesture lastPerformedGesture) {
    Timber.i("START, x: %s, y: %s", me.getX() , me.getY());
  }

  @Override public void onChartGestureEnd(MotionEvent me,
      ChartTouchListener.ChartGesture lastPerformedGesture) {
    Timber.i("END, lastGesture: %s" , lastPerformedGesture);

    // un-highlight values after the gesture is finished and no single-tap
    if (lastPerformedGesture != ChartTouchListener.ChartGesture.SINGLE_TAP) {
      this.highlightValues(null); // or highlightTouch(null) for callback to onNothingSelected(...)
    }
  }

  @Override public void onChartLongPressed(MotionEvent me) {
    Timber.i("Chart longpressed.");
  }

  @Override public void onChartDoubleTapped(MotionEvent me) {
    Timber.i("Chart double-tapped.");
  }

  @Override public void onChartSingleTapped(MotionEvent me) {
    Timber.i("Chart single-tapped.");
  }

  @Override
  public void onChartFling(MotionEvent me1, MotionEvent me2, float velocityX, float velocityY) {
    Timber.i("Chart flinged. VeloX: %s, VeloY: %s ", velocityX ,velocityY);
  }

  @Override public void onChartScale(MotionEvent me, float scaleX, float scaleY) {
    Timber.i("ScaleX: %s, ScaleY: %s" , scaleX , scaleY);
  }

  @Override public void onChartTranslate(MotionEvent me, float dX, float dY) {
    Timber.i("dX: %s , dY: %s",dX , dY);
  }

  @Override public void onValueSelected(Entry e, Highlight h) {
    Timber.i("Entry selected: %s", e.toString());
    Timber.i("LOWHIGH: low: %s, high: %s ", this.getLowestVisibleX(), this.getHighestVisibleX());
    Timber.i("MIN MAX, xmin: %s, xmax: %s , ymin: %s, ymax: %s", this.getXChartMin(),
        this.getXChartMax(), this.getYChartMin(), this.getYChartMax());
  }

  @Override public void onNothingSelected() {
    Timber.i("Nothing selected.");
  }
}
