package com.example.medibookandroid.ui.patient.fragment;

import com.example.medibookandroid.R;
import com.example.medibookandroid.ui.common.BasePasswordManagerFragment;

public class PatientPasswordManagerFragment extends BasePasswordManagerFragment {

    @Override
    protected int getBottomNavigationId() {
        // Trả về ID Bottom Nav của Bệnh nhân
        return R.id.patient_bottom_nav;
    }
}