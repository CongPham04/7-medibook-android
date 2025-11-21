package com.example.medibookandroid.ui.doctor.fragment;

import com.example.medibookandroid.R;
import com.example.medibookandroid.ui.common.BasePasswordManagerFragment;

public class DoctorPasswordManagerFragment extends BasePasswordManagerFragment {

    @Override
    protected int getBottomNavigationId() {
        // Trả về ID Bottom Nav của Bác sĩ
        return R.id.doctor_bottom_nav;
    }
}