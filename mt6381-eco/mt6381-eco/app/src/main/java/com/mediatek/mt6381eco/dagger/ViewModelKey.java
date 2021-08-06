package com.mediatek.mt6381eco.dagger;

import android.arch.lifecycle.ViewModel;

import com.mediatek.mt6381eco.mvp.BasePresenter2;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import dagger.MapKey;

@Documented
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@MapKey public @interface ViewModelKey {
    Class<? extends ViewModel> value();
}
