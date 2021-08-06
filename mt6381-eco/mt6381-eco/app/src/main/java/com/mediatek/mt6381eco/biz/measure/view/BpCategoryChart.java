package com.mediatek.mt6381eco.biz.measure.view;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.mediatek.mt6381eco.R;
import com.mediatek.mt6381eco.ui.widgets.VerticalTextView;
import lombok.AllArgsConstructor;
import lombok.Getter;

public class BpCategoryChart extends RelativeLayout {

  private static final float BP_MIN_X = 40f;
  private static final float BP_MIN_Y = 40f;
  private static final float BP_HYPE_X = 120f;
  private static final float BP_HYPE_Y = 180f;
  @BindView(R.id.txt_axis_y_title) VerticalTextView mTxtAxisYTitle;
  @BindView(R.id.txt_axis_y_label_origin) TextView mTxtAxisYLabelOrigin;
  @BindView(R.id.txt_axis_y_label_hypo) TextView mTxtAxisYLabelHypo;
  @BindView(R.id.txt_axis_y_label_norm) TextView mTxtAxisYLabelNorm;
  @BindView(R.id.txt_axis_y_label_preh) TextView mTxtAxisYLabelPreh;
  @BindView(R.id.txt_axis_y_label_hyper) TextView mTxtAxisYLabelHyper;
  @BindView(R.id.layout_axis_y_label) RelativeLayout mLayoutAxisYLabel;
  @BindView(R.id.txt_axis_x_label_origin) TextView mTxtAxisXLabelOrigin;
  @BindView(R.id.txt_axis_x_label_hypo) TextView mTxtAxisXLabelHypo;
  @BindView(R.id.txt_axis_x_label_norm) TextView mTxtAxisXLabelNorm;
  @BindView(R.id.txt_axis_x_label_preh) TextView mTxtAxisXLabelPreh;
  @BindView(R.id.txt_axis_x_label_hyper) TextView mTxtAxisXLabelHyper;
  @BindView(R.id.layout_axis_x_label) RelativeLayout mLayoutAxisXLabel;
  @BindView(R.id.view_bp_hyper) View mViewBpHyper;
  @BindView(R.id.view_bp_preh) View mViewBpPreh;
  @BindView(R.id.view_bp_norm) View mViewBpNorm;
  @BindView(R.id.view_bp_hypo) View mViewBpHypo;
  @BindView(R.id.layout_chart_inner) RelativeLayout mLayoutChartInner;
  @BindView(R.id.txt_bp_category_hypo) TextView mTxtBpCategoryHypo;
  @BindView(R.id.txt_bp_category_norm) TextView mTxtBpCategoryNorm;
  @BindView(R.id.txt_bp_category_preh) TextView mTxtBpCategoryPreh;
  @BindView(R.id.txt_bp_category_hyper) TextView mTxtBpCategoryHyper;
  @BindView(R.id.img_bp_point) ImageView mImgBpPoint;
  private BloodPressure mBloodPressure;

  public BpCategoryChart(Context context) {
    super(context);
    initViews(context);
  }

  public BpCategoryChart(Context context, AttributeSet attrs) {
    super(context, attrs);
    initViews(context);
  }

  public BpCategoryChart(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    initViews(context);
  }

  public void setBloodPressure(int sys, int dias) {
    Log.d("setBloodPressure Herman","sys: " + sys);//-1
    Log.d("setBloodPressure Herman","dias: " + dias);//8197
    sys = parseSysOutOfRange(sys);
    dias = parseDiasOutOfRange(dias);
    Log.d("setBloodPressure Herman","sys: " + sys);//40
    Log.d("setBloodPressure Herman","dias: " + dias);//120
    mBloodPressure = new BloodPressure(sys, dias);
    invalidate();
  }

  private void initViews(Context context) {
    View view = inflate(getContext(), R.layout.view_chart_bp_category, this);
    ButterKnife.bind(view);
  }

  private int parseDiasOutOfRange(int dias) {
    if (dias < BpCategoryChart.BP_MIN_Y) {
      dias = (int) BpCategoryChart.BP_MIN_Y;
    } else if (dias > BpCategoryChart.BP_HYPE_Y) {
      dias = (int) BpCategoryChart.BP_HYPE_Y;
    }
    Log.d("BpCategoryChart Herman","dias: " + dias);//120
    return dias;
  }

  private int parseSysOutOfRange(int sys) {
    if (sys < BpCategoryChart.BP_MIN_X) {
      sys = (int) BpCategoryChart.BP_MIN_X;
    } else if (sys > BpCategoryChart.BP_HYPE_X) {
      sys = (int) BpCategoryChart.BP_HYPE_X;
    }
    Log.d("BpCategoryChart Herman","sys: " + sys);//40
    return sys;
  }

  @Override protected void dispatchDraw(Canvas canvas) {
    super.dispatchDraw(canvas);

    setAxisXLabelPosition(mTxtAxisXLabelOrigin, 0);
    setAxisXLabelPosition(mTxtAxisXLabelHypo, mViewBpHypo.getWidth());
    setAxisXLabelPosition(mTxtAxisXLabelNorm, mViewBpNorm.getWidth());
    setAxisXLabelPosition(mTxtAxisXLabelPreh, mViewBpPreh.getWidth());
    setAxisXLabelPosition(mTxtAxisXLabelHyper, mViewBpHyper.getWidth());

    setAxisYLabelPosition(mTxtAxisYLabelOrigin, 0);
    setAxisYLabelPosition(mTxtAxisYLabelHypo, mViewBpHypo.getHeight());
    setAxisYLabelPosition(mTxtAxisYLabelNorm, mViewBpNorm.getHeight());
    setAxisYLabelPosition(mTxtAxisYLabelPreh, mViewBpPreh.getHeight());
    setAxisYLabelPosition(mTxtAxisYLabelHyper, mViewBpHyper.getHeight());

    setBpCategoryPosition(mTxtBpCategoryHypo, (int) (mViewBpHypo.getHeight() / 2f));
    setBpCategoryPosition(mTxtBpCategoryNorm,
        mViewBpHypo.getHeight() + (int) ((mViewBpNorm.getHeight() - mViewBpHypo.getHeight()) / 2f));
    setBpCategoryPosition(mTxtBpCategoryPreh,
        mViewBpNorm.getHeight() + (int) ((mViewBpPreh.getHeight() - mViewBpNorm.getHeight()) / 2f));
    setBpCategoryPosition(mTxtBpCategoryHyper,
        mViewBpPreh.getHeight() + (int) ((mViewBpHyper.getHeight() - mViewBpPreh.getHeight())
            / 2f));

    setBpPointPosition(canvas);
  }

  private void setAxisXLabelPosition(TextView textView, int subBarWidth) {
    LayoutParams p = (LayoutParams) textView.getLayoutParams();
    p.leftMargin = mLayoutAxisYLabel.getWidth() - (int) (textView.getWidth() / 2f) + subBarWidth;
    textView.setLayoutParams(p);
  }

  private void setAxisYLabelPosition(TextView textView, int subBarHeight) {
    LayoutParams p = (LayoutParams) textView.getLayoutParams();
    p.bottomMargin =
        mLayoutAxisXLabel.getHeight() - (int) (textView.getHeight() / 2f) + subBarHeight;
    textView.setLayoutParams(p);
  }

  private void setBpCategoryPosition(TextView textView, int subBarOffset) {
    LayoutParams p = (LayoutParams) textView.getLayoutParams();
    p.bottomMargin =
        mLayoutAxisXLabel.getHeight() - (int) (textView.getHeight() / 2f) + subBarOffset;
    textView.setLayoutParams(p);
  }

  private void setBpPointPosition(Canvas canvas) {
    final Point originPoint = new Point(
        mTxtAxisYTitle.getPaddingLeft() + mTxtAxisYTitle.getWidth() + mLayoutAxisYLabel.getWidth(),
        mLayoutChartInner.getBottom());
    final Point bpPoint = getBpPointInside(mBloodPressure, mLayoutChartInner);

    LayoutParams p = (LayoutParams) mImgBpPoint.getLayoutParams();
    p.bottomMargin =
        (int) ((canvas.getHeight() - originPoint.y) + bpPoint.y - mImgBpPoint.getWidth() / 2f) -10;
    p.leftMargin = (int) (originPoint.x + bpPoint.x - mImgBpPoint.getWidth() / 2f);
    mImgBpPoint.setLayoutParams(p);
  }

  private Point getBpPointInside(BloodPressure bp, View chart) {
    final float deltaFullSys = BpCategoryChart.BP_HYPE_X - BpCategoryChart.BP_MIN_X;
    final float deltaFullDias = BpCategoryChart.BP_HYPE_Y - BpCategoryChart.BP_MIN_Y;

    final float deltaSys = bp.getSystolic() - BpCategoryChart.BP_MIN_X;
    final float deltaDias = bp.getDiastolic() - BpCategoryChart.BP_MIN_Y;

    final float sysPointPx = (deltaSys / deltaFullSys) * chart.getWidth();
    final float diasPointPx = (deltaDias / deltaFullDias) * chart.getHeight();

    return new Point(sysPointPx, diasPointPx);
  }

  @AllArgsConstructor private class Point {
    public float x;
    public float y;
  }

  @AllArgsConstructor @Getter private class BloodPressure {
    private int systolic;
    private int diastolic;
  }
}