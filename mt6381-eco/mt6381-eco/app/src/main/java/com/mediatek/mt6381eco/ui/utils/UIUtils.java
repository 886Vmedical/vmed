package com.mediatek.mt6381eco.ui.utils;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.TextView;
import com.mediatek.mt6381eco.utils.MTextUtils;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import timber.log.Timber;

public class UIUtils {

  public static TextView findLabelFor(int id, ViewGroup viewGroup) {
    if (id < 1) return null;
    TextView ret = null;
    for (int i = 0; i < viewGroup.getChildCount(); ++i) {
      View item = viewGroup.getChildAt(i);
      if (item instanceof TextView && item.getLabelFor() == id) {
        return (TextView) item;
      } else if (item instanceof ViewGroup) {
        ret = findLabelFor(id, (ViewGroup) item);
      }
    }
    if (ret == null) {
      ViewParent parent = viewGroup.getParent();
      while (parent != null && !(parent instanceof ViewGroup)) {
        parent = parent.getParent();
      }
      if (parent != null) {
        return findLabelFor(id, (ViewGroup) parent);
      }
    }
    return ret;
  }

  public static void bindDateSelector(Activity context, TextView valueLabel) {
    valueLabel.setOnClickListener(v -> {
      String dateStr = valueLabel.getText().toString();
      if (MTextUtils.isEmpty(dateStr)) {
        dateStr = MTextUtils.formatDate(new Date());
      }
      Calendar calendar = Calendar.getInstance();
      try {
        calendar.setTime(MTextUtils.parseDate(dateStr));
      } catch (ParseException e) {
        calendar.setTime(new Date());
        Timber.w(e, e.getMessage());
      }

      int lastYear = calendar.get(Calendar.YEAR);
      int lastMonthOfYear = calendar.get(Calendar.MONTH);
      int lastDayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

      DatePickerDialog dpd = DatePickerDialog.newInstance(
          (view, year, monthOfYear, dayOfMonth) -> valueLabel.setText(
              MTextUtils.formatDate(year, monthOfYear, dayOfMonth)), lastYear, lastMonthOfYear,
          lastDayOfMonth);

      dpd.show(context.getFragmentManager(), "Datepickerdialog");
    });
  }

  public static int dpToPx(float dp, Context context) {
    return (int) (dp * getDensity(context) + 0.5f);
  }

  public static float getDensity(Context context) {
    return context.getResources().getDisplayMetrics().density;
  }
}
