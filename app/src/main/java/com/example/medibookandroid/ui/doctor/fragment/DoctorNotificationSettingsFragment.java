package com.example.medibookandroid.ui.doctor.fragment;

import com.example.medibookandroid.R;
import com.example.medibookandroid.ui.common.BaseNotificationSettingsFragment;

public class DoctorNotificationSettingsFragment extends BaseNotificationSettingsFragment {

    @Override
    protected int getBottomNavigationId() {
        // ID của Bác sĩ
        return R.id.doctor_bottom_nav;
    }
}