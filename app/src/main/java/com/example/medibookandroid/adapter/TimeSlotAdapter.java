package com.example.medibookandroid.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.medibookandroid.databinding.ItemTimeSlotBinding;
import java.util.List;

public class TimeSlotAdapter extends RecyclerView.Adapter<TimeSlotAdapter.TimeSlotViewHolder> {

    private final List<String> timeSlots;
    private final OnTimeSlotClickListener listener;
    private int selectedPosition = -1;

    public interface OnTimeSlotClickListener {
        void onTimeSlotClick(String time);
    }

    public TimeSlotAdapter(List<String> timeSlots, OnTimeSlotClickListener listener) {
        this.timeSlots = timeSlots;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TimeSlotViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemTimeSlotBinding binding = ItemTimeSlotBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new TimeSlotViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull TimeSlotViewHolder holder, int position) {
        String time = timeSlots.get(position);
        holder.bind(time, listener, position == selectedPosition);

        holder.itemView.setOnClickListener(v -> {
            int currentPosition = holder.getAdapterPosition();
            if (currentPosition != RecyclerView.NO_POSITION) {
                if (selectedPosition != currentPosition) {
                    int oldPosition = selectedPosition;
                    selectedPosition = currentPosition;
                    notifyItemChanged(oldPosition);
                    notifyItemChanged(selectedPosition);
                    listener.onTimeSlotClick(timeSlots.get(currentPosition));
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return timeSlots.size();
    }

    static class TimeSlotViewHolder extends RecyclerView.ViewHolder {
        private final ItemTimeSlotBinding binding;

        public TimeSlotViewHolder(ItemTimeSlotBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(final String time, final OnTimeSlotClickListener listener, boolean isSelected) {
            binding.getRoot().setText(time);
            binding.getRoot().setChecked(isSelected);
        }
    }
}
