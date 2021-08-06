package com.mediatek.mt6381eco.biz.profile;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mediatek.mt6381eco.R;
import com.mediatek.mt6381eco.db.AppDatabase;
import com.mediatek.mt6381eco.db.ProfileDao;
import com.mediatek.mt6381eco.db.entries.Profile;
import com.mediatek.mt6381eco.network.ApiService;
import com.mediatek.mt6381eco.network.model.ProfileRes;
import com.mediatek.mt6381eco.ui.dialogs.NumberPickDialog;
import com.mediatek.mt6381eco.ui.widgets.MutableLiveDataTextView;
import com.mediatek.mt6381eco.utils.MTextUtils;
import com.mediatek.mt6381eco.utils.MappingUtils;
import com.mobsandgeeks.saripaar.annotation.NotEmpty;

import butterknife.BindView;
import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;

import java.util.Date;
import java.util.Locale;

import javax.inject.Inject;

import okhttp3.MediaType;
import okhttp3.RequestBody;

import static android.os.ParcelFileDescriptor.MODE_APPEND;

public class ProfilePresenter implements ProfileContract.Presenter {

    private final ProfileViewModel mViewModel;
    private final ProfileDao mProfileDao;
    private final ApiService mApiService;
    private final ProfileContract.View mView;
    public boolean isChinese = false;
    @Inject
    ProfilePresenter(ProfileContract.View view, ProfileViewModel viewModel, AppDatabase appDatabase, ApiService apiService) {
        mView = view;
        mViewModel = viewModel;
        mProfileDao = appDatabase.profileDao();
        mApiService = apiService;
        loadProfile();
    }

    private void loadProfile() {
        Profile profile = mProfileDao.findProfile();
        if (null != profile) {
            mViewModel.getNickName().setValue(profile.getNickName());
            mViewModel.getGender().setValue(profile.getGender());
            mViewModel.getBirthday()
                    .setValue(profile.getBirthday() != null ? new Date(profile.getBirthday()) : null);
            mViewModel.getHeight().setValue(profile.getHeight());
            Integer weight = profile.getWeight();
            String weightUnit = profile.getWeightUnit();
            if (weight != null && weightUnit != null) {
                mViewModel.getWeight().setValue(new ValueUnit(weight, weightUnit));
            }
            mViewModel.getProfileId().setValue(profile.getProfileId());
            //此时会把访客模式下的信息加载进来
            Log.d("ProfilePresenter", "profile.getProfileId()" + profile.getProfileId());
            int personalStatus = 0;
            switch (profile.getPersonalStatus()) {
                case Profile.PERSONAL_STATUS_NONE: {
                    personalStatus = ProfileViewModel.PERSONAL_STATUS_NONE;
                    break;
                }
                case Profile.PERSONAL_STATUS_HYPOTENSION: {
                    personalStatus = ProfileViewModel.PERSONAL_STATUS_HYPOTENSION;
                    break;
                }
                case Profile.PERSONAL_STATUS_HYPERTENSION: {
                    personalStatus = ProfileViewModel.PERSONAL_STATUS_HYPERTENSION;
                    break;
                }
            }
            mViewModel.getPersonalStatus().setValue(personalStatus);
            mViewModel.getTakeMedicineTime().setValue(profile.getTakeMedicineTime());
            mViewModel.isCalibrated.setValue(profile.isCalibrated());
        }
    }

    @Override
    public void destroy() {

    }

