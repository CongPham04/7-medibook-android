package com.example.medibookandroid.ui.adapter;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.medibookandroid.R;
import com.example.medibookandroid.data.model.Appointment;
import com.example.medibookandroid.databinding.ItemPatientAppointmentCardBinding;
import com.example.medibookandroid.ui.patient.viewmodel.PatientAppointmentsViewModel;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PatientAppointmentAdapter extends RecyclerView.Adapter<PatientAppointmentAdapter.AppointmentViewHolder> {

    private List<Appointment> appointments;
    private final OnAppointmentActionListener listener;
    private final PatientAppointmentsViewModel viewModel;
    private final LifecycleOwner lifecycleOwner;

    public interface OnAppointmentActionListener {
        void onCancelClick(Appointment appointment);
        void onRateClick(Appointment appointment);
    }

    public PatientAppointmentAdapter(List<Appointment> appointments,
                                     PatientAppointmentsViewModel viewModel,
                                     OnAppointmentActionListener listener,
                                     LifecycleOwner lifecycleOwner) {
        this.appointments = appointments;
        this.viewModel = viewModel;
        this.listener = listener;
        this.lifecycleOwner = lifecycleOwner;
    }

    public void updateData(List<Appointment> newAppointments) {
        this.appointments = newAppointments;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AppointmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemPatientAppointmentCardBinding binding = ItemPatientAppointmentCardBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new AppointmentViewHolder(binding, viewModel);
    }

    @Override
    public void onBindViewHolder(@NonNull AppointmentViewHolder holder, int position) {
        Appointment appointment = appointments.get(position);
        holder.bind(appointment, listener, lifecycleOwner);
    }

    @Override
    public int getItemCount() {
        return appointments.size();
    }

    static class AppointmentViewHolder extends RecyclerView.ViewHolder {
        private final ItemPatientAppointmentCardBinding binding;
        private final PatientAppointmentsViewModel viewModel;
        private final Context context;

        public AppointmentViewHolder(ItemPatientAppointmentCardBinding binding, PatientAppointmentsViewModel viewModel) {
            super(binding.getRoot());
            this.binding = binding;
            this.viewModel = viewModel;
            this.context = itemView.getContext();
        }

        public void bind(final Appointment appointment, final OnAppointmentActionListener listener, LifecycleOwner owner) {

            // --- 1. Hiển thị thông tin Bác sĩ (Giữ nguyên) ---
            String nameLabel = "<b>Bs.</b> ";
            String loadingText = "Đang tải...";
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                binding.tvDoctorName.setText(Html.fromHtml(nameLabel + loadingText, Html.FROM_HTML_MODE_LEGACY));
            } else {
                binding.tvDoctorName.setText(Html.fromHtml(nameLabel + loadingText));
            }

            viewModel.getDoctorById(appointment.getDoctorId()).observe(owner, doctor -> {
                String nameValue;
                if (doctor != null) {
                    nameValue = doctor.getFullName();
                    if (doctor.getAvatarUrl() != null && !doctor.getAvatarUrl().isEmpty()) {
                        Glide.with(context)
                                .load(doctor.getAvatarUrl())
                                .placeholder(R.drawable.logo2)
                                .circleCrop()
                                .into(binding.ivDoctorAvatar);
                    } else {
                        binding.ivDoctorAvatar.setImageResource(R.drawable.logo2);
                    }
                } else {
                    nameValue = "Không rõ bác sĩ";
                    binding.ivDoctorAvatar.setImageResource(R.drawable.logo2);
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    binding.tvDoctorName.setText(Html.fromHtml(nameLabel + nameValue, Html.FROM_HTML_MODE_LEGACY));
                } else {
                    binding.tvDoctorName.setText(Html.fromHtml(nameLabel + nameValue));
                }
            });

            // --- 2. Hiển thị Ngày giờ (Giữ nguyên) ---
            String displayDate = appointment.getDate();
            if (appointment.getDate() != null && !appointment.getDate().isEmpty()) {
                try {
                    SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    Date date = inputFormat.parse(appointment.getDate());
                    if (date != null) {
                        displayDate = outputFormat.format(date);
                    }
                } catch (Exception e) {
                    Log.e("PatientApptAdapter", "Lỗi định dạng ngày: " + e.getMessage());
                }
            }

            String timeLabel = "<b>Thời gian:</b> ";
            String dateTimeValue = appointment.getTime() + ", " + displayDate;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                binding.tvAppointmentDatetime.setText(Html.fromHtml(timeLabel + dateTimeValue, Html.FROM_HTML_MODE_LEGACY));
            } else {
                binding.tvAppointmentDatetime.setText(Html.fromHtml(timeLabel + dateTimeValue));
            }

            // --- 3. Xử lý Trạng thái & Nút bấm (SỬA Ở ĐÂY) ---
            String status = appointment.getStatus();
            if (status == null) status = "";
            status = status.toLowerCase();

            // Kiểm tra quá khứ
            boolean isPast = false;
            if (appointment.getDate() != null && appointment.getTime() != null) {
                try {
                    String dateTimeStr = appointment.getDate() + " " + appointment.getTime();
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                    Date apptDate = sdf.parse(dateTimeStr);
                    Date now = new Date();
                    if (apptDate != null && apptDate.before(now)) {
                        isPast = true;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            // Mặc định ẩn cả 2 nút trước
            binding.btnCancelOrChange.setVisibility(View.GONE);
            binding.btnRateDoctor.setVisibility(View.GONE);

            // Xử lý trường hợp đặc biệt: Đã xác nhận nhưng quá hạn
            if (status.equals("confirmed") && isPast) {
                binding.tvStatusLabel.setText("Đã kết thúc");
                binding.tvStatusLabel.setTextColor(Color.parseColor("#4CAF50"));
                binding.btnCancelOrChange.setVisibility(View.GONE);
            }
            else {
                switch (status) {
                    case "pending":
                        // Yêu cầu 1: Trạng thái chờ -> Hiện nút Hủy
                        binding.tvStatusLabel.setText("Chờ xác nhận");
                        binding.tvStatusLabel.setTextColor(Color.parseColor("#FF9800")); // Cam

                        binding.btnCancelOrChange.setVisibility(View.VISIBLE); // HIỆN
                        binding.btnCancelOrChange.setText("Hủy lịch");
                        binding.btnCancelOrChange.setOnClickListener(v -> listener.onCancelClick(appointment));
                        break;

                    case "confirmed":
                        // Yêu cầu 1: Trạng thái đã xác nhận -> ẨN nút Hủy
                        binding.tvStatusLabel.setText("Đã xác nhận");
                        binding.tvStatusLabel.setTextColor(Color.parseColor("#2196F3")); // Xanh dương

                        binding.btnCancelOrChange.setVisibility(View.GONE); // ẨN
                        break;

                    case "completed":
                        // Yêu cầu 2: Trạng thái hoàn thành -> Check đánh giá
                        binding.tvStatusLabel.setText("Hoàn thành");
                        binding.tvStatusLabel.setTextColor(Color.parseColor("#4CAF50")); // Xanh lá

                        if (!appointment.isReviewed()) {
                            // Chưa đánh giá -> Hiện nút
                            binding.btnRateDoctor.setVisibility(View.VISIBLE);
                            binding.btnRateDoctor.setOnClickListener(v -> listener.onRateClick(appointment));
                        } else {
                            // Đã đánh giá -> Ẩn nút
                            binding.btnRateDoctor.setVisibility(View.GONE);
                        }
                        break;

                    case "cancelled":
                        binding.tvStatusLabel.setText("Đã hủy");
                        binding.tvStatusLabel.setTextColor(Color.parseColor("#F44336")); // Đỏ
                        binding.btnCancelOrChange.setVisibility(View.GONE);
                        break;

                    case "rejected":
                        binding.tvStatusLabel.setText("Bị từ chối");
                        binding.tvStatusLabel.setTextColor(Color.parseColor("#F44336")); // Đỏ
                        binding.btnCancelOrChange.setVisibility(View.GONE);
                        break;

                    default:
                        binding.tvStatusLabel.setText(status);
                        binding.tvStatusLabel.setTextColor(Color.BLACK);
                        binding.btnCancelOrChange.setVisibility(View.GONE);
                        break;
                }
            }
        }
    }
}