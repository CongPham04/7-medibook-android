package com.example.medibookandroid;

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
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.medibookandroid.adapter.DoctorAppointmentAdapter;
import com.example.medibookandroid.adapter.DoctorAvailableSlotAdapter;
import com.example.medibookandroid.databinding.DialogAddScheduleSlotBinding;
import com.example.medibookandroid.databinding.FragmentDoctorScheduleBinding;
import com.example.medibookandroid.model.Appointment;
import com.example.medibookandroid.model.StorageRepository;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;

public class DoctorScheduleFragment extends Fragment implements
        DoctorAvailableSlotAdapter.OnEditClickListener,
        DoctorAvailableSlotAdapter.OnDeleteClickListener {

    private FragmentDoctorScheduleBinding binding;
    private StorageRepository storageRepository;
    private Calendar selectedDate;
    private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    private DoctorAvailableSlotAdapter slotAdapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentDoctorScheduleBinding.inflate(inflater, container, false);
        storageRepository = StorageRepository.getInstance(getContext());
        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (selectedDate != null) {
            updateAppointmentsForDate(selectedDate);
        }
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        selectedDate = Calendar.getInstance();

        binding.rvAvailableSlots.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvConfirmedAppointments.setLayoutManager(new LinearLayoutManager(getContext()));

        binding.calendarView.setOnDateChangeListener((v, year, month, dayOfMonth) -> {
            selectedDate.set(year, month, dayOfMonth);
            updateAppointmentsForDate(selectedDate);
        });

        binding.fabAddSlot.setOnClickListener(v -> {
            showAddOrEditSlotDialog(selectedDate, null);
        });

        updateAppointmentsForDate(selectedDate);
    }

    private void updateAppointmentsForDate(Calendar date) {
        binding.tvAppointmentsTitle.setText("Lịch hẹn đã xác nhận (" + dateFormat.format(date.getTime()) + ")");

        int currentDoctorId = getCurrentDoctorId();

        // 1. Lọc Lịch hẹn "Đã xác nhận"
        List<Appointment> confirmedAppointments = storageRepository.appointments.stream()
                .filter(a -> a.getDoctorId() == currentDoctorId)
                .filter(a -> a.getStatus().equalsIgnoreCase("Confirmed"))
                .filter(a -> {
                    if (a.getAppointmentDate() == null || date == null) return false;
                    Calendar appointmentDateCal = Calendar.getInstance();
                    appointmentDateCal.setTime(a.getAppointmentDate());
                    return date.get(Calendar.YEAR) == appointmentDateCal.get(Calendar.YEAR) &&
                            date.get(Calendar.MONTH) == appointmentDateCal.get(Calendar.MONTH) &&
                            date.get(Calendar.DAY_OF_MONTH) == appointmentDateCal.get(Calendar.DAY_OF_MONTH);
                })
                .collect(Collectors.toList());

        DoctorAppointmentAdapter confirmedAdapter = new DoctorAppointmentAdapter(confirmedAppointments);
        binding.rvConfirmedAppointments.setAdapter(confirmedAdapter);

        // --- BẮT ĐẦU SỬA ĐỔI ---
        // Ẩn/hiện text "Không có lịch hẹn"
        if (confirmedAppointments.isEmpty()) {
            binding.tvNoConfirmedAppointments.setVisibility(View.VISIBLE);
            binding.rvConfirmedAppointments.setVisibility(View.GONE);
        } else {
            binding.tvNoConfirmedAppointments.setVisibility(View.GONE);
            binding.rvConfirmedAppointments.setVisibility(View.VISIBLE);
        }
        // --- KẾT THÚC SỬA ĐỔI ---

        // 2. Lọc Ca làm việc "Có sẵn"
        binding.tvAvailableSlotsTitle.setText("Ca làm việc có sẵn (" + dateFormat.format(date.getTime()) + ")");
        List<Appointment> availableSlots = storageRepository.appointments.stream()
                .filter(a -> a.getDoctorId() == currentDoctorId)
                .filter(a -> a.getStatus().equalsIgnoreCase("Available"))
                .filter(a -> {
                    if (a.getAppointmentDate() == null || date == null) return false;
                    Calendar appointmentDateCal = Calendar.getInstance();
                    appointmentDateCal.setTime(a.getAppointmentDate());
                    return date.get(Calendar.YEAR) == appointmentDateCal.get(Calendar.YEAR) &&
                            date.get(Calendar.MONTH) == appointmentDateCal.get(Calendar.MONTH) &&
                            date.get(Calendar.DAY_OF_MONTH) == appointmentDateCal.get(Calendar.DAY_OF_MONTH);
                })
                .collect(Collectors.toList());

        slotAdapter = new DoctorAvailableSlotAdapter(availableSlots, this, this);
        binding.rvAvailableSlots.setAdapter(slotAdapter);

        // --- BẮT ĐẦU SỬA ĐỔI ---
        // Ẩn/hiện text "Chưa có ca làm việc"
        if (availableSlots.isEmpty()) {
            binding.tvNoAvailableSlots.setVisibility(View.VISIBLE);
            binding.rvAvailableSlots.setVisibility(View.GONE);
        } else {
            binding.tvNoAvailableSlots.setVisibility(View.GONE);
            binding.rvAvailableSlots.setVisibility(View.VISIBLE);
        }
        // --- KẾT THÚC SỬA ĐỔI ---
    }

    private int getCurrentDoctorId() {
        String currentDoctorIdString = "dr1";
        try {
            return Integer.parseInt(currentDoctorIdString.replaceAll("[^0-9]", ""));
        } catch (NumberFormatException e) {
            return 1;
        }
    }

    private void showAddOrEditSlotDialog(Calendar date, @Nullable Appointment slotToEdit) {
        if (getContext() == null) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        DialogAddScheduleSlotBinding dialogBinding = DialogAddScheduleSlotBinding.inflate(LayoutInflater.from(getContext()));
        builder.setView(dialogBinding.getRoot());
        AlertDialog dialog = builder.create();

        final Calendar startTime = Calendar.getInstance();
        final Calendar endTime = Calendar.getInstance();
        final boolean[] isStartTimeSet = {false};
        final boolean[] isEndTimeSet = {false};

        dialogBinding.tvSelectedDate.setText("Ngày: " + dateFormat.format(date.getTime()));

        if (slotToEdit != null) {
            dialogBinding.toolbar.setTitle("Sửa ca làm việc");
            String timeSlot = slotToEdit.getSymptoms();
            String[] times = timeSlot.split(" - ");

            if (times.length == 2) {
                try {
                    dialogBinding.etStartTime.setText(times[0]);
                    dialogBinding.etEndTime.setText(times[1]);
                    startTime.setTime(timeFormat.parse(times[0]));
                    endTime.setTime(timeFormat.parse(times[1]));
                    isStartTimeSet[0] = true;
                    isEndTimeSet[0] = true;
                } catch (ParseException e) {
                    Log.e("DoctorScheduleFragment", "Lỗi parse thời gian khi sửa: " + timeSlot, e);
                    Toast.makeText(getContext(), "Lỗi tải ca làm việc", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        }

        dialogBinding.etStartTime.setOnClickListener(v -> {
            TimePickerDialog timePicker = new TimePickerDialog(getContext(), (view, hourOfDay, minute) -> {
                startTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                startTime.set(Calendar.MINUTE, minute);
                dialogBinding.etStartTime.setText(timeFormat.format(startTime.getTime()));
                isStartTimeSet[0] = true;
            }, startTime.get(Calendar.HOUR_OF_DAY), startTime.get(Calendar.MINUTE), true);
            timePicker.show();
        });

        dialogBinding.etEndTime.setOnClickListener(v -> {
            TimePickerDialog timePicker = new TimePickerDialog(getContext(), (view, hourOfDay, minute) -> {
                endTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                endTime.set(Calendar.MINUTE, minute);
                dialogBinding.etEndTime.setText(timeFormat.format(endTime.getTime()));
                isEndTimeSet[0] = true;
            }, endTime.get(Calendar.HOUR_OF_DAY), endTime.get(Calendar.MINUTE), true);
            timePicker.show();
        });

        dialogBinding.btnSaveSlot.setOnClickListener(v -> {
            if (!isStartTimeSet[0] || !isEndTimeSet[0]) {
                Toast.makeText(getContext(), "Vui lòng chọn giờ bắt đầu và kết thúc", Toast.LENGTH_SHORT).show();
                return;
            }
            if (startTime.after(endTime) || startTime.equals(endTime)) {
                Toast.makeText(getContext(), "Giờ kết thúc phải sau giờ bắt đầu", Toast.LENGTH_SHORT).show();
                return;
            }

            String timeSlot = timeFormat.format(startTime.getTime()) + " - " + timeFormat.format(endTime.getTime());
            int currentDoctorIdInt = getCurrentDoctorId();

            if (slotToEdit != null) {
                storageRepository.appointments.remove(slotToEdit);
                Appointment updatedSlot = new Appointment(
                        slotToEdit.getId(),
                        slotToEdit.getPatientId(),
                        currentDoctorIdInt,
                        date.getTime(),
                        "Available",
                        timeSlot
                );
                storageRepository.appointments.add(updatedSlot);
                Toast.makeText(getContext(), "Đã cập nhật ca làm việc", Toast.LENGTH_SHORT).show();
            } else {
                int newSlotId = UUID.randomUUID().hashCode();
                Appointment newSlot = new Appointment(
                        newSlotId,
                        0,
                        currentDoctorIdInt,
                        date.getTime(),
                        "Available",
                        timeSlot
                );
                storageRepository.appointments.add(newSlot);
                Toast.makeText(getContext(), "Đã lưu ca làm việc mới", Toast.LENGTH_SHORT).show();
            }

            storageRepository.saveAppointments();
            dialog.dismiss();
            updateAppointmentsForDate(selectedDate);
        });

        dialogBinding.toolbar.setNavigationOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    @Override
    public void onEditClick(Appointment appointment) {
        showAddOrEditSlotDialog(selectedDate, appointment);
    }

    @Override
    public void onDeleteClick(Appointment appointment) {
        if (getContext() == null) return;

        new AlertDialog.Builder(getContext())
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc chắn muốn xóa ca làm việc này không?\n(" + appointment.getSymptoms() + ")")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    storageRepository.appointments.remove(appointment);
                    storageRepository.saveAppointments();
                    Toast.makeText(getContext(), "Đã xóa ca: " + appointment.getSymptoms(), Toast.LENGTH_SHORT).show();
                    updateAppointmentsForDate(selectedDate);
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

