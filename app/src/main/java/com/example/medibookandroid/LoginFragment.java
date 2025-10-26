package com.example.medibookandroid;

import android.content.Intent;
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
import com.example.medibookandroid.databinding.FragmentLoginBinding;
import com.example.medibookandroid.model.StorageRepository;

public class LoginFragment extends Fragment {

    private FragmentLoginBinding binding;
    private StorageRepository storageRepository;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentLoginBinding.inflate(inflater, container, false);
        storageRepository = StorageRepository.getInstance(getContext());
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final NavController navController = Navigation.findNavController(view);

        binding.btnLogin.setOnClickListener(v -> {
            String phone = binding.tilPhone.getEditText().getText().toString();
            String password = binding.tilPassword.getEditText().getText().toString();

            // Mock patient login
            if (phone.equals("0987654321") && password.equals("123456")) {
                storageRepository.loginUser("PATIENT");
                Intent intent = new Intent(getActivity(), PatientMainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
            // Mock doctor login
            else if (phone.equals("0989898989") && password.equals("doctor123")) {
                storageRepository.loginUser("DOCTOR");
                Intent intent = new Intent(getActivity(), DoctorMainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            } else {
                Toast.makeText(getContext(), "Invalid credentials", Toast.LENGTH_SHORT).show();
            }
        });

        binding.toolbar.setNavigationOnClickListener(v -> navController.popBackStack());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
