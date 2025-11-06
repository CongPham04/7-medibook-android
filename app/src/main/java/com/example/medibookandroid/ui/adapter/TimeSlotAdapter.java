package com.example.medibookandroid.ui.adapter; // ⭐️ Sửa package nếu cần

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.medibookandroid.data.model.DoctorSchedule; // ⭐️ SỬA: Import model thật
import com.example.medibookandroid.databinding.ItemTimeSlotBinding;
import java.util.List;

public class TimeSlotAdapter extends RecyclerView.Adapter<TimeSlotAdapter.TimeSlotViewHolder> {

    // ⭐️ SỬA: Dùng model DoctorSchedule
    private List<DoctorSchedule> schedules;
    private final OnTimeSlotClickListener listener;
    private int selectedPosition = -1;

    public interface OnTimeSlotClickListener {
        // ⭐️ SỬA: Trả về object DoctorSchedule
        void onTimeSlotClick(DoctorSchedule schedule);
    }

    public TimeSlotAdapter(List<DoctorSchedule> schedules, OnTimeSlotClickListener listener) {
        this.schedules = schedules;
        this.listener = listener;
    }

    // ⭐️ THÊM: Hàm cập nhật dữ liệu
    public void updateData(List<DoctorSchedule> newSchedules) {
        this.schedules = newSchedules;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TimeSlotViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemTimeSlotBinding binding = ItemTimeSlotBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new TimeSlotViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull TimeSlotViewHolder holder, int position) {
        // ⭐️ SỬA: Lấy object schedule
        DoctorSchedule schedule = schedules.get(position);
        holder.bind(schedule, position == selectedPosition);

        holder.itemView.setOnClickListener(v -> {
            int currentPosition = holder.getAdapterPosition();
            if (currentPosition != RecyclerView.NO_POSITION) {
                if (selectedPosition != currentPosition) {
                    int oldPosition = selectedPosition;
                    selectedPosition = currentPosition;
                    notifyItemChanged(oldPosition);
                    notifyItemChanged(selectedPosition);
                    // ⭐️ SỬA: Trả về object
                    listener.onTimeSlotClick(schedules.get(currentPosition));
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return schedules.size(); // ⭐️ SỬA
    }

    public void resetSelection() {
        if (selectedPosition != -1) {
            int oldPosition = selectedPosition;
            selectedPosition = -1;
            notifyItemChanged(oldPosition);
        }
    }

    static class TimeSlotViewHolder extends RecyclerView.ViewHolder {
        private final ItemTimeSlotBinding binding;

        public TimeSlotViewHolder(ItemTimeSlotBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        // ⭐️ SỬA: Nhận DoctorSchedule
        public void bind(final DoctorSchedule schedule, boolean isSelected) {
            // Ghép 2 trường startTime và endTime
            String timeText = schedule.getStartTime() + " - " + schedule.getEndTime();

            if (binding.getRoot() instanceof com.google.android.material.chip.Chip) {
                ((com.google.android.material.chip.Chip) binding.getRoot()).setText(timeText);
                ((com.google.android.material.chip.Chip) binding.getRoot()).setChecked(isSelected);
            }
        }
    }
}