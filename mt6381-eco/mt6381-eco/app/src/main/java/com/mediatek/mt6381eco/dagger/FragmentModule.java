package com.mediatek.mt6381eco.dagger;

import com.mediatek.mt6381eco.biz.account.createAccount.CreateAccountFragment;
import com.mediatek.mt6381eco.biz.account.createAccount.CreateAccountModule;
import com.mediatek.mt6381eco.biz.account.signin.SignInFragment;
import com.mediatek.mt6381eco.biz.account.signin.SignInModule;
import com.mediatek.mt6381eco.biz.calibration.CalibrationFragment;
import com.mediatek.mt6381eco.biz.calibration.CalibrationModule;
import com.mediatek.mt6381eco.biz.measure.MeasureFragment;
import com.mediatek.mt6381eco.biz.measure.MeasureModule;
import com.mediatek.mt6381eco.biz.recorddetail.RecordDetailFragment;
import com.mediatek.mt6381eco.biz.recorddetail.RecordDetailModule;
import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class FragmentModule {
  @FragmentScoped
  @ContributesAndroidInjector(modules = RecordDetailModule.class)
  abstract RecordDetailFragment contributeRecordDetailFragment();

  @FragmentScoped
  @ContributesAndroidInjector(modules = SignInModule.class)
  abstract SignInFragment contributeSignInFragment();


  @FragmentScoped
  @ContributesAndroidInjector(modules = CreateAccountModule.class)
  abstract CreateAccountFragment contributeCreateAccountFragment();

  @FragmentScoped
  @ContributesAndroidInjector(modules = MeasureModule.class)
  abstract MeasureFragment contributeMeasureFragment();


  @FragmentScoped
  @ContributesAndroidInjector(modules = CalibrationModule.class)
  abstract CalibrationFragment contributeCalibration2Fragment();
}
