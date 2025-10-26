package com.example.medibookandroid;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.medibookandroid.databinding.FragmentDoctorProfileBinding;
import com.example.medibookandroid.model.Doctor;
import android.content.Intent;
import com.example.medibookandroid.model.StorageRepository;

public class DoctorProfileFragment extends Fragment {

    private FragmentDoctorProfileBinding binding;
    private StorageRepository storageRepository;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentDoctorProfileBinding.inflate(inflater, container, false);
        storageRepository = StorageRepository.getInstance(getContext());
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // For the demo, assume the logged-in doctor is the first in the list.
        Doctor currentDoctor = storageRepository.doctors.get(0);
        if (currentDoctor != null) {
            binding.tilFullName.getEditText().setText(currentDoctor.getName());
            binding.tilSpecialty.getEditText().setText(currentDoctor.getSpecialty());
            binding.tilQualification.getEditText().setText(currentDoctor.getQualifications());
            binding.tilWorkplace.getEditText().setText(currentDoctor.getWorkplace());
            binding.tilPhone.getEditText().setText(currentDoctor.getPhone());
            binding.tilDescription.getEditText().setText(currentDoctor.getDescription());
        }

        binding.btnSaveProfile.setOnClickListener(v -> {
            // In a real app, you would save the updated data.
            Toast.makeText(getContext(), "Profile updated", Toast.LENGTH_SHORT).show();
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
