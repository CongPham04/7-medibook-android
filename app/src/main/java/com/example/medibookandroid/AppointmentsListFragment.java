package com.example.medibookandroid;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.medibookandroid.adapter.PatientAppointmentAdapter;
import com.example.medibookandroid.databinding.FragmentAppointmentsListBinding; // Cần tệp layout fragment_appointments_list.xml
import com.example.medibookandroid.model.Appointment;
import com.example.medibookandroid.model.StorageRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Fragment này đại diện cho MỘT tab (ví dụ: "Sắp tới")
 * và hiển thị danh sách các lịch hẹn tương ứng.
 */
public class AppointmentsListFragment extends Fragment implements PatientAppointmentAdapter.OnAppointmentCancelListener {

    private static final String ARG_STATUS_TYPE = "status_type";
    private FragmentAppointmentsListBinding binding;
    private StorageRepository storageRepository;
    private PatientAppointmentAdapter adapter;
    private List<Appointment> filteredAppointments;
    private String statusType; // Sẽ là "Upcoming", "History", hoặc "Canceled"

    /**
     * Hàm khởi tạo mới, nhận loại tab (ví dụ: "Upcoming")
     * mà ViewPager gọi.
     */
    public static AppointmentsListFragment newInstance(String statusType) {
        AppointmentsListFragment fragment = new AppointmentsListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_STATUS_TYPE, statusType);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            statusType = getArguments().getString(ARG_STATUS_TYPE);
        }
        storageRepository = StorageRepository.getInstance(getContext());
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Sử dụng tệp layout XML (fragment_appointments_list.xml)
        binding = FragmentAppointmentsListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        filteredAppointments = new ArrayList<>();
        // Khởi tạo Adapter và gán listener (chính Fragment này)
        adapter = new PatientAppointmentAdapter(filteredAppointments, this);

        binding.rvAppointments.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvAppointments.setAdapter(adapter);

        // Tải dữ liệu lần đầu
        loadAppointments();
    }

    /**
     * Tải lại dữ liệu khi quay lại tab này
     * (ví dụ: sau khi hủy lịch ở tab khác và quay lại tab "Đã hủy")
     */
    @Override
    public void onResume() {
        super.onResume();
        loadAppointments();
    }

    /**
     * Logic chính: Lọc và hiển thị các lịch hẹn
     */
    private void loadAppointments() {
        if (storageRepository == null || binding == null) return;

        // ID Bệnh nhân (Giả sử là 1 cho demo)
        int currentPatientId = 1; // TODO: Lấy ID bệnh nhân đang đăng nhập từ StorageRepository

        // 1. Xác định các trạng thái thực tế cần lọc dựa trên tên tab
        List<String> statusesToFilter;
        if (statusType.equals("Upcoming")) {
            // "Sắp tới" bao gồm cả "Chờ" và "Đã xác nhận"
            statusesToFilter = Arrays.asList("Pending", "Confirmed");
        } else if (statusType.equals("History")) {
            // "Lịch sử" (Giả sử trạng thái này là "Completed" khi khám xong)
            statusesToFilter = Arrays.asList("Completed");
        } else { // "Canceled"
            statusesToFilter = Arrays.asList("Canceled");
        }

        // 2. Lọc danh sách từ Repository
        List<Appointment> appointments = storageRepository.appointments.stream()
                // Lọc theo Bệnh nhân
                .filter(a -> a.getPatientId() == currentPatientId)
                // Lọc theo các trạng thái đã chọn
                .filter(a -> statusesToFilter.contains(a.getStatus()))
                .collect(Collectors.toList());

        // 3. Cập nhật Adapter
        filteredAppointments.clear();
        filteredAppointments.addAll(appointments);
        adapter.notifyDataSetChanged();

        // 4. Hiển thị/Ẩn thông báo "Không có lịch hẹn"
        updateEmptyView();
    }

    /**
     * Kiểm tra xem danh sách có rỗng không và cập nhật UI
     * (Sử dụng ID từ fragment_appointments_list.xml)
     */
    private void updateEmptyView() {
        // Giả sử fragment_appointments_list.xml có tv_no_appointments và rv_appointments
        if (filteredAppointments.isEmpty()) {
            binding.tvNoAppointments.setVisibility(View.VISIBLE);
            binding.rvAppointments.setVisibility(View.GONE);
        } else {
            binding.tvNoAppointments.setVisibility(View.GONE);
            binding.rvAppointments.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Được gọi khi bệnh nhân nhấn nút "Hủy" trên thẻ (từ Adapter)
     */
    @Override
    public void onCancelClick(Appointment appointment) {
        if (getContext() == null) return;

        new AlertDialog.Builder(getContext())
                .setTitle("Xác nhận hủy lịch")
                .setMessage("Bạn có chắc chắn muốn hủy lịch hẹn này không?")
                .setPositiveButton("Hủy lịch", (dialog, which) -> {
                    // Cập nhật trạng thái
                    appointment.setStatus("Canceled");
                    // Lưu lại vào SharedPreferences
                    storageRepository.saveAppointments();
                    // Tải lại danh sách cho tab hiện tại (danh sách "Sắp tới" sẽ mất item này)
                    loadAppointments();
                    dialog.dismiss();
                })
                .setNegativeButton("Không", (dialog, which) -> {
                    // Đóng dialog
                    dialog.dismiss();
                })
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

