package com.example.medibookandroid;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.medibookandroid.adapter.DoctorRequestAdapter;
import com.example.medibookandroid.databinding.FragmentDoctorRequestsBinding;
import com.example.medibookandroid.model.Appointment;
// Thay đổi 1: Import StorageRepository
import com.example.medibookandroid.model.Doctor;
import com.example.medibookandroid.model.Notification;
import com.example.medibookandroid.model.StorageRepository;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class DoctorRequestsFragment extends Fragment {

    private FragmentDoctorRequestsBinding binding;
    // Thay đổi 2: Sử dụng StorageRepository
    private StorageRepository storageRepository;
    private DoctorRequestAdapter adapter;
    private List<Appointment> pendingAppointments;
    private int currentDoctorId; // Giả sử ID bác sĩ

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentDoctorRequestsBinding.inflate(inflater, container, false);
        // Thay đổi 3: Khởi tạo StorageRepository
        storageRepository = StorageRepository.getInstance(getContext());
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Giả sử lấy ID bác sĩ hiện tại (ví dụ: 1)
        currentDoctorId = 1; // TODO: Lấy ID bác sĩ thực tế

        // Thay đổi 4: Lọc từ storageRepository
        pendingAppointments = storageRepository.appointments.stream()
                .filter(a -> a.getDoctorId() == currentDoctorId && a.getStatus().equalsIgnoreCase("Pending"))
                .collect(Collectors.toList());

        adapter = new DoctorRequestAdapter(pendingAppointments, new DoctorRequestAdapter.OnRequestInteractionListener() {
            @Override
            public void onAccept(Appointment appointment) {
                appointment.setStatus("Confirmed");
                storageRepository.saveAppointments();

                // --- BẠN CẦN THÊM LOGIC NÀY ---
                // 1. Lấy thông tin
                int patientId = appointment.getPatientId();
                Doctor doctor = storageRepository.findDoctorById(appointment.getDoctorId());
                String doctorName = (doctor != null) ? doctor.getName() : "Bác sĩ";
                String time = new SimpleDateFormat("HH:mm, dd/MM/yyyy", Locale.getDefault()).format(appointment.getAppointmentDate());

                // 2. Tạo tiêu đề và nội dung
                String title = "Lịch hẹn đã được xác nhận";
                String body = "Lịch hẹn của bạn với " + doctorName + " vào lúc " + time + " đã được xác nhận.";

                // 3. Tạo và lưu thông báo
                Notification newNotif = new Notification(patientId, title, body);
                storageRepository.notifications.add(newNotif);
                storageRepository.saveNotifications();
                // --- KẾT THÚC PHẦN THÊM MỚI ---

                // Xóa khỏi danh sách UI (Pending)
                int position = pendingAppointments.indexOf(appointment);
                if (position != -1) {
                    pendingAppointments.remove(position);
                    adapter.notifyItemRemoved(position);
                    adapter.notifyItemRangeChanged(position, pendingAppointments.size());
                }
                updateNoRequestsView();
            }

            @Override
            public void onDecline(Appointment appointment) {
                appointment.setStatus("Canceled");
                // Thay đổi 6: Lưu thay đổi
                storageRepository.saveAppointments();

                // Xóa khỏi danh sách UI (Pending)
                int position = pendingAppointments.indexOf(appointment);
                if (position != -1) {
                    pendingAppointments.remove(position);
                    adapter.notifyItemRemoved(position);
                    adapter.notifyItemRangeChanged(position, pendingAppointments.size());
                }
                updateNoRequestsView();
            }
        });

        binding.rvAppointmentRequests.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvAppointmentRequests.setAdapter(adapter);

        updateNoRequestsView();
    }

    private void updateNoRequestsView() {
        if (pendingAppointments.isEmpty()) {
            binding.tvNoRequests.setVisibility(View.VISIBLE);
        } else {
            binding.tvNoRequests.setVisibility(View.GONE);
        }
    }

    // --- THÊM MỚI (BẮT ĐẦU) ---
    // Tải lại dữ liệu khi Fragment được hiển thị lại
    // (Phòng trường hợp bác sĩ xem chi tiết rồi quay lại)
    @Override
    public void onResume() {
        super.onResume();

        // Tải lại danh sách "Pending"
        if (storageRepository != null && adapter != null) {
            currentDoctorId = 1; // TODO: Lấy ID bác sĩ thực tế

            pendingAppointments.clear();
            pendingAppointments.addAll(
                    storageRepository.appointments.stream()
                            .filter(a -> a.getDoctorId() == currentDoctorId && a.getStatus().equalsIgnoreCase("Pending"))
                            .collect(Collectors.toList())
            );

            adapter.notifyDataSetChanged();
            updateNoRequestsView();
        }
    }
    // --- THÊM MỚI (KẾT THÚC) ---

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

