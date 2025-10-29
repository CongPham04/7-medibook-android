package com.example.medibookandroid;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.medibookandroid.adapter.DoctorAdapter;
import com.example.medibookandroid.databinding.FragmentPatientHomeBinding;
import com.example.medibookandroid.model.StorageRepository;

public class PatientHomeFragment extends Fragment {

    private FragmentPatientHomeBinding binding;
    private StorageRepository storageRepository;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentPatientHomeBinding.inflate(inflater, container, false);
        storageRepository = StorageRepository.getInstance(getContext());
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final NavController navController = Navigation.findNavController(view);

        // Using a hardcoded name as currentPatient is not stored in StorageRepository
        binding.tvWelcomeUser.setText("Hi, Jane Doe!");

        DoctorAdapter adapter = new DoctorAdapter(storageRepository.doctors, doctor -> {
            Bundle bundle = new Bundle();
            bundle.putInt("doctorId", doctor.getId());
            navController.navigate(R.id.action_patientHomeFragment_to_patientDoctorDetailFragment, bundle);
        });
        binding.rvDoctorList.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvDoctorList.setAdapter(adapter);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