    @Override
    public Completable requestSaveProfile() {
        ProfileRes profileRes = new ProfileRes();
        profileRes.name = mViewModel.getNickName().getValue();
        profileRes.gender =
                Profile.GENDER_MALE == mViewModel.getGender().getValue() ? ProfileRes.GENDER_MALE
                        : ProfileRes.GENDER_FEMALE;
        profileRes.birthday = MTextUtils.formatDate(mViewModel.getBirthday().getValue());
        if (isChinese()) {
            isChinese = true;
        } else {
            isChinese = false;
        }
        profileRes.height = mViewModel.getHeight().getValue();
        profileRes.weight = mViewModel.getWeight().getValue().getValue();
        profileRes.weightUnit = mViewModel.getWeight().getValue().getUnit();
        switch (mViewModel.getPersonalStatus().getValue()) {
            case ProfileViewModel.PERSONAL_STATUS_NONE: {
                profileRes.personalStatus = ProfileRes.PERSONAL_STATUS_NONE;
                break;
            }
            case ProfileViewModel.PERSONAL_STATUS_HYPERTENSION: {
                profileRes.personalStatus = ProfileRes.PERSONAL_STATUS_HYPERTENSION;
                break;
            }
            case ProfileViewModel.PERSONAL_STATUS_HYPOTENSION: {
                profileRes.personalStatus = ProfileRes.PERSONAL_STATUS_HYPOTENSION;
                break;
            }
        }
        profileRes.takeMedicineTime = mViewModel.getTakeMedicineTime().getValue();

        String profileId = mViewModel.getProfileId().getValue();
        Single<ProfileRes> mApiRequestProfile;
        Gson gson = new GsonBuilder()
                .serializeNulls()
                .excludeFieldsWithoutExposeAnnotation()
                .create();
        RequestBody requestBody =
                RequestBody.create(MediaType.parse("application/json"), gson.toJson(profileRes));
        Log.d("ProfilePresenter", "guestoUser profileId: " + profileId);//28 88888
        //modify by herman 登录模式下，更新之前的88888，profileId:解决从访客模式切换到登录模式下，测量之前校准无效的问题,前提是登录后需要跳转到profile界面
        if (profileId == null || profileId.equals("88888")) {
            mApiRequestProfile = mApiService.createProfile(requestBody);
            Log.d("ProfilePresenter", "createProfile...");
        } else {
            mApiRequestProfile =
                    mApiService.updateProfile(mViewModel.getProfileId().getValue(), requestBody);
        }
        return mApiRequestProfile.doOnSuccess(profileRes1 -> {
            Profile profile = MappingUtils.toDbEntry(profileRes1);
            mProfileDao.insertProfile(profile);
        }).subscribeOn(Schedulers.io()).toCompletable().doOnComplete(mView::navToNext);
    }

    //add by herman for sb访客模式
    public void requestSaveProfileForSB() {
        ProfileRes profileRes = new ProfileRes();
        profileRes.name = mViewModel.getNickName().getValue();
        profileRes.gender =
                Profile.GENDER_MALE == mViewModel.getGender().getValue() ? ProfileRes.GENDER_MALE
                        : ProfileRes.GENDER_FEMALE;
        profileRes.birthday = MTextUtils.formatDate(mViewModel.getBirthday().getValue());
        profileRes.height = mViewModel.getHeight().getValue();
        profileRes.weight = mViewModel.getWeight().getValue().getValue();
        profileRes.weightUnit = mViewModel.getWeight().getValue().getUnit();
        switch (mViewModel.getPersonalStatus().getValue()) {
            case ProfileViewModel.PERSONAL_STATUS_NONE: {
                profileRes.personalStatus = ProfileRes.PERSONAL_STATUS_NONE;
                break;
            }
            case ProfileViewModel.PERSONAL_STATUS_HYPERTENSION: {
                profileRes.personalStatus = ProfileRes.PERSONAL_STATUS_HYPERTENSION;
                break;
            }
            case ProfileViewModel.PERSONAL_STATUS_HYPOTENSION: {
                profileRes.personalStatus = ProfileRes.PERSONAL_STATUS_HYPOTENSION;
                break;
            }
        }
        profileRes.takeMedicineTime = mViewModel.getTakeMedicineTime().getValue();

        String profileId = mViewModel.getProfileId().getValue();

        //modify by herman
        if (profileId == null) {
            Log.d("ProfilePresenter", "profileId: " + profileId);
            profileId = "88888";
        }
        Log.d("ProfilePresenter", "profileId: " + profileId);
        profileRes.profileId = profileId;
        //end
        try {
            Profile profile = MappingUtils.toDbEntry(profileRes);
            mProfileDao.insertProfile(profile);
            Log.d("ProfilePresenter", "profile 数据插入数据库成功");
        } catch (Exception e) {
            e.printStackTrace();
        }
        mView.navToNext();
    }

    //add by herman end
    public boolean isChinese() {
        Locale locale = Locale.getDefault();
        String language = locale.getLanguage();
        return "zh".equals(language);
    }
}
