package com.example.medibookandroid.ui.doctor.fragment;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.medibookandroid.data.model.Appointment;
import com.example.medibookandroid.data.model.DoctorSchedule;
import com.example.medibookandroid.databinding.DialogAddScheduleSlotBinding;
import com.example.medibookandroid.databinding.FragmentDoctorScheduleBinding;
import com.example.medibookandroid.ui.adapter.DoctorAppointmentAdapter;
import com.example.medibookandroid.ui.adapter.DoctorAvailableSlotAdapter;
import com.example.medibookandroid.ui.doctor.viewmodel.DoctorScheduleViewModel;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DoctorScheduleFragment extends Fragment implements
        DoctorAvailableSlotAdapter.OnEditClickListener,
        DoctorAvailableSlotAdapter.OnDeleteClickListener,
        DoctorAppointmentAdapter.OnCompleteClickListener {

    private FragmentDoctorScheduleBinding binding;
    private DoctorScheduleViewModel viewModel; // Sử dụng ViewModel
    private Calendar selectedDate;

    // Định dạng ngày
    private final SimpleDateFormat displayDateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private final SimpleDateFormat firestoreDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

    private DoctorAvailableSlotAdapter slotAdapter;
    private DoctorAppointmentAdapter appointmentAdapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentDoctorScheduleBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Khởi tạo ViewModel
        viewModel = new ViewModelProvider(this).get(DoctorScheduleViewModel.class);

        selectedDate = Calendar.getInstance();

        // 2. Setup RecyclerViews với Adapter rỗng
        setupRecyclerViews();

        // 3. Setup Listeners
        setupListeners();

        // 4. Setup Observers (Lắng nghe ViewModel)
        setupObservers();

        // 5. Tải dữ liệu lần đầu
        updateTitles(selectedDate.getTime());
        viewModel.loadDataForDate(selectedDate.getTime());
    }

    private void setupRecyclerViews() {
        binding.rvAvailableSlots.setLayoutManager(new LinearLayoutManager(getContext()));
        // Khởi tạo adapter với this (ViewModel) và list rỗng
        slotAdapter = new DoctorAvailableSlotAdapter(new ArrayList<>(), this, this);
        binding.rvAvailableSlots.setAdapter(slotAdapter);

        binding.rvConfirmedAppointments.setLayoutManager(new LinearLayoutManager(getContext()));

        // ⭐️ BẮT ĐẦU SỬA: Thêm `getViewLifecycleOwner()` ⭐️
        appointmentAdapter = new DoctorAppointmentAdapter(new ArrayList<>(), viewModel, this, getViewLifecycleOwner());
        // ⭐️ KẾT THÚC SỬA ⭐️

        binding.rvConfirmedAppointments.setAdapter(appointmentAdapter);
    }

    private void setupListeners() {
        binding.calendarView.setOnDateChangeListener((v, year, month, dayOfMonth) -> {
            selectedDate.set(year, month, dayOfMonth);
            updateTitles(selectedDate.getTime());
            // Chỉ cần bảo ViewModel tải data
            viewModel.loadDataForDate(selectedDate.getTime());
        });

        binding.fabAddSlot.setOnClickListener(v -> {
            // Truyền null vì đây là tạo mới
            showAddOrEditSlotDialog(null);
        });
    }

    private void setupObservers() {
        // Observer cho ca làm việc
        viewModel.getAvailableSlots().observe(getViewLifecycleOwner(), schedules -> {
            // ⭐️ SỬA: Thêm kiểm tra null
            if (schedules == null) return;
            slotAdapter.updateData(schedules); // Cập nhật adapter
            if (schedules.isEmpty()) {
                binding.tvNoAvailableSlots.setVisibility(View.VISIBLE);
                binding.rvAvailableSlots.setVisibility(View.GONE);
            } else {
                binding.tvNoAvailableSlots.setVisibility(View.GONE);
                binding.rvAvailableSlots.setVisibility(View.VISIBLE);
            }
        });

        // Observer cho lịch đã hẹn
        viewModel.getConfirmedAppointments().observe(getViewLifecycleOwner(), appointments -> {
            // ⭐️ SỬA: Thêm kiểm tra null
            if (appointments == null) return;
            appointmentAdapter.updateData(appointments); // Cập nhật adapter
            if (appointments.isEmpty()) {
                binding.tvNoConfirmedAppointments.setVisibility(View.VISIBLE);
                binding.rvConfirmedAppointments.setVisibility(View.GONE);
            } else {
                binding.tvNoConfirmedAppointments.setVisibility(View.GONE);
                binding.rvConfirmedAppointments.setVisibility(View.VISIBLE);
            }
        });

        // Observer cho thông báo (Toast)
        viewModel.getToastMessage().observe(getViewLifecycleOwner(), message -> {
            if (message != null && !message.isEmpty()) {
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            }
        });

        // Quan sát trạng thái "Hoàn tất" (để báo lỗi nếu cần)
        viewModel.getCompletionStatus().observe(getViewLifecycleOwner(), success -> {
            if (success == null) return;

            if (Boolean.FALSE.equals(success)) {
                // Chỉ báo lỗi nếu thất bại, vì thành công đã có toast "Đã hoàn tất"
                Toast.makeText(getContext(), "Lỗi: Không thể hoàn tất lịch hẹn", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Hàm này chỉ cập nhật UI, không lấy data
    private void updateTitles(Date date) {
        String formattedDate = displayDateFormat.format(date);
        binding.tvAppointmentsTitle.setText("📅 Lịch hẹn đã xác nhận (" + formattedDate + ")");
        binding.tvAvailableSlotsTitle.setText("🕘 Ca làm việc có sẵn (" + formattedDate + ")");
    }

    // Sửa lại hàm này để dùng DoctorSchedule
    private void showAddOrEditSlotDialog(@Nullable DoctorSchedule slotToEdit) {
        if (getContext() == null) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        DialogAddScheduleSlotBinding dialogBinding = DialogAddScheduleSlotBinding.inflate(LayoutInflater.from(getContext()));
        builder.setView(dialogBinding.getRoot());
        AlertDialog dialog = builder.create();

        final Calendar startTime = Calendar.getInstance();
        final Calendar endTime = Calendar.getInstance();

        dialogBinding.tvSelectedDate.setText("Ngày: " + displayDateFormat.format(selectedDate.getTime()));

        if (slotToEdit != null) {
            // Chế độ Sửa
            // ⭐️ SỬA: Đặt text cho TextView, không phải Toolbar
            dialogBinding.tvDialogTitle.setText("Sửa ca làm việc");
            dialogBinding.etStartTime.setText(slotToEdit.getStartTime());
            dialogBinding.etEndTime.setText(slotToEdit.getEndTime());
            try {
                if (slotToEdit.getStartTime() != null)
                    startTime.setTime(timeFormat.parse(slotToEdit.getStartTime()));
                if (slotToEdit.getEndTime() != null)
                    endTime.setTime(timeFormat.parse(slotToEdit.getEndTime()));
            } catch (ParseException e) {
                Log.e("DoctorScheduleFragment", "Lỗi parse thời gian khi sửa", e);
            }
        } else {
            // Chế độ Thêm mới
            // (Giữ nguyên text mặc định "Tạo ca làm việc mới" từ XML)
            // hoặc
            // dialogBinding.tvDialogTitle.setText("Tạo ca làm việc mới");
        }

        dialogBinding.etStartTime.setOnClickListener(v -> {
            TimePickerDialog timePicker = new TimePickerDialog(getContext(), (view, hourOfDay, minute) -> {
                startTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                startTime.set(Calendar.MINUTE, minute);
                dialogBinding.etStartTime.setText(timeFormat.format(startTime.getTime()));
            }, startTime.get(Calendar.HOUR_OF_DAY), startTime.get(Calendar.MINUTE), true);
            timePicker.show();
        });

        dialogBinding.etEndTime.setOnClickListener(v -> {
            TimePickerDialog timePicker = new TimePickerDialog(getContext(), (view, hourOfDay, minute) -> {
                endTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                endTime.set(Calendar.MINUTE, minute);
                dialogBinding.etEndTime.setText(timeFormat.format(endTime.getTime()));
            }, endTime.get(Calendar.HOUR_OF_DAY), endTime.get(Calendar.MINUTE), true);
            timePicker.show();
        });

        // Khi nhấn LƯU
        dialogBinding.btnSaveSlot.setOnClickListener(v -> {
            String startTimeStr = dialogBinding.etStartTime.getText().toString();
            String endTimeStr = dialogBinding.etEndTime.getText().toString();
            String dateString = firestoreDateFormat.format(selectedDate.getTime());

            // Fragment không tự kiểm tra, chỉ gửi lệnh cho ViewModel
            if (slotToEdit != null) {
                // Gửi lệnh SỬA
                viewModel.updateScheduleSlot(slotToEdit, startTimeStr, endTimeStr);
            } else {
                // Gửi lệnh TẠO MỚI
                viewModel.createScheduleSlot(dateString, startTimeStr, endTimeStr);
            }
            dialog.dismiss();
            // ViewModel sẽ tự động cập nhật LiveData, Observers sẽ bắt và refresh UI
        });

        // ⭐️ SỬA: Gán listener cho nút 'X' (ib_close_dialog)
        dialogBinding.ibCloseDialog.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    // Interface click từ Adapter
    @Override
    public void onEditClick(DoctorSchedule schedule) {
        showAddOrEditSlotDialog(schedule);
    }

    // Interface click từ Adapter
    @Override
    public void onDeleteClick(DoctorSchedule schedule) {
        if (getContext() == null) return;

        new AlertDialog.Builder(getContext())
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc chắn muốn xóa ca làm việc này không?\n(" + schedule.getStartTime() + " - " + schedule.getEndTime() + ")")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    // Chỉ gửi lệnh XÓA cho ViewModel
                    viewModel.deleteScheduleSlot(schedule);
                    dialog.dismiss();
                })
                .setNegativeButton("Hủy", (dialog, which) -> {
                    dialog.dismiss();
                })
                .show();
    }

    /**
     * Được gọi khi bác sĩ nhấn nút "Hoàn tất" (dấu tích)
     */
    @Override
    public void onCompleteClick(Appointment appointment) {
        if (getContext() == null) return;

        new AlertDialog.Builder(getContext())
                .setTitle("Xác nhận Hoàn tất")
                .setMessage("Bạn có chắc chắn muốn đánh dấu lịch hẹn này là đã hoàn thành không?")
                .setPositiveButton("Hoàn tất", (dialog, which) -> {
                    // Gọi ViewModel
                    viewModel.markAsCompleted(appointment);
                    dialog.dismiss();
                })
                .setNegativeButton("Hủy", (dialog, which) -> {
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