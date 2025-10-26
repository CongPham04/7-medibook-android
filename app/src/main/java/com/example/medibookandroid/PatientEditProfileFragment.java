package com.example.medibookandroid;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import com.example.medibookandroid.databinding.FragmentPatientEditProfileBinding;
import com.example.medibookandroid.model.StorageRepository;
import com.example.medibookandroid.model.StorageRepository;
import com.example.medibookandroid.model.StorageRepository;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class PatientEditProfileFragment extends Fragment {

    private FragmentPatientEditProfileBinding binding;
    private StorageRepository storageRepository;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentPatientEditProfileBinding.inflate(inflater, container, false);
        storageRepository = StorageRepository.getInstance(getContext());
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

        // Mock data for display
        binding.tilFullName.getEditText().setText("Jane Doe");
        binding.tilDob.getEditText().setText("01/01/1990");
        binding.tilGender.getEditText().setText("Female");
        binding.tilPhone.getEditText().setText("0900000000");
        binding.tilAddress.getEditText().setText("123 Main St");

        binding.btnSaveProfile.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Profile updated", Toast.LENGTH_SHORT).show();
            navController.popBackStack();
        });

        binding.toolbar.setNavigationOnClickListener(v -> navController.popBackStack());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
