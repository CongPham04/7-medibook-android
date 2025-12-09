package com.example.medibookandroid.ui.help;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.medibookandroid.databinding.FragmentGuideDetailBinding;
import java.util.ArrayList;
import java.util.List;

public class GuideDetailFragment extends Fragment {
    private FragmentGuideDetailBinding binding;
    private boolean isPatient;

    public static GuideDetailFragment newInstance(boolean isPatient) {
        GuideDetailFragment f = new GuideDetailFragment();
        f.isPatient = isPatient;
        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentGuideDetailBinding.inflate(inflater, container, false);
        binding.rvGuide.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvGuide.setAdapter(new GuideAdapter(isPatient ? getPatientData() : getDoctorData()));
        return binding.getRoot();
    }

    private List<GuideItem> getPatientData() {
        List<GuideItem> list = new ArrayList<>();
        list.add(new GuideItem("1. Bắt đầu (Đăng ký và Đăng nhập)",
                "1. Đăng ký\n" +
                        "• Tại màn hình Chào mừng, chọn \"Đăng Ký\".\n" +
                        "• Nhập Họ và tên, Email, Số điện thoại, Mật khẩu.\n" +
                        "• Tích vào ô đồng ý với Điều khoản sử dụng.\n" +
                        "• Nhấn \"Đăng Ký\" để hoàn tất.\n\n" +
                        "2. Đăng nhập\n" +
                        "• Nhập Email và Mật khẩu đã đăng ký.\n" +
                        "• Nhấn \"Đăng Nhập\"."));

        list.add(new GuideItem("3. Quản lý Hồ sơ",
                "Truy cập tab Hồ sơ → Chọn Chỉnh sửa Hồ sơ để thay đổi thông tin cá nhân"));

        list.add(new GuideItem("1. Tìm kiếm Bác sĩ",
                "Tại Trang chủ, sử dụng thanh tìm kiếm phía trên → Nhập tên hoặc Chuyên khoa"));

        list.add(new GuideItem("2. Đặt Lịch Khám",
                "• Chọn bác sĩ → Xem lịch làm việc\n" +
                        "• Chọn Ngày phù hợp → Chọn Khung giờ còn trống\n" +
                        "• Nhập Lý do khám\n" +
                        "• Nhấn \"Đặt lịch hẹn\" → Chờ xác nhận"));

        list.add(new GuideItem("3. Quản lý Lịch Hẹn",
                "Vào tab Lịch → Xem Sắp tới / Đã hủy → Hủy nếu cần"));

        list.add(new GuideItem("4. Xem Thông báo",
                "Tab chuông → Xem lịch được xác nhận, bác sĩ hủy, nhắc lịch..."));

        return list;
    }

    private List<GuideItem> getDoctorData() {
        List<GuideItem> list = new ArrayList<>();
        list.add(new GuideItem("1. Đăng nhập",
                "Bác sĩ dùng email & mật khẩu do hệ thống cung cấp"));

        list.add(new GuideItem("1. Quản lý Lịch Làm việc",
                "• Vào tab Lịch\n" +
                        "• Chọn ngày muốn làm việc\n" +
                        "• Nhấn \"+\" để tạo ca làm việc mới\n" +
                        "• Nhập Giờ bắt đầu và Giờ kết thúc"));

        list.add(new GuideItem("2. Quản lý Yêu cầu Đặt Lịch",
                "• Vào tab Yêu Cầu\n" +
                        "• Xem chi tiết bệnh nhân\n" +
                        "• Nhấn \"Chấp Nhận\" hoặc \"Từ Chối\""));

        list.add(new GuideItem("3. Xem Lịch Hẹn Đã Xác nhận",
                "Vào Lịch Trình → Xem theo ngày"));

        list.add(new GuideItem("4. Quản lý Hồ sơ",
                "Cập nhật Chuyên khoa, Bằng cấp, Nơi công tác..."));

        return list;
    }
}
