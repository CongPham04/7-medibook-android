package com.example.medibookandroid.data.repository; // ⭐️ Sửa package nếu cần

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.medibookandroid.data.model.Review;
import com.google.firebase.firestore.DocumentReference;
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
    /**
     * Transaction:
     * 1. Tạo Review mới
     * 2. Cập nhật rating trung bình cho Doctor
     * 3. Cập nhật Appointment (isReviewed = true)
     */
    public void createReview(Review review, String appointmentId, OnOperationCompleteListener listener) {
        final DocumentReference doctorRef = db.collection("doctors").document(review.getDoctorId());
        final DocumentReference apptRef = db.collection("appointments").document(appointmentId);
        final DocumentReference newReviewRef = db.collection("reviews").document();

        db.runTransaction(transaction -> {
            // 1. Lấy thông tin hiện tại của Bác sĩ
            Double currentRating = transaction.get(doctorRef).getDouble("rating");
            Long totalReviews = transaction.get(doctorRef).getLong("reviewCount");

            if (currentRating == null) currentRating = 0.0;
            if (totalReviews == null) totalReviews = 0L;

            // 2. Tính điểm trung bình mới
            // Công thức: (Điểm cũ * số lượng cũ + Điểm mới) / (Số lượng cũ + 1)
            double newRating = ((currentRating * totalReviews) + review.getRating()) / (totalReviews + 1);
            long newTotalReviews = totalReviews + 1;

            // 3. Thực hiện ghi dữ liệu
            transaction.set(newReviewRef, review); // Lưu review

            transaction.update(doctorRef, "rating", newRating); // Cập nhật bác sĩ
            transaction.update(doctorRef, "reviewCount", newTotalReviews);

            // ⭐️ QUAN TRỌNG: Đánh dấu lịch hẹn này đã được review
            transaction.update(apptRef, "isReviewed", true);

            return null;
        }).addOnSuccessListener(aVoid -> {
            Log.d("ReviewRepo", "Đánh giá thành công!");
            listener.onComplete(true);
        }).addOnFailureListener(e -> {
            Log.e("ReviewRepo", "Lỗi transaction đánh giá", e);
            listener.onComplete(false);
        });
    }
}