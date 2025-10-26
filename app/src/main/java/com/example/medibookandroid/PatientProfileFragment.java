package com.example.medibookandroid;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import com.example.medibookandroid.databinding.FragmentPatientProfileBinding;
import com.example.medibookandroid.model.StorageRepository;

public class PatientProfileFragment extends Fragment {

    private FragmentPatientProfileBinding binding;
    private StorageRepository storageRepository;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentPatientProfileBinding.inflate(inflater, container, false);
        storageRepository = StorageRepository.getInstance(getContext());
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final NavController navController = Navigation.findNavController(view);

        // In a real app, you'd get the current patient from the repository
        binding.tvUserName.setText("Jane Doe");
        binding.tvFullName.setText("Jane Doe");
        binding.tvDob.setText("01/01/1990");
        binding.tvGender.setText("Female");

        binding.btnEditProfile.setOnClickListener(v -> {
            navController.navigate(R.id.action_patientProfileFragment_to_patientEditProfileFragment);
        });

        binding.btnLogout.setOnClickListener(v -> {
            storageRepository.logoutUser();
            Intent intent = new Intent(getActivity(), MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
