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

import com.example.medibookandroid.R;
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
    private DoctorScheduleViewModel viewModel;
    private Calendar selectedDate;

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

        viewModel = new ViewModelProvider(this).get(DoctorScheduleViewModel.class);
        selectedDate = Calendar.getInstance();
//        Đặt ngày tối thiểu là ngày hiện tại để không chọn ca trong quá khứ , vô hiệu hóa tuwf bước chọn ngày
        binding.calendarView.setMinDate(System.currentTimeMillis());
        setupRecyclerViews();
        setupListeners();
        setupObservers(); // ⭐️ SỬA: Gọi hàm này

        // ⭐️ SỬA: Tải dữ liệu lần đầu (đã bao gồm loading)
        updateTitles(selectedDate.getTime());
        viewModel.loadDataForDate(selectedDate.getTime());
    }

    private void setupRecyclerViews() {
        binding.rvAvailableSlots.setLayoutManager(new LinearLayoutManager(getContext()));
        slotAdapter = new DoctorAvailableSlotAdapter(new ArrayList<>(), this, this);
        binding.rvAvailableSlots.setAdapter(slotAdapter);

        binding.rvConfirmedAppointments.setLayoutManager(new LinearLayoutManager(getContext()));
        // ⭐️ SỬA: Thêm `getViewLifecycleOwner()`
        appointmentAdapter = new DoctorAppointmentAdapter(new ArrayList<>(), viewModel, this, getViewLifecycleOwner());
        binding.rvConfirmedAppointments.setAdapter(appointmentAdapter);
    }

    private void setupListeners() {
        binding.calendarView.setOnDateChangeListener((v, year, month, dayOfMonth) -> {
            selectedDate.set(year, month, dayOfMonth);
            updateTitles(selectedDate.getTime());
            viewModel.loadDataForDate(selectedDate.getTime());
        });

        binding.fabAddSlot.setOnClickListener(v -> {
//            Thông báo khi chọn ngày trong quá khứ để chọn ca
            if (isDateInPast(selectedDate)) {
                Toast.makeText(getContext(), "Không thể thêm ca làm việc trong quá khứ", Toast.LENGTH_SHORT).show();
                return;
            }
            showAddOrEditSlotDialog(null);
        });
    }

    // ⭐️ BẮT ĐẦU SỬA: Tách riêng logic Observe ⭐️
    private void setupObservers() {
        // 1. Lắng nghe Ca làm việc
        viewModel.getAvailableSlots().observe(getViewLifecycleOwner(), schedules -> {
            if (schedules != null) {
                slotAdapter.updateData(schedules);
            }
            // Logic loading/empty được chuyển sang observer 2
        });

        // 2. Lắng nghe trạng thái TẢI Ca làm việc
        viewModel.isLoadingAvailable().observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading == null) return;
            if (isLoading) {
                binding.progressBarAvailable.setVisibility(View.VISIBLE);
                binding.rvAvailableSlots.setVisibility(View.GONE);
                binding.tvNoAvailableSlots.setVisibility(View.GONE);
            } else {
                binding.progressBarAvailable.setVisibility(View.GONE);
                // Kiểm tra lại list sau khi tải xong
                if (slotAdapter.getItemCount() == 0) {
                    binding.rvAvailableSlots.setVisibility(View.GONE);
                    binding.tvNoAvailableSlots.setVisibility(View.VISIBLE);
                } else {
                    binding.rvAvailableSlots.setVisibility(View.VISIBLE);
                    binding.tvNoAvailableSlots.setVisibility(View.GONE);
                }
            }
        });

        // 3. Lắng nghe Lịch hẹn
        viewModel.getConfirmedAppointments().observe(getViewLifecycleOwner(), appointments -> {
            if (appointments != null) {
                appointmentAdapter.updateData(appointments);
            }
            // Logic loading/empty được chuyển sang observer 4
        });

        // 4. Lắng nghe trạng thái TẢI Lịch hẹn
        viewModel.isLoadingConfirmed().observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading == null) return;
            if (isLoading) {
                binding.progressBarConfirmed.setVisibility(View.VISIBLE);
                binding.rvConfirmedAppointments.setVisibility(View.GONE);
                binding.tvNoConfirmedAppointments.setVisibility(View.GONE);
            } else {
                binding.progressBarConfirmed.setVisibility(View.GONE);
                // Kiểm tra lại list sau khi tải xong
                if (appointmentAdapter.getItemCount() == 0) {
                    binding.rvConfirmedAppointments.setVisibility(View.GONE);
                    binding.tvNoConfirmedAppointments.setVisibility(View.VISIBLE);
                } else {
                    binding.rvConfirmedAppointments.setVisibility(View.VISIBLE);
                    binding.tvNoConfirmedAppointments.setVisibility(View.GONE);
                }
            }
        });

        // 5. Lắng nghe thông báo (Toast)
        viewModel.getToastMessage().observe(getViewLifecycleOwner(), message -> {
            if (message != null && !message.isEmpty()) {
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            }
        });

        // 6. Lắng nghe trạng thái "Hoàn tất"
        viewModel.getCompletionStatus().observe(getViewLifecycleOwner(), success -> {
            if (success == null) return;
            if (Boolean.FALSE.equals(success)) {
                Toast.makeText(getContext(), "Lỗi: Không thể hoàn tất lịch hẹn", Toast.LENGTH_SHORT).show();
            }
        });
    }
    // ⭐️ KẾT THÚC SỬA ⭐️

//     (Hàm updateTitles giữ nguyên)
    private void updateTitles(Date date) {
        String formattedDate = displayDateFormat.format(date);
        binding.tvAppointmentsTitle.setText("📅 Lịch hẹn đã xác nhận (" + formattedDate + ")");
        binding.tvAvailableSlotsTitle.setText("🕘 Ca làm việc có sẵn (" + formattedDate + ")");
    }

//    private void updateTitles(Date date) {
//        // Chỉ cần getContext() là đủ, không cần requireContext() vì ta đã kiểm tra null trong các hàm khác
//        if (getContext() == null) return;
//
//        String formattedDate = displayDateFormat.format(date);
//
//        // Sử dụng getString(resourceId, formatArgs) để chèn ngày vào chuỗi
//        binding.tvAppointmentsTitle.setText(getString(R.string.appointments_confirmed, formattedDate));
//        binding.tvAvailableSlotsTitle.setText(getString(R.string.shifts_available, formattedDate));
//    }


    // (Hàm showAddOrEditSlotDialog giữ nguyên)
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
            // Chế độ Thêm mới (dùng text mặc định từ XML)
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

            if (slotToEdit != null) {
                viewModel.updateScheduleSlot(slotToEdit, startTimeStr, endTimeStr);
            } else {
                viewModel.createScheduleSlot(dateString, startTimeStr, endTimeStr);
            }
            dialog.dismiss();
        });

        // ⭐️ SỬA: Gán listener cho nút 'X' (ib_close_dialog)
        dialogBinding.ibCloseDialog.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    // (Hàm onEditClick giữ nguyên)
    @Override
    public void onEditClick(DoctorSchedule schedule) {
        showAddOrEditSlotDialog(schedule);
    }

    // (Hàm onDeleteClick giữ nguyên)
    @Override
    public void onDeleteClick(DoctorSchedule schedule) {
        if (getContext() == null) return;
        new AlertDialog.Builder(getContext())
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc chắn muốn xóa ca làm việc này không?\n(" + schedule.getStartTime() + " - " + schedule.getEndTime() + ")")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    viewModel.deleteScheduleSlot(schedule);
                    dialog.dismiss();
                })
                .setNegativeButton("Hủy", (dialog, which) -> {
                    dialog.dismiss();
                })
                .show();
    }

    // (Hàm onCompleteClick giữ nguyên)
    @Override
    public void onCompleteClick(Appointment appointment) {
        if (getContext() == null) return;

        new AlertDialog.Builder(getContext())
                .setTitle("Xác nhận Hoàn tất")
                .setMessage("Bạn có chắc chắn muốn đánh dấu lịch hẹn này là đã hoàn thành không?")
                .setPositiveButton("Hoàn tất", (dialog, which) -> {
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
//    Kiểm tra ngày đã chọn có trong quá khứ không
    private boolean isDateInPast(Calendar date) {
        Calendar today = Calendar.getInstance();
        // Đặt giờ, phút, giây, mili-giây về 0 để chỉ so sánh ngày
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);

        // So sánh ngày đã chọn với ngày hôm nay (đã được làm tròn về đầu ngày)
        return date.before(today);
    }
}