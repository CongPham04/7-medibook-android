package com.example.medibookandroid.ui.common;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import com.example.medibookandroid.R;
import com.example.medibookandroid.databinding.FragmentBookingSuccessBinding;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class BookingSuccessFragment extends Fragment {

    private FragmentBookingSuccessBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentBookingSuccessBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() != null) {
            BottomNavigationView bottomNav = getActivity().findViewById(R.id.patient_bottom_nav);
            if (bottomNav != null) {
                bottomNav.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (getActivity() != null) {
            BottomNavigationView bottomNav = getActivity().findViewById(R.id.patient_bottom_nav);
            if (bottomNav != null) {
                bottomNav.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final NavController navController = Navigation.findNavController(view);

        binding.btnBackToHome.setOnClickListener(v -> {
            // Navigate back to the home screen, clearing the back stack
            navController.navigate(R.id.action_bookingSuccessFragment_to_patientHomeFragment, null);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
