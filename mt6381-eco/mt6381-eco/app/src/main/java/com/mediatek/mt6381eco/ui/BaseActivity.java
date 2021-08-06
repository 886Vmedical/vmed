package com.mediatek.mt6381eco.ui;

import android.arch.lifecycle.LifecycleRegistry;
import android.arch.lifecycle.LifecycleRegistryOwner;
import android.arch.lifecycle.MutableLiveData;
import android.content.Intent;
import android.support.annotation.StringRes;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import butterknife.ButterKnife;
import com.afollestad.materialdialogs.MaterialDialog;
import com.mediatek.mt6381eco.mvp.BaseView;
import com.mediatek.mt6381eco.ui.data.IntentResult;
import com.mediatek.mt6381eco.ui.exceptions.ActivityIntentActionException;
import com.mediatek.mt6381eco.ui.exceptions.UIBindParseException;
import com.mediatek.mt6381eco.ui.interfaces.IntentChecker;
import com.mobsandgeeks.saripaar.ValidationError;
import com.mobsandgeeks.saripaar.Validator;
import dagger.android.support.DaggerAppCompatActivity;
import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.SingleSource;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Function;
import io.reactivex.internal.fuseable.ScalarCallable;
import io.reactivex.subjects.PublishSubject;
import java.util.Date;
import java.util.List;
import timber.log.Timber;

public class BaseActivity extends DaggerAppCompatActivity
    implements LifecycleRegistryOwner, BaseView, Validator.ValidationListener {

  private static final int REQUEST_CUSTOM = 199;
  private final LifecycleRegistry mRegistry = new LifecycleRegistry(this);
  public PublishSubject<IntentResult> mIntentRequestSubject;
  protected UiBinder mUiBinder = new UiBinder(this);
  protected Validator mValidator = null;
  private boolean mIsValid = false;
  private final UIActionExecutor mUIActionExecutor = new UIActionExecutor(this);
  private MaterialDialog mProgressDialog;
  private final CompositeDisposable mDisposables = new CompositeDisposable();
  private boolean mIsShowError;

  @Override public LifecycleRegistry getLifecycle() {
    return mRegistry;
  }

  @Override public void setContentView(int layoutResID) {
    super.setContentView(layoutResID);
    ButterKnife.bind(this);
    mValidator = new Validator(this);
    mValidator.setValidationListener(this);
    Timber.i(this.getLocalClassName());
  }

  @Override protected void onDestroy() {
    super.onDestroy();
    mDisposables.clear();
  }

  public boolean validate() {
    return validate(true);
  }

  public boolean validate(boolean showError) {
    //todo by herman
    mIsValid = false;
    mIsShowError = showError;
    mValidator.validate();
    return mIsValid;
  }

  protected void uiAction(UIActionExecutor.Action action) {
    mUIActionExecutor.uiAction(action);
  }

  protected void bindString(TextView textView, MutableLiveData<String> liveData) {
    mUiBinder.bindString(textView, liveData);
  }

  protected void bindDate(TextView textView, MutableLiveData<Date> liveData) {
    mUiBinder.bindDate(textView, liveData);
  }

  protected void bindInteger(TextView textView, MutableLiveData<Integer> liveData) {
    mUiBinder.bindInteger(textView, liveData);
  }

  protected void bindFloat(TextView textView, MutableLiveData<Float> liveData) {
    mUiBinder.bindFloat(textView, liveData);
  }

  protected <T> void bind(TextView textView, MutableLiveData<T> liveData,
      UiBinder.DataAdapter<T> dataAdapter) {
    mUiBinder.bind(textView, liveData, dataAdapter);
  }

  protected boolean postUiChange() {
    try {
      mUiBinder.postUiChange();
      return true;
    } catch (UIBindParseException e) {
      Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
    }
    return false;
  }

  @Override public void startLoading(Object... args) {
    uiAction(() -> {
      if (args.length > 0 && args[0] instanceof String) {
        startLoading((String) args[0]);
      }
    });
  }

  @Override public void stopLoading() {
    uiAction(() -> {
      if (mProgressDialog != null && mProgressDialog.isShowing()) {
        mProgressDialog.dismiss();
        mProgressDialog = null;
      }
    });
  }

  @Override public void showError(Throwable throwable) {
    uiAction(() -> showError(ContextUtils.getErrorMessage(throwable)));
  }

  protected void showError(String message) {
    Toast.makeText(this, message, Toast.LENGTH_LONG).show();
  }

  protected void startLoading(String message) {
    mProgressDialog = new MaterialDialog.Builder(this).content(message)
        .progress(true, 0)
        .cancelable(false)
        .show();
  }

  @Override public void onValidationSucceeded() {
    mIsValid = true;
  }

  @Override public void onValidationFailed(List<ValidationError> errors) {
    mIsValid = false;
    Timber.d("onValidationFailed");
    if (mIsShowError) {
      for (ValidationError error : errors) {
        showError(error.getCollatedErrorMessage(this));
        if (error.getView() instanceof EditText) {
          error.getView().requestFocus();
        }
        break;
      }
    }
  }

  protected void subscribe(@StringRes int loadingMessageResId, Completable completable) {
    subscribe(getString(loadingMessageResId), completable);
  }

  protected void subscribe(String loadingMessage, Completable completable) {
    subscribe(completable.doOnSubscribe(disposable -> startLoading(loadingMessage))
        .doOnDispose(this::stopLoading)
        .doOnTerminate(this::stopLoading));
  }

  protected void subscribe(Completable completable) {
    mDisposables.add(completable.subscribe(() -> {
    }, this::showError));
  }

  public Completable requestIntent(Intent intent, IntentChecker checker) {
    return Completable.defer(() -> {
      if (checker.check(IntentResult.empty())) {
        return Completable.complete();
      } else {
        return requestIntent(intent).map((Function<IntentResult, Object>) intentResult -> {
          if (!checker.check(intentResult)) {
            throw new ActivityIntentActionException(intent);
          }
          return intentResult;
        }).toCompletable();
      }
    });
  }

  public Single<IntentResult> requestIntent(Intent intent) {
    return Single.defer((ScalarCallable<SingleSource<? extends IntentResult>>) () -> {
      mIntentRequestSubject = PublishSubject.create();
      startActivityForResult(intent, REQUEST_CUSTOM);
      return mIntentRequestSubject.single(IntentResult.empty());
    });
  }

  @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    switch (requestCode) {
      case REQUEST_CUSTOM: {
        if (mIntentRequestSubject != null) {
          mIntentRequestSubject.onNext(new IntentResult(resultCode, data));
          mIntentRequestSubject.onComplete();
          mIntentRequestSubject = null;
        }
        break;
      }
    }
    super.onActivityResult(requestCode, resultCode, data);
  }
}
