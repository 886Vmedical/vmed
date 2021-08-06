package com.mediatek.mt6381eco.network;

import android.support.annotation.Nullable;
import com.mediatek.mt6381eco.BuildConfig;
import io.reactivex.Completable;
import io.reactivex.CompletableSource;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Single;
import io.reactivex.SingleSource;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Function;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import org.reactivestreams.Publisher;
import retrofit2.Call;
import retrofit2.CallAdapter;
import retrofit2.HttpException;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import timber.log.Timber;

public class RxErrorHandlingCallAdapterFactory extends CallAdapter.Factory {
  private final RxJava2CallAdapterFactory original;

  private RxErrorHandlingCallAdapterFactory() {
    original = RxJava2CallAdapterFactory.create();
  }

  public static CallAdapter.Factory create() {
    return new RxErrorHandlingCallAdapterFactory();
  }

  @Nullable @Override
  public CallAdapter<?, ?> get(Type returnType, Annotation[] annotations, Retrofit retrofit) {
    return new RxCallAdapterWrapper(retrofit, original.get(returnType, annotations, retrofit));
  }

  private static class RxCallAdapterWrapper<R, T> implements CallAdapter<R, T> {
    private final Retrofit retrofit;
    private final CallAdapter<R, T> wrapped;

    public RxCallAdapterWrapper(Retrofit retrofit, CallAdapter<R, T> wrapped) {
      this.retrofit = retrofit;
      this.wrapped = wrapped;
    }

    @Override public Type responseType() {
      return wrapped.responseType();
    }

    @SuppressWarnings("unchecked") @Override public T adapt(Call<R> call) {
      T ret = wrapped.adapt(call);
      if (ret instanceof Flowable) {
        ret = (T) ((Flowable) ret).onErrorResumeNext(new Function<Throwable, Publisher>() {
          @Override public Publisher apply(@NonNull Throwable throwable) throws Exception {
            return Flowable.error(asRetrofitException(throwable));
          }
        });
      } else if (ret instanceof Observable) {
        ret = (T) ((Observable) ret).onErrorResumeNext(new Function<Throwable, ObservableSource>() {
          @Override public ObservableSource apply(@NonNull Throwable throwable) throws Exception {
            return Observable.error(asRetrofitException(throwable));
          }
        });
      }else if(ret instanceof Single){
        ret = (T)((Single)ret).onErrorResumeNext(new Function<Throwable, SingleSource>() {
          @Override public SingleSource apply(@NonNull Throwable throwable) throws Exception {
            return Single.error(asRetrofitException(throwable));
          }
        });
      } else if(ret instanceof Completable){
        ret = (T) ((Completable)ret).onErrorResumeNext(
            throwable -> Completable.error(asRetrofitException(throwable)));
      }
      return ret;
    }

    private RetrofitException asRetrofitException(Throwable throwable) {
      if(BuildConfig.DEBUG){
        Timber.e(throwable, throwable.getMessage());
      }
      // We had non-200 http error
      if (throwable instanceof HttpException) {
        HttpException httpException = (HttpException) throwable;
        Response response = httpException.response();
        return RetrofitException.httpError(response.raw().request().url().toString(), response,
            retrofit);
      }
      // A network error happened
      if (throwable instanceof IOException) {
        return RetrofitException.networkError((IOException) throwable);
      }

      // We don't know what happened. We need to simply convert to an unknown error
      return RetrofitException.unexpectedError(throwable);
    }
  }
}