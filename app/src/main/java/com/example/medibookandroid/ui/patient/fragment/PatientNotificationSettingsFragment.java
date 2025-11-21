package com.example.medibookandroid.ui.patient.fragment;

import com.example.medibookandroid.R;
import com.example.medibookandroid.ui.common.BaseNotificationSettingsFragment;

public class PatientNotificationSettingsFragment extends BaseNotificationSettingsFragment {

    @Override
    protected int getBottomNavigationId() {
        // ID của Bệnh nhân
        return R.id.patient_bottom_nav;
    }
}