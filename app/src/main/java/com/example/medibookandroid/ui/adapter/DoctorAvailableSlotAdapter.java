package com.example.medibookandroid.ui.adapter;

import android.os.Build; // ⭐️ THÊM
import android.text.Html; // ⭐️ THÊM
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


    /**
     * Cập nhật danh sách ca làm việc và báo cho RecyclerView vẽ lại
     */
    public void updateData(List<DoctorSchedule> newSlots) {
        this.availableSlots = newSlots;
        notifyDataSetChanged(); // Rất quan trọng!
    }


    @NonNull
    @Override
    public SlotViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_doctor_available_slot, parent, false);
        return new SlotViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SlotViewHolder holder, int position) {

        DoctorSchedule slot = availableSlots.get(position);

        // ⭐️ BẮT ĐẦU SỬA: Thêm logic HTML ⭐️

        // Lấy số thứ tự (position bắt đầu từ 0, nên + 1)
        int caNumber = position + 1;

        // Tạo chuỗi in đậm
        String caLabel = "<b>Ca " + caNumber + ":</b> ";
        // Tạo chuỗi thường
        String timeValue = slot.getStartTime() + " - " + slot.getEndTime();

        // Gán vào TextView
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            holder.tvSlotTime.setText(Html.fromHtml(caLabel + timeValue, Html.FROM_HTML_MODE_LEGACY));
        } else {
            holder.tvSlotTime.setText(Html.fromHtml(caLabel + timeValue));
        }
        // ⭐️ KẾT THÚC SỬA ⭐️

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