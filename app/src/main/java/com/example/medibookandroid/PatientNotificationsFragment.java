package com.example.medibookandroid;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.medibookandroid.adapter.NotificationAdapter;
import com.example.medibookandroid.databinding.FragmentPatientNotificationsBinding;
import java.util.ArrayList;
import java.util.Arrays;

public class PatientNotificationsFragment extends Fragment {

    private FragmentPatientNotificationsBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentPatientNotificationsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ArrayList<String> notifications = new ArrayList<>(Arrays.asList(
                "Your appointment with Dr. Olivia Turner is tomorrow at 10:00 AM.",
                "Your appointment with Dr. Ben Carter has been confirmed."
        ));

        NotificationAdapter adapter = new NotificationAdapter(notifications);
        binding.rvNotifications.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvNotifications.setAdapter(adapter);

        if (notifications.isEmpty()) {
            binding.tvNoNotifications.setVisibility(View.VISIBLE);
        } else {
            binding.tvNoNotifications.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
