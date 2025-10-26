package com.example.medibookandroid;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.medibookandroid.adapter.AppointmentsPagerAdapter;
import com.example.medibookandroid.databinding.FragmentPatientAppointmentsBinding;
import com.google.android.material.tabs.TabLayoutMediator;

public class PatientAppointmentsFragment extends Fragment {

    private FragmentPatientAppointmentsBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentPatientAppointmentsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        AppointmentsPagerAdapter adapter = new AppointmentsPagerAdapter(getActivity());
        binding.viewPager.setAdapter(adapter);

        new TabLayoutMediator(binding.tabLayout, binding.viewPager,
                (tab, position) -> {
                    switch (position) {
                        case 0:
                            tab.setText("Upcoming");
                            break;
                        case 1:
                            tab.setText("History");
                            break;
                        case 2:
                            tab.setText("Canceled");
                            break;
                    }
                }).attach();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
