package com.example.medibookandroid.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.medibookandroid.R;
import com.example.medibookandroid.model.Appointment;
import java.util.List;

public class DoctorAvailableSlotAdapter extends RecyclerView.Adapter<DoctorAvailableSlotAdapter.SlotViewHolder> {

    private List<Appointment> availableSlots;
    private OnEditClickListener onEditClickListener;
    private OnDeleteClickListener onDeleteClickListener;

    // Interfaces cho sự kiện click
    public interface OnEditClickListener {
        void onEditClick(Appointment appointment);
    }

    public interface OnDeleteClickListener {
        void onDeleteClick(Appointment appointment);
    }

    public DoctorAvailableSlotAdapter(List<Appointment> availableSlots, OnEditClickListener editListener, OnDeleteClickListener deleteListener) {
        this.availableSlots = availableSlots;
        this.onEditClickListener = editListener;
        this.onDeleteClickListener = deleteListener;
    }

    @NonNull
    @Override
    public SlotViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_doctor_available_slot, parent, false);
        return new SlotViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SlotViewHolder holder, int position) {
        Appointment slot = availableSlots.get(position);

        // Giả định thời gian được lưu trong trường 'symptoms'
        holder.tvSlotTime.setText(slot.getSymptoms());

        holder.ibEdit.setOnClickListener(v -> onEditClickListener.onEditClick(slot));
        holder.ibDelete.setOnClickListener(v -> onDeleteClickListener.onDeleteClick(slot));
    }

    @Override
    public int getItemCount() {
        return availableSlots.size();
    }

    static class SlotViewHolder extends RecyclerView.ViewHolder {
        TextView tvSlotTime;
        ImageButton ibEdit;
        ImageButton ibDelete;

        public SlotViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSlotTime = itemView.findViewById(R.id.tv_slot_time);
            ibEdit = itemView.findViewById(R.id.ib_edit_slot);
            ibDelete = itemView.findViewById(R.id.ib_delete_slot);
        }
    }
}
