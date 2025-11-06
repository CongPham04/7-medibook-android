package com.example.medibookandroid.data.repository; // ⭐️ Sửa package nếu cần

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.medibookandroid.data.model.Review;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import java.util.List;

public class ReviewRepository {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    /**
     * Lấy TẤT CẢ đánh giá cho một bác sĩ cụ thể, sắp xếp theo thời gian mới nhất
     */
    public LiveData<List<Review>> getReviewsForDoctor(String doctorId) {
        MutableLiveData<List<Review>> reviewsLiveData = new MutableLiveData<>();

        db.collection("reviews")
                .whereEqualTo("doctorId", doctorId)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        // Xử lý lỗi
                        reviewsLiveData.setValue(null);
                        return;
                    }
                    if (snapshots != null) {
                        // Chuyển đổi snapshots sang List<Review>
                        List<Review> reviews = snapshots.toObjects(Review.class);
                        reviewsLiveData.setValue(reviews);
                    }
                });

        return reviewsLiveData;
    }
}