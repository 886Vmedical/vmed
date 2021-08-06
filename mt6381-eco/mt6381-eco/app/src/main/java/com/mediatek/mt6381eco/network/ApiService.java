package com.mediatek.mt6381eco.network;

import com.mediatek.mt6381eco.network.model.CalibrationObject;
import com.mediatek.mt6381eco.network.model.CalibrationResponse;
import com.mediatek.mt6381eco.network.model.DataResponse;
import com.mediatek.mt6381eco.network.model.FirmwareResponse;
import com.mediatek.mt6381eco.network.model.MeasureRetrieveResponse;
import com.mediatek.mt6381eco.network.model.Measurement;
import com.mediatek.mt6381eco.network.model.MeasurementRequest;
import com.mediatek.mt6381eco.network.model.MeasurementResponse;
import com.mediatek.mt6381eco.network.model.ProfileListResponse;
import com.mediatek.mt6381eco.network.model.ProfileRes;
import com.mediatek.mt6381eco.network.model.Screening;
import com.mediatek.mt6381eco.network.model.TempRequest;
import com.mediatek.mt6381eco.network.model.TempResponse;
import com.mediatek.mt6381eco.network.model.TemperatureRetrieveResponse;
import com.mediatek.mt6381eco.network.model.UploadRawDataResponse;
import io.reactivex.Completable;
import io.reactivex.Single;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Streaming;
import retrofit2.http.Url;

public interface ApiService {

  @GET("profiles") Single<ProfileListResponse> getProfiles();

  @POST("profiles") Single<ProfileRes> createProfile(@Body RequestBody profileRes);

  @PUT("profiles/{profileId}") Single<ProfileRes> updateProfile(@Path("profileId") String profileId,
      @Body RequestBody profileRes);

  @GET("profiles/{profileId}/measurements") Single<MeasureRetrieveResponse> retrieveMeasurements(
      @Path("profileId") String profileId, @Query("columns") String columns,
      @Query("startTime") String startTime, @Query("endTime") String endTime);

  @GET("profiles/{profileId}/measurements") @Headers({ "Content-Type: application/json" })
  Single<MeasureRetrieveResponse> retrieveMeasurements2(@Path("profileId") String profileId,
      @Query("limit") int limit, @Query("offset") int offset);

  @GET("profiles/{profileId}/measurements/{measurementId}")
  Single<Measurement> getSingleMeasurement(@Path("profileId") String profileId,
      @Path("measurementId") int measurementId);

  //gzdata change to rawData
  @Streaming @GET("profiles/{profileId}/measurements/{measurementId}/rawData")
  Single<ResponseBody> getSingleMeasurementData(@Path("profileId") String profileId,
      @Path("measurementId") int measurementId);

  // Calibrations
  //modify by herman
  @POST("profiles/{profileId}/calibrations") Completable createCalibrations2(
      @Path("profileId") String profileId, @Body CalibrationObject request);

  @GET("profiles/{profileId}/calibrations") Single<CalibrationResponse> retrieveCalibrations2(
      @Path("profileId") String profileId);

  @DELETE("profiles/{profileId}/calibrations") Completable deleteCalibration(
      @Path("profileId") String profileId);

  @GET("profiles/{profileId}/screenings") Single<DataResponse<Screening>> retrieveScreening(
      @Path("profileId") String profileId, @Query("limit") int limit, @Query("offset") int offset);

//upload temperature
  @POST("profiles/{profileId}/temperatures") Single<TempResponse>  createTemp(
          @Path("profileId") String profileId, @Body TempRequest tempRequest);

  @POST("profiles/{profileId}/temperatures") Completable  createTemp2(
          @Path("profileId") String profileId, @Body TempRequest tempRequest);

  @GET("profiles/{profileId}/temperatures")
    Single<TemperatureRetrieveResponse> retrievetemps(
          @Path("profileId") String profileId,
          @Query("startTime") String startTime,
          @Query("endTime") String endTime);
//end


  // Measurements
  @POST("profiles/{profileId}/measurements") Single<MeasurementResponse> createMeasurements2(
      @Path("profileId") String profileId, @Body MeasurementRequest request);

  //gzdata change to rawData
  @Multipart @PUT("profiles/{profileId}/measurements/{measurementId}/rawData")
  Single<UploadRawDataResponse> uploadRawData(@Path("profileId") String profileId,
      @Path("measurementId") int measurementId, @Part MultipartBody.Part file);

  @GET("firmwares/latest") Single<FirmwareResponse> getLatestFirmware();

  @Streaming @GET Single<ResponseBody> getStream(@Url String url);
}
