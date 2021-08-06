package com.mediatek.mt6381eco.dagger;

import com.mediatek.mt6381eco.biz.flavor.FlavorUtils;
import com.mediatek.mt6381eco.biz.flavor.IFlavorUtils;
import dagger.Binds;
import dagger.Module;
import javax.inject.Singleton;

@Module
public abstract class BindModule {
  @Singleton @Binds abstract IFlavorUtils provideFlavor(FlavorUtils flavor);
  @Singleton @Binds abstract SupportSensorTypes provideSupportSensorTypes(SupportSensorTypesImpl impl);
  @Singleton @Binds abstract IAppContext provideAppContext(AppContext appContext);
}
