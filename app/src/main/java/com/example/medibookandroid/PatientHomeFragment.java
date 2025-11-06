package com.example.medibookandroid;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.medibookandroid.adapter.DoctorAdapter;
import com.example.medibookandroid.databinding.FragmentPatientHomeBinding;
import com.example.medibookandroid.model.Doctor;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class PatientHomeFragment extends Fragment {

    private FragmentPatientHomeBinding binding;
    private FirebaseFirestore db;
    private DoctorAdapter adapter;
    private List<Doctor> doctorList;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentPatientHomeBinding.inflate(inflater, container, false);
        db = FirebaseFirestore.getInstance();
        doctorList = new ArrayList<>();
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final NavController navController = Navigation.findNavController(view);

        binding.tvWelcomeUser.setText("Chào Jane Doe!"); // Bạn có thể thay bằng tên user thực tế

        // Khởi tạo adapter rỗng trước
        adapter = new DoctorAdapter(doctorList, doctor -> {
            Bundle bundle = new Bundle();
            bundle.putInt("doctorId", doctor.getId());
            navController.navigate(R.id.action_patientHomeFragment_to_patientDoctorDetailFragment, bundle);
        });
        binding.rvDoctorList.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvDoctorList.setAdapter(adapter);

        loadDoctorsFromFirestore();
    }

    private void loadDoctorsFromFirestore() {
        db.collection("doctors")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    doctorList.clear();
                    int autoId = 1;
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        String name = doc.getString("name"); // nếu không có field "name" trong Firestore, có thể thêm sau
                        String specialty = doc.getString("specialty");
                        String qualifications = doc.getString("qualifications");
                        String workplace = doc.getString("workplace");
                        String phone = doc.getString("phone");
                        String about = doc.getString("about");

                        Doctor doctor = new Doctor(
                                autoId++,
                                name != null ? name : "Bác sĩ chưa rõ tên",
                                specialty != null ? specialty : "Chưa rõ chuyên khoa",
                                qualifications != null ? qualifications : "Chưa rõ trình độ",
                                workplace != null ? workplace : "Chưa rõ nơi làm việc",
                                phone != null ? phone : "Không có số điện thoại",
                                about != null ? about : "Không có mô tả",
                                0.0
                        );
                        doctorList.add(doctor);
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Lỗi khi tải danh sách bác sĩ", e);
                    Toast.makeText(getContext(), "Lỗi khi tải dữ liệu", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
