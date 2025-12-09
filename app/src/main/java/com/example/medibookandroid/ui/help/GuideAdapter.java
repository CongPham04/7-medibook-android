// File 8: GuideAdapter.java
package com.example.medibookandroid.ui.help;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.medibookandroid.databinding.ItemGuideExpandableBinding;
import java.util.List;

public class GuideAdapter extends RecyclerView.Adapter<GuideAdapter.VH> {
    private final List<GuideItem> items;

    public GuideAdapter(List<GuideItem> items) { this.items = items; }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(ItemGuideExpandableBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int i) {
        GuideItem item = items.get(i);
        h.binding.tvTitle.setText(item.title);
        h.binding.tvContent.setText(item.content);
        h.binding.tvContent.setVisibility(View.GONE);
        h.binding.ivArrow.setRotation(0);

        h.binding.getRoot().setOnClickListener(v -> {
            if (h.binding.tvContent.getVisibility() == View.VISIBLE) {
                h.binding.tvContent.setVisibility(View.GONE);
                h.binding.ivArrow.setRotation(0);
            } else {
                h.binding.tvContent.setVisibility(View.VISIBLE);
                h.binding.ivArrow.setRotation(180);
            }
        });
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        ItemGuideExpandableBinding binding;
        VH(ItemGuideExpandableBinding b) {
            super(b.getRoot());
            binding = b;
        }
    }
}