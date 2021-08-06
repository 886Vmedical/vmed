package com.mediatek.mt6381eco.biz.profile;

import android.Manifest;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.OnClick;
import butterknife.OnTextChanged;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.mediatek.mt6381eco.R;
import com.mediatek.mt6381eco.biz.viewmodel.AppViewModel;
import com.mediatek.mt6381eco.ui.BaseActivity;
import com.mediatek.mt6381eco.ui.dialogs.NumberPickDialog;
import com.mediatek.mt6381eco.ui.widgets.MutableLiveDataTextView;
import com.mediatek.mt6381eco.utils.DataUtils;
import com.mediatek.mt6381eco.utils.MTextUtils;
import com.mobsandgeeks.saripaar.annotation.NotEmpty;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import io.reactivex.Completable;
import io.reactivex.android.schedulers.AndroidSchedulers;

import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicBoolean;

import com.wdullaer.materialdatetimepicker.date.DateRangeLimiter;
import javax.inject.Inject;

import static android.content.Context.ALARM_SERVICE;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class ProfileActivity extends BaseActivity
    implements ProfileContract.View, DatePickerDialog.OnDateSetListener {
  public static final String EXTRA_NEXT = "NEXT";
  private static final int DEFAULT_HEIGHT = 170;
  private static final int DEFAULT_INCH= (int)(170 * 0.3937008);
  private static final int MAX_HEIGHT = 250;
  private static final int MAX_INCH= 150;
  private static final int DEFAULT_WEIGHT = 100;
  private static final int MAX_WEIGHT = 400;
  public static final String MEDICINE_TIME_ACTION = "com.mediatek.mt6381eco.biz.profile.MEDICINE_TIME_ACTION";
  private static final int numMed = 1;
  DateRangeLimiter mDateRange;
  public int medHour = -1;

  @Inject ProfileContract.Presenter mPresenter;
  @Inject ProfileViewModel mViewModel;
  //add by herman for sb guest
  @Inject AppViewModel mAppViewModel;
  @NotEmpty(messageResId = R.string.error_nick_name_required) @BindView(R.id.edt_nick_name) EditText
      mEdtNickName;
  @NotEmpty(messageResId = R.string.error_gender_required) @BindView(R.id.txt_gender)
  MutableLiveDataTextView mTxtGender;
  @NotEmpty(messageResId = R.string.error_birthday_required) @BindView(R.id.txt_birthday)
  MutableLiveDataTextView mTxtBirthday;
  @NotEmpty(messageResId = R.string.error_height_required) @BindView(R.id.txt_height)
  MutableLiveDataTextView mTxtHeight;
  @NotEmpty(messageResId = R.string.error_weight_required) @BindView(R.id.txt_weight)
  MutableLiveDataTextView mTxtWeight;
  @NotEmpty(emptyTextResId = R.string.default_select, messageResId = R.string.error_personal_status_required)
  @BindView(R.id.txt_personal_status) MutableLiveDataTextView mTxtPersonalStatus;
  @BindView(R.id.txt_take_medicine_time) MutableLiveDataTextView mTxtTakeMedicineTime;
  @BindView(R.id.layout_take_medicine_time) LinearLayout mViewTakeMedicineTime;
  @BindView(R.id.btn_submit) Button mBtnSubmit;
  @BindView(R.id.txt_calibrated) TextView mTxtCalibrated;
  @BindView(R.id.scrollView) ScrollView mScrollView;
  Calendar mCalendar;

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_profile);
    bindUI();
    DatePickerDialog fragment =
        (DatePickerDialog) getFragmentManager().findFragmentByTag("Datepickerdialog");
    if (fragment != null) {

      fragment.setOnDateSetListener(this);
    }
  }

  @Override protected void onDestroy() {
    super.onDestroy();
    mPresenter.destroy();
  }

  @Override public void onAttachedToWindow() {
    super.onAttachedToWindow();
    mBtnSubmit.setText(R.string.save);
    if (!validate(false)) {
      mBtnSubmit.setText(R.string.complete);
      mBtnSubmit.setEnabled(false);
    } else {
      getSupportActionBar().setDisplayHomeAsUpEnabled(true);
      getSupportActionBar().setDisplayShowHomeEnabled(true);
    }
    mBtnSubmit.setVisibility(View.VISIBLE);
  }

  @Override public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case android.R.id.home: {
        attentionShow();
        break;
      }
    }
    return true;
  }

  private void bindUI() {
    bindString(mEdtNickName, mViewModel.getNickName());
    bindGender();
    mTxtBirthday.setLiveData(this, mViewModel.getBirthday(),
        data -> data == null ? "" : MTextUtils.formatDate(data));
    if(isChinese()){
    mTxtHeight.setLiveData(this, mViewModel.getHeight(),
            data -> data == null ? "" : String.format(Locale.CHINA, "%d cm", data));
    }else{
      mTxtHeight.setLiveData(this, mViewModel.getHeight(),
              data -> data == null ? "" : String.format(Locale.US, "%d inch", data));
    }
    mTxtWeight.setLiveData(this, mViewModel.getWeight(), data -> data == null ? ""
            : String.format(Locale.US, "%d %s", data.getValue(), data.getUnit()));
    mViewModel.isCalibrated.observe(this, isCalibrated -> mTxtCalibrated.setVisibility(
        Boolean.TRUE.equals(isCalibrated) ? View.VISIBLE : View.GONE));
    bindPersonalStatus();
    bindTakeMedicineTime();
  }

  @OnClick(R.id.layout_birthday) void onLayoutBirthdayClick() {
    Calendar calendar = Calendar.getInstance();
    int lastYear = calendar.get(Calendar.YEAR);
    int lastMonthOfYear = calendar.get(Calendar.MONTH);
    int lastDayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

    if (mViewModel.getBirthday().getValue() != null) {
      calendar.setTime(mViewModel.getBirthday().getValue());
      lastYear = calendar.get(Calendar.YEAR);
      if(lastYear > 2020) {
        lastYear = 2020;
      }else if(lastYear <= 1920){
        //lastYear = calendar.get(Calendar.YEAR);
        lastYear = 1920;
      }
      lastMonthOfYear = calendar.get(Calendar.MONTH);
      lastDayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
    }
    DatePickerDialog dpd =
        DatePickerDialog.newInstance(this, lastYear, lastMonthOfYear, lastDayOfMonth);
    dpd.show(getFragmentManager(), "Datepickerdialog");
  }

  @OnClick(R.id.layout_height)
  void onLayoutHeightClick() {
    if (!isChinese()) {
      Toast.makeText(this, R.string.unit_warning, Toast.LENGTH_LONG).show();
      int defaultValue = (int) (DEFAULT_HEIGHT * 0.3937008);
      String defaultUnit = "inch";

      if (mViewModel.getHeight().getValue() != null) {
        defaultValue = mViewModel.getHeight().getValue();
        Log.d("onLayoutHeightClick", "defaultUnit =" + defaultUnit);
      }
      NumberPickDialog.Builder builder = new NumberPickDialog.Builder(this);
      builder.iconRes(R.drawable.icon_profile_height)
              .title(R.string.height_title)
              .minValue(1)
              .maxValue(MAX_HEIGHT)
              .value(defaultValue)
              .units(new String[]{getString(R.string.inch)})
              .unit(defaultUnit);
      String[] unitArr = getResources().getStringArray(R.array.height_units);
      int unitIndex = DataUtils.indexOf(unitArr, getString(R.string.cm));
      builder.callBack((value, unit) -> mViewModel.getHeight().setValue((value))).show();
    } else {
      int defaultValue = DEFAULT_HEIGHT;
      String defaultUnit = "cm";
      if (mViewModel.getHeight().getValue() != null) {
        defaultValue = mViewModel.getHeight().getValue();
        Log.d("onLayoutHeightClick", "defaultUnit =" + defaultUnit);
      }
      new NumberPickDialog.Builder(this).iconRes(R.drawable.icon_profile_height)
              .title(R.string.height_title)
              .minValue(1)
              .maxValue(MAX_HEIGHT)
              .value(defaultValue)
              .units(new String[]{getString(R.string.unit_cm)})
              .unit(defaultUnit)
              .callBack((value, unit) -> mViewModel.getHeight().setValue(value))
              .show();
    }
  }

  @OnClick(R.id.layout_weight) void onLayoutWeightClick() {
    int defaultValue = DEFAULT_WEIGHT;
    String defaultUnit = "kg";
    if (mViewModel.getWeight().getValue() != null) {
      defaultValue = mViewModel.getWeight().getValue().getValue();
      defaultUnit = mViewModel.getWeight().getValue().getUnit();
      Log.d("onLayoutWeightClick","weightUnit ="  + defaultUnit);
    }
    new NumberPickDialog.Builder(this).iconRes(R.drawable.icon_profile_weight)
        .title(R.string.weight_title)
        .minValue(1)
        .maxValue(MAX_WEIGHT)
        .value(defaultValue)
        .units(getResources().getStringArray(R.array.weight_units))
        .unit(defaultUnit)
        .callBack((value, unit) -> mViewModel.getWeight().setValue(new ValueUnit(value, unit)))
        .show();
  }

  private void bindGender() {
    String[] gender_label = getResources().getStringArray(R.array.gender_label);
    mTxtGender.setLiveData(this, mViewModel.getGender(), data -> {
      if (data == null) return "";
      //modify by herman
      if (data >=0) {
        return gender_label[data];
      }
      return "";
    });
  }

  @OnClick(R.id.layout_gender) void onTxtGenderClick() {
    String[] gender_label = getResources().getStringArray(R.array.gender_label);
    int index = DataUtils.indexOf(gender_label, mTxtGender.getText().toString());
    new MaterialDialog.Builder(this).iconRes(R.drawable.icon_profile_gender)
        .title(R.string.gender)
        .items(gender_label)
        .itemsCallbackSingleChoice(index, (dialog, itemView, which, text) -> {
          mViewModel.getGender().setValue(which);
          return true;
        })
        .show();
  }

  private void bindPersonalStatus() {
    String[] personal_status = getResources().getStringArray(R.array.personal_status);
    mTxtPersonalStatus.setLiveData(this, mViewModel.getPersonalStatus(), data -> {
      String defaultText = getString(R.string.default_select);
      if (data == null) return defaultText;
      if (data > -1) {
        return personal_status[data];
      }
      return defaultText;
    });

    mViewModel.getPersonalStatus().observe(this, data -> {
      boolean chooseNone = (data == ProfileViewModel.PERSONAL_STATUS_NONE);
      mViewTakeMedicineTime.setVisibility(chooseNone ? View.GONE : View.VISIBLE);
      if (chooseNone) {
        clearMedicineTime();
      }
    });
  }

  @OnClick(R.id.layout_personal_status) void onTxtPersonalStatusClick() {
    String[] personal_status = getResources().getStringArray(R.array.personal_status);//00:00~00:59
    int index = DataUtils.indexOf(personal_status, mTxtPersonalStatus.getText().toString());
    new MaterialDialog.Builder(this).iconRes(R.drawable.icon_profile_medicine)
        .title(R.string.personal_status_hint)
        .items(personal_status)
        .itemsCallbackSingleChoice(index, (dialog, itemView, which, text) -> {
          mViewModel.getPersonalStatus().setValue(which);
          if (index == 0 && which > 0) {
            Completable.complete().observeOn(AndroidSchedulers.mainThread()).subscribe(() -> {
              mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
            });
          }
          return true;
        })
        .show();
  }

  private void bindTakeMedicineTime() {
    String[] takeMedicineTimeArray = getTakeMedicineTimeArray();
    mTxtTakeMedicineTime.setLiveData(this, mViewModel.getTakeMedicineTime(), data -> {
      String defaultText = getString(R.string.default_select);
      if (data == null) return defaultText;
      if (data > -1) {
        return takeMedicineTimeArray[data];
      }
      return defaultText;
    });
  }

  @OnClick(R.id.layout_take_medicine_time) void onTxtMedicineTimeClick() {
    String[] medicineTimeArray = getTakeMedicineTimeArray();
    int index = DataUtils.indexOf(medicineTimeArray, mTxtTakeMedicineTime.getText().toString());
    Log.d("ProfileActivity","ProfIndex: " + index);
    new MaterialDialog.Builder(this).iconRes(R.drawable.icon_profile_medicine)
        .title(R.string.take_medicine_time_hint)
        .items(getTakeMedicineTimeArray())
        .itemsCallbackSingleChoice(index, (dialog, itemView, which, text) -> {
          mViewModel.getTakeMedicineTime().setValue(which == -1 ? null : which);
          medHour = which;
          addNotification();
          Log.d("ProfileActivity","WhichHour: " + medHour);
          return true;
        })
        .positiveText(R.string.ok)
        .negativeText(R.string.clear)
        .onPositive((dialog, which) -> {

         })
        .onNegative((dialog, which) -> {
          clearMedicineTime();
          //krestin modify can not cancel medicine time bug start
          mViewModel.getPersonalStatus().setValue(ProfileViewModel.PERSONAL_STATUS_NONE);
          mViewTakeMedicineTime.setVisibility(View.GONE);
          //krestin modify can not cancel medicine time bug end
        })
        .show();
  }

  private String[] getTakeMedicineTimeArray() {
    String[] medicineTimeArray = new String[24];
    for (int i = 0; i < 24; i++) {
      String item = String.format("0%d", i);
      item = item.substring(item.length() - 2);
      medicineTimeArray[i] = String.format("%s:00~%s:59", item, item);
    }
    return medicineTimeArray;
  }

  private void clearMedicineTime() {
    mViewModel.getTakeMedicineTime().setValue(null);
  }

  @OnClick(R.id.btn_submit) void onBtnSubmitClick() {
    if (validate()) {
      postUiChange();
      //这里需要判断，是否为sb访客模式。
      boolean isGuest =
              mAppViewModel.account.getValue() != null && mAppViewModel.account.getValue().isGuest;
      Log.d("ProfileActivity", "isGuest: " + isGuest);
      if (isGuest) {
        mPresenter.requestSaveProfileForSB();
      } else {
        //krestin modify medicine no time but can be saved bug start
        if (!(mTxtPersonalStatus.getText().toString().equals(getString(R.string.personal_status_none)))
                && (mTxtTakeMedicineTime.getText().toString().equals(getString(R.string.default_select)))) {
          Toast.makeText(this, R.string.no_medicine_choice, Toast.LENGTH_LONG).show();
        } else {
          subscribe(R.string.saving, mPresenter.requestSaveProfile());
        }
        //krestin modify medicine no time but can be saved bug end
      }
    }
  }

  @OnTextChanged({
      R.id.edt_nick_name, R.id.txt_gender, R.id.txt_birthday, R.id.txt_height, R.id.txt_weight,
      R.id.txt_personal_status
  }) void onValueChange() {
    mBtnSubmit.setEnabled(validate(false));
  }

  @Override public void navToNext() {
    uiAction(() -> {
      setResult(Activity.RESULT_OK);
      finish();
    });
  }

  @Override
  protected void onResume() {
    super.onResume();
  }

  public void attentionShow() {
    MaterialDialog.Builder builder = new MaterialDialog.Builder(this);
    if ((mEdtNickName.getEditableText().toString()).equals("")
            || mViewModel.getHeight().getValue() == null
            || mViewModel.getBirthday().getValue() == null
            || (mTxtPersonalStatus.getText().toString().equals("Select"))
            || mViewModel.getWeight().getValue() == null
            || mTxtGender.getText().toString().equals("")) {
      builder.title(R.string.attention)
              .content(R.string.attention_fill_info)
              .negativeText(R.string.no)
              .onNegative(new MaterialDialog.SingleButtonCallback() {
                @Override
                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                }
              })
              .positiveText(R.string.measure_interrupt_yes)
              .onPositive(new MaterialDialog.SingleButtonCallback() {
                @Override
                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                  setResult(Activity.RESULT_CANCELED);
                  finish();
                }
              });
    } else {
      builder.title(R.string.attention)
              .content(R.string.attention_save)
              .negativeText(R.string.no)
              .onNegative(new MaterialDialog.SingleButtonCallback() {
                @Override
                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                }
              })
              .positiveText(R.string.measure_interrupt_yes)
              .onPositive(new MaterialDialog.SingleButtonCallback() {
                @Override
                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                  setResult(Activity.RESULT_CANCELED);
                  finish();
                }
              });
    }
    builder.show();
  }

  @Override public void onBackPressed() {
    attentionShow();
  }

  @Override
  public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
    DatePickerDialog mDpk = new DatePickerDialog();
    int myear;
    Calendar calendar = Calendar.getInstance();
    if(year > 2020){
      myear = 2020;
      //calendar.set(Calendar.YEAR, 2008);
      //calendar.set(Calendar.MONTH, 1);
      //calendar.set(Calendar.DAY_OF_MONTH, 1);
      //mDatePicker.setMaxDate(calendar.getTimeInMillis());
    }else{
      //myear = calendar.get(Calendar.YEAR);
      myear =year;
    }
    mDpk.setMaxDate(calendar);
    calendar.set(myear, monthOfYear, dayOfMonth);
    mViewModel.getBirthday().setValue(calendar.getTime());
  }

  public void  addNotification(){
    String strMedTime = String.valueOf(medHour);
    mCalendar = Calendar.getInstance();
    mCalendar.setTimeInMillis(System.currentTimeMillis());
    long systemTime = System.currentTimeMillis();

    mCalendar.setTimeInMillis(System.currentTimeMillis());
    mCalendar.setTimeZone(TimeZone.getTimeZone("GMT+8"));
    Log.d("ProfileActivity","HourTime: " + medHour);
    if(medHour != -1) {
      mCalendar.set(Calendar.HOUR_OF_DAY, medHour);
      mCalendar.set(Calendar.MINUTE, 0);
      mCalendar.set(Calendar.SECOND, 0);
      mCalendar.set(Calendar.MILLISECOND, 0);

      long selectTime = mCalendar.getTimeInMillis();
      if (systemTime > selectTime) {
        mCalendar.add(Calendar.DAY_OF_MONTH, 1);
      }
      Intent intent = new Intent(this, MedicineTimeAlarmReceiver.class);
      intent.putExtra("medTime", strMedTime);
      PendingIntent pi = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
      AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
      //am.set(AlarmManager.RTC_WAKEUP, mCalendar.getTimeInMillis(), pi);
      am.setRepeating(AlarmManager.RTC_WAKEUP, mCalendar.getTimeInMillis(), (1000 * 60 * 60 * 24), pi);
    }
  }

  public boolean isChinese() {
    Locale locale = Locale.getDefault();
    String language = locale.getLanguage();
    return "zh".equals(language);
  }
}
