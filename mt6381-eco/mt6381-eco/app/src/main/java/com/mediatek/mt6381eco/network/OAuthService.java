package com.mediatek.mt6381eco.network;

import com.mediatek.mt6381eco.network.model.AuthResponse;
import com.mediatek.mt6381eco.network.model.LoginRequest;
import io.reactivex.Single;
import java.util.Map;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.HeaderMap;
import retrofit2.http.POST;

public interface OAuthService {
  String HEADER_AUTHORIZATION = "Authorization";

  @POST("/med/auth/login") Single<AuthResponse> login(@Body LoginRequest request);

  @GET("/med/auth/guest") Single<AuthResponse> guest();

  @GET("/med/auth/refresh") Single<AuthResponse> refreshToken(
      @Header(HEADER_AUTHORIZATION) String authorization);

  @POST("/med/auth/downgrade") Single<AuthResponse> downgrade(
      @Header(HEADER_AUTHORIZATION) String authorization);

  @POST("/med/auth/upgrade") Single<AuthResponse> upgrade(
      @Header(HEADER_AUTHORIZATION) String authorization);

  @POST("/med/auth/register") Single<AuthResponse> register(@Body LoginRequest request,@HeaderMap
      Map<String, String> headers);
}
