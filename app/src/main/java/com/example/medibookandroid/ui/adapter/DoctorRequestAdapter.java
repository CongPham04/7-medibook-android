package com.example.medibookandroid.ui.adapter; // ⭐️ SỬA PACKAGE NẾU CẦN

import android.os.Build; // ⭐️ THÊM
import android.text.Html; // ⭐️ THÊM
import android.util.Log; // ⭐️ THÊM
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medibookandroid.data.model.Patient; // ⭐️ THÊM
import com.example.medibookandroid.databinding.ItemDoctorAppointmentRequestBinding;
import com.example.medibookandroid.data.model.Appointment; // ⭐️ SỬA
import com.example.medibookandroid.ui.doctor.viewmodel.DoctorRequestsViewModel; // ⭐️ THÊM

// ⭐️ THÊM CÁC IMPORT NÀY ⭐️
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
// ⭐️ KẾT THÚC THÊM ⭐️

public class DoctorRequestAdapter extends RecyclerView.Adapter<DoctorRequestAdapter.RequestViewHolder> {

    private List<Appointment> appointments;
    private final OnRequestInteractionListener listener;
    private final DoctorRequestsViewModel viewModel; // ⭐️ THÊM

    public interface OnRequestInteractionListener {
        void onAccept(Appointment appointment);
        void onDecline(Appointment appointment);
    }

    public DoctorRequestAdapter(List<Appointment> appointments, DoctorRequestsViewModel viewModel, OnRequestInteractionListener listener) {
        this.appointments = appointments;
        this.viewModel = viewModel; // ⭐️ THÊM
        this.listener = listener;
    }

    // ⭐️ THÊM: Hàm cập nhật dữ liệu
    public void updateData(List<Appointment> newAppointments) {
        this.appointments = newAppointments;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemDoctorAppointmentRequestBinding binding = ItemDoctorAppointmentRequestBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new RequestViewHolder(binding, viewModel); // ⭐️ SỬA
    }

    @Override
    public void onBindViewHolder(@NonNull RequestViewHolder holder, int position) {
        Appointment appointment = appointments.get(position);
        // ⭐️ SỬA: Truyền 'position'
        holder.bind(appointment, listener, (LifecycleOwner) holder.itemView.getContext(), position);
    }

    @Override
    public int getItemCount() {
        return appointments.size();
    }

    static class RequestViewHolder extends RecyclerView.ViewHolder {
        private final ItemDoctorAppointmentRequestBinding binding;
        private final DoctorRequestsViewModel viewModel; // ⭐️ THÊM

        // ⭐️ THÊM: Định dạng ngày
        private final SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        private final SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        public RequestViewHolder(ItemDoctorAppointmentRequestBinding binding, DoctorRequestsViewModel viewModel) {
            super(binding.getRoot());
            this.binding = binding;
            this.viewModel = viewModel; // ⭐️ THÊM
        }

        // ⭐️ SỬA: Thêm 'position' và logic Html
        public void bind(final Appointment appointment, final OnRequestInteractionListener listener, LifecycleOwner owner, int position) {

            // --- 1. Tải tên bệnh nhân (với số thứ tự) ---
            int benhNhanNumber = position + 1;
            String patientLabel = "<b>Bệnh nhân " + benhNhanNumber + ":</b> ";
            String loadingText = "Đang tải...";

            // Set trạng thái đang tải
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                binding.tvPatientName.setText(Html.fromHtml(patientLabel + loadingText, Html.FROM_HTML_MODE_LEGACY));
            } else {
                binding.tvPatientName.setText(Html.fromHtml(patientLabel + loadingText));
            }

            MutableLiveData<Patient> patientLiveData = new MutableLiveData<>();
            viewModel.loadPatientInfo(appointment.getPatientId(), patientLiveData);

            patientLiveData.observe(owner, patient -> {
                String patientName = (patient != null) ? patient.getFullName() : "Không rõ";
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    binding.tvPatientName.setText(Html.fromHtml(patientLabel + patientName, Html.FROM_HTML_MODE_LEGACY));
                } else {
                    binding.tvPatientName.setText(Html.fromHtml(patientLabel + patientName));
                }
            });

            // --- 2. Hiển thị ngày giờ (với định dạng) ---
            String timeLabel = "<b>Thời gian:</b> ";
            String displayDate = appointment.getDate();
            if (appointment.getDate() != null && !appointment.getDate().isEmpty()) {
                try {
                    Date date = inputFormat.parse(appointment.getDate());
                    if (date != null) {
                        displayDate = outputFormat.format(date);
                    }
                } catch (Exception e) {
                    Log.e("DoctorRequestAdapter", "Lỗi định dạng ngày: " + e.getMessage());
                }
            }
            String dateTimeValue = displayDate + ", " + appointment.getTime();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                binding.tvRequestedDatetime.setText(Html.fromHtml(timeLabel + dateTimeValue, Html.FROM_HTML_MODE_LEGACY));
            } else {
                binding.tvRequestedDatetime.setText(Html.fromHtml(timeLabel + dateTimeValue));
            }

            // --- 3. Hiển thị mô tả ---
            String symptomsLabel = "<b>Mô tả tình trạng:</b> ";
            String symptomsValue = (appointment.getDescription() != null && !appointment.getDescription().isEmpty())
                    ? appointment.getDescription()
                    : "(Không có mô tả)";
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                binding.tvSymptomsDescription.setText(Html.fromHtml(symptomsLabel + symptomsValue, Html.FROM_HTML_MODE_LEGACY));
            } else {
                binding.tvSymptomsDescription.setText(Html.fromHtml(symptomsLabel + symptomsValue));
            }

            // --- 4. Gán listener (Giữ nguyên) ---
            binding.btnAccept.setOnClickListener(v -> listener.onAccept(appointment));
            binding.btnDecline.setOnClickListener(v -> listener.onDecline(appointment));
        }
    }
}