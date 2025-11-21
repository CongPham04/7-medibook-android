package com.example.medibookandroid.ui.doctor.fragment;

import com.example.medibookandroid.R;
import com.example.medibookandroid.ui.common.BaseLanguageSettingsFragment;

public class DoctorLanguageSettingsFragment extends BaseLanguageSettingsFragment {

    @Override
    protected int getBottomNavigationId() {
        // Trả về ID Bottom Nav của Bác sĩ
        return R.id.doctor_bottom_nav;
    }
}