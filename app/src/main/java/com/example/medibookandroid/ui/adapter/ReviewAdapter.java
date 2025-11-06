// ⭐️ Đảm bảo import đúng package
package com.example.medibookandroid.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

// ⭐️ Đảm bảo import đúng package
import com.example.medibookandroid.R;
import com.example.medibookandroid.data.model.Review; // ⭐️ Import model Review của bạn

import java.util.List;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {

    private List<Review> reviewList;

    public ReviewAdapter(List<Review> reviewList) {
        this.reviewList = reviewList;
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_review, parent, false);
        return new ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        Review review = reviewList.get(position);
        holder.bind(review);
    }

    @Override
    public int getItemCount() {
        return reviewList.size();
    }

    // Hàm cập nhật data
    public void updateData(List<Review> newReviews) {
        this.reviewList.clear();
        this.reviewList.addAll(newReviews);
        notifyDataSetChanged();
    }

    // ViewHolder
    static class ReviewViewHolder extends RecyclerView.ViewHolder {
        TextView tvPatientName;
        RatingBar ratingBar;
        TextView tvComment;

        public ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPatientName = itemView.findViewById(R.id.tv_patient_name);
            ratingBar = itemView.findViewById(R.id.rating_bar_indicator);
            tvComment = itemView.findViewById(R.id.tv_review_comment);
        }

        public void bind(Review review) {
            tvPatientName.setText(review.getPatientName()); // ⭐️ Giả sử hàm là getPatientName()
            ratingBar.setRating(review.getRating());       // ⭐️ Giả sử hàm là getRating()
            tvComment.setText(review.getComment());       // ⭐️ Giả sử hàm là getComment()
        }
    }
}