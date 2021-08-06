package com.mediatek.mt6381eco.biz.profile;
import com.mediatek.mt6381eco.dagger.ActivityScoped;
import dagger.Subcomponent;
import dagger.android.AndroidInjector;

@ActivityScoped @Subcomponent(modules = {ProfileModule.class}) public interface ProfileSubComponent
    extends AndroidInjector<ProfileActivity> {
  @Subcomponent.Builder abstract class Builder extends AndroidInjector.Builder<ProfileActivity> {
  }
}
