package com.mediatek.mt6381eco.biz.screening;

import android.support.v4.app.Fragment;
import com.mediatek.mt6381eco.biz.screening.history.HistoryFragment;
import com.mediatek.mt6381eco.biz.screening.history.HistorySubComponent;
import com.mediatek.mt6381eco.dagger.FragmentScoped;
import dagger.Binds;
import dagger.Module;
import dagger.android.AndroidInjector;
import dagger.android.support.FragmentKey;
import dagger.multibindings.IntoMap;

@Module(subcomponents = HistorySubComponent.class) abstract class ScreeningModule {

  @Binds @IntoMap @FragmentKey(HistoryFragment.class)
  abstract AndroidInjector.Factory<? extends Fragment> bindHistoryFragmentInjectorFactory(
      HistorySubComponent.Builder builder);
}
