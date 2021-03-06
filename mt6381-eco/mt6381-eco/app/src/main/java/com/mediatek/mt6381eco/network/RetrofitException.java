package com.mediatek.mt6381eco.network;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.mediatek.mt6381eco.network.model.ErrorResponse;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.Locale;
import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Response;
import retrofit2.Retrofit;
import timber.log.Timber;

public class RetrofitException extends RuntimeException {
  private final String url;
  private final Response response;
  private final Kind kind;
  private final Retrofit retrofit;
  private static final Gson sGson = new Gson();
  RetrofitException(String message, String url, Response response, Kind kind, Throwable exception,
      Retrofit retrofit) {
    super(message, exception);
    this.url = url;
    this.response = response;
    this.kind = kind;
    this.retrofit = retrofit;
  }

  public static RetrofitException httpError(String url, Response response, Retrofit retrofit) {
    String message = response.code() + " " + response.message();
    try {
      ResponseBody errorBody = response.errorBody();
      if(errorBody != null) {
        ErrorResponse errorResponse =
            sGson.fromJson(errorBody.charStream(), ErrorResponse.class);
        if (errorResponse.message != null) {
          message = String.format(Locale.getDefault(), "%s (%d)", errorResponse.message, response.code());
        }
      }
    }catch (JsonSyntaxException |JsonIOException e){
      Timber.d(e);
    }
    return new RetrofitException(message, url, response, Kind.HTTP, null, retrofit);
  }

  public static RetrofitException networkError(IOException exception) {
    return new RetrofitException(exception.getMessage(), null, null, Kind.NETWORK, exception, null);
  }

  public static RetrofitException unexpectedError(Throwable exception) {
    return new RetrofitException(exception.getMessage(), null, null, Kind.UNEXPECTED, exception,
        null);
  }

  /**
   * HTTP response body converted to specified {@code type}. {@code null} if there is no
   * response.
   *
   * @throws IOException if unable to convert the body to the specified {@code type}.
   */
  public static <T> T parseErrorBodyAs(Class<T> type, Response response, Retrofit retrofit)
      throws IOException {
    if (response == null || response.errorBody() == null) {
      return null;
    }
    Converter<ResponseBody, T> converter = retrofit.responseBodyConverter(type, new Annotation[0]);
    return converter.convert(response.errorBody());
  }

  /** The request URL which produced the error. */
  public String getUrl() {
    return url;
  }

  /** Response object containing status code, headers, body, etc. */
  public Response getResponse() {
    return response;
  }

  /** The event kind which triggered this error. */
  public Kind getKind() {
    return kind;
  }

  /** The Retrofit this request was executed on */
  public Retrofit getRetrofit() {
    return retrofit;
  }

  /** Identifies the event kind which triggered a {@link RetrofitException}. */
  public enum Kind {
    /** An {@link IOException} occurred while communicating to the server. */
    NETWORK, /** A non-200 HTTP status code was received from the server. */
    HTTP, /**
     * An internal error occurred while attempting to execute a request. It is best practice to
     * re-throw this exception so your application crashes.
     */
    UNEXPECTED
  }
}