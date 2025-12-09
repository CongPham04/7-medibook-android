// File 6: FaqFragment.java
package com.example.medibookandroid.ui.help;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.medibookandroid.databinding.FragmentGuideDetailBinding;
import java.util.ArrayList;

public class FaqFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        FragmentGuideDetailBinding binding = FragmentGuideDetailBinding.inflate(inflater, container, false);
        binding.rvGuide.setLayoutManager(new LinearLayoutManager(getContext()));

        var list = new ArrayList<GuideItem>();
        list.add(new GuideItem("Đăng nhập không thành công", "Kiểm tra lại email và mật khẩu. Nếu quên → chọn \"Quên mật khẩu\""));
        list.add(new GuideItem("Không thể đặt lịch", "Khung giờ này đã đầy, vui lòng chọn khung giờ khác còn trống hoặc chọn bác sĩ khác"));
        list.add(new GuideItem("Lỗi kết nối", "Kiểm tra Internet (WiFi/4G). Thử tắt mở lại ứng dụng"));
        list.add(new GuideItem("Lỗi Bác sĩ tạo lịch", "Nhận thông báo \"Ca khám trùng thời gian\" → vui lòng điều chỉnh lại thời gian"));

        binding.rvGuide.setAdapter(new GuideAdapter(list));
        return binding.getRoot();
    }
}