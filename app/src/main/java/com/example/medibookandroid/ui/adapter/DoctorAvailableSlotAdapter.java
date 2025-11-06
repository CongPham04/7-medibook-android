package com.example.medibookandroid.ui.adapter; // (Hoặc package của bạn)

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.medibookandroid.R;
import com.example.medibookandroid.data.model.DoctorSchedule;
import java.util.List;

public class DoctorAvailableSlotAdapter extends RecyclerView.Adapter<DoctorAvailableSlotAdapter.SlotViewHolder> {

    private List<DoctorSchedule> availableSlots;
    private OnEditClickListener onEditClickListener;
    private OnDeleteClickListener onDeleteClickListener;

    public interface OnEditClickListener {
        void onEditClick(DoctorSchedule schedule);
    }

    public interface OnDeleteClickListener {
        void onDeleteClick(DoctorSchedule schedule);
    }

    public DoctorAvailableSlotAdapter(List<DoctorSchedule> availableSlots, OnEditClickListener editListener, OnDeleteClickListener deleteListener) {
        this.availableSlots = availableSlots;
        this.onEditClickListener = editListener;
        this.onDeleteClickListener = deleteListener;
    }

    // ⭐️⭐️ THÊM HÀM NÀY VÀO ⭐️⭐️
    /**
     * Cập nhật danh sách ca làm việc và báo cho RecyclerView vẽ lại
     */
    public void updateData(List<DoctorSchedule> newSlots) {
        this.availableSlots = newSlots;
        notifyDataSetChanged(); // Rất quan trọng!
    }
    // ⭐️⭐️ KẾT THÚC HÀM MỚI ⭐️⭐️

    @NonNull
    @Override
    public SlotViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // (Giữ nguyên)
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_doctor_available_slot, parent, false);
        return new SlotViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SlotViewHolder holder, int position) {
        // (Giữ nguyên)
        DoctorSchedule slot = availableSlots.get(position);
        String timeSlot = slot.getStartTime() + " - " + slot.getEndTime();
        holder.tvSlotTime.setText(timeSlot);
        holder.ibEdit.setOnClickListener(v -> onEditClickListener.onEditClick(slot));
        holder.ibDelete.setOnClickListener(v -> onDeleteClickListener.onDeleteClick(slot));
    }

    @Override
    public int getItemCount() {
        return availableSlots.size(); // (Giữ nguyên)
    }

    static class SlotViewHolder extends RecyclerView.ViewHolder {
        // (Giữ nguyên)
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