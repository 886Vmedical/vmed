package com.mediatek.mt6381eco.biz.temp;

import android.content.SharedPreferences;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.mediatek.mt6381eco.R;

public class SettingsActivity extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    private SwitchPreference mSwitchPreference;
    private static final String KEY_Switch = "hasTempbutton";
    private boolean mSwitchOn = false;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings_preference);
        mSwitchPreference = (SwitchPreference) findPreference(KEY_Switch);

        /*//这个是给Settings加自己定义Title
        final boolean isCustom = requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        if(isCustom){
            getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.title_list);
        }
        TextView title_text = (TextView)findViewById(R.id.title_text);
        title_text.setText("Settings");
        Button back = (Button)findViewById(R.id.back);
        back.setVisibility(View.VISIBLE);
        back.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                finish();

            }
        });*/

    }


    @Override
    protected void onResume() {
        super.onResume();

        // Setup the initial values
        sharedPreferences = getPreferenceScreen().getSharedPreferences();
        // Set up a listener whenever a key changes
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
        //PreferenceManager preferenceManager = getPreferenceManager();
        updateSummary();
    }

    private void updateSummary(){
        mSwitchOn = sharedPreferences.getBoolean(KEY_Switch, false);
        if (mSwitchOn) {
            Log.d("KEY_Switch","value is ture");
            // Your switch is on
            mSwitchPreference.setSummary(R.string.virtualMode);
        } else {
            // Your switch is off
            Log.d("KEY_Switch","value is false");
            mSwitchPreference.setSummary(R.string.physicalMode);

        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        // Unregister the listener whenever a key changes
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Log.d("SettingsActivity","key is :" + key);
            if(key.equals(KEY_Switch)){
                updateSummary();
            }


    }

}