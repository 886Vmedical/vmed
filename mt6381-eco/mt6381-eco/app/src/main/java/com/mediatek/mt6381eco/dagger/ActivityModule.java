package com.mediatek.mt6381eco.dagger;

import android.app.Activity;
import com.mediatek.mt6381eco.biz.history.BPHistoryActivity;
import com.mediatek.mt6381eco.biz.history.BPHistoryComponent;
import com.mediatek.mt6381eco.biz.history.HRSpO2HistoryActivity;
import com.mediatek.mt6381eco.biz.history.HRSpO2HistoryComponent;
import com.mediatek.mt6381eco.biz.history.HRVHistoryActivity;
import com.mediatek.mt6381eco.biz.history.HRVHistoryComponent;
import com.mediatek.mt6381eco.biz.history.BRVHistoryActivity;
import com.mediatek.mt6381eco.biz.history.BRVHistoryComponent;
import com.mediatek.mt6381eco.biz.history.TEMPHistoryActivity;
import com.mediatek.mt6381eco.biz.history.TEMPHistoryComponent;
import com.mediatek.mt6381eco.biz.history.HistoryActivity;
import com.mediatek.mt6381eco.biz.history.HistoryComponent;
import com.mediatek.mt6381eco.biz.historyrecord.HistoryRecordActivity;
import com.mediatek.mt6381eco.biz.historyrecord.HistoryRecordComponent;
import com.mediatek.mt6381eco.biz.home.HomeActivity;
import com.mediatek.mt6381eco.biz.home.HomeSubComponent;
import com.mediatek.mt6381eco.biz.measure.ready.MeasureReadyActivity;
import com.mediatek.mt6381eco.biz.measure.ready.MeasureReadyComponent;
import com.mediatek.mt6381eco.biz.measure.result.MeasureResultActivity;
import com.mediatek.mt6381eco.biz.measure.result.MeasureResultSubComponent;
import com.mediatek.mt6381eco.biz.profile.ProfileActivity;
import com.mediatek.mt6381eco.biz.profile.ProfileSubComponent;
import com.mediatek.mt6381eco.biz.screening.ScreeningActivity;
import com.mediatek.mt6381eco.biz.screening.ScreeningSubComponent;
import com.mediatek.mt6381eco.biz.startup.StartupActivity;
import com.mediatek.mt6381eco.biz.startup.StartupSubComponent;
import com.mediatek.mt6381eco.biz.temp.TempSubComponent;
import com.mediatek.mt6381eco.biz.temp.TemperatureActivity;

import dagger.Binds;
import dagger.Module;
import dagger.android.ActivityKey;
import dagger.android.AndroidInjector;
import dagger.multibindings.IntoMap;

@Module(subcomponents = {
    StartupSubComponent.class, ProfileSubComponent.class, TempSubComponent.class, MeasureReadyComponent.class,
     MeasureResultSubComponent.class, HomeSubComponent.class,
    HistoryRecordComponent.class, HistoryComponent.class, HRSpO2HistoryComponent.class,
    HRVHistoryComponent.class, BRVHistoryComponent.class, TEMPHistoryComponent.class, BPHistoryComponent.class, ScreeningSubComponent.class
}) public abstract class ActivityModule {

  @Binds @IntoMap @ActivityKey(StartupActivity.class)
  abstract AndroidInjector.Factory<? extends Activity> bindLoginActivityInjectorFactory(
      StartupSubComponent.Builder builder);

  @Binds @IntoMap @ActivityKey(ProfileActivity.class)
  abstract AndroidInjector.Factory<? extends Activity> bindProfileActivityInjectorFactory(
      ProfileSubComponent.Builder builder);

  @Binds @IntoMap @ActivityKey(TemperatureActivity.class)
  abstract AndroidInjector.Factory<? extends Activity> bindTemperatureActivityInjectorFactory(
          TempSubComponent.Builder builder);

  @Binds @IntoMap @ActivityKey(MeasureReadyActivity.class)
  abstract AndroidInjector.Factory<? extends Activity> bindMeasureReadyActivityInjectorFactory(
      MeasureReadyComponent.Builder builder);

  @Binds @IntoMap @ActivityKey(MeasureResultActivity.class)
  abstract AndroidInjector.Factory<? extends Activity> bindMeasureResultActivityInjectorFactory(
      MeasureResultSubComponent.Builder builder);

  @Binds @IntoMap @ActivityKey(HistoryRecordActivity.class)
  abstract AndroidInjector.Factory<? extends Activity> bindHistoryRecordActivityInjectorFactory(
      HistoryRecordComponent.Builder builder);

  @Binds @IntoMap @ActivityKey(HistoryActivity.class)
  abstract AndroidInjector.Factory<? extends Activity> bindHistoryActivityInjectorFactory(
      HistoryComponent.Builder builder);

  @Binds @IntoMap @ActivityKey(HomeActivity.class)
  abstract AndroidInjector.Factory<? extends Activity> bindHomeActivityInjectorFactory(
      HomeSubComponent.Builder builder);

  @Binds @IntoMap @ActivityKey(HRSpO2HistoryActivity.class)
  abstract AndroidInjector.Factory<? extends Activity> bindHRSpO2HistoryActivityInjectorFactory(
      HRSpO2HistoryComponent.Builder builder);

  @Binds @IntoMap @ActivityKey(HRVHistoryActivity.class)
  abstract AndroidInjector.Factory<? extends Activity> bindHRVHistoryActivityInjectorFactory(
      HRVHistoryComponent.Builder builder);

  @Binds @IntoMap @ActivityKey(BRVHistoryActivity.class)
  abstract AndroidInjector.Factory<? extends Activity> bindBRVHistoryActivityInjectorFactory(
          BRVHistoryComponent.Builder builder);

  @Binds @IntoMap @ActivityKey(TEMPHistoryActivity.class)
  abstract AndroidInjector.Factory<? extends Activity> bindTEMPHistoryActivityInjectorFactory(
          TEMPHistoryComponent.Builder builder);

  @Binds @IntoMap @ActivityKey(BPHistoryActivity.class)
  abstract AndroidInjector.Factory<? extends Activity> bindBPHistoryActivityInjectorFactory(
      BPHistoryComponent.Builder builder);

  @Binds @IntoMap @ActivityKey(ScreeningActivity.class)
  abstract AndroidInjector.Factory<? extends Activity> bindScreeningActivityInjectorFactory(
      ScreeningSubComponent.Builder builder);
}
