package com.example.medibookandroid.ui.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
// ⭐️ SỬA: Xóa import FragmentActivity
// import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
// ⭐️ SỬA: Đảm bảo đường dẫn này đúng
import com.example.medibookandroid.ui.common.AppointmentsListFragment;

public class AppointmentsPagerAdapter extends FragmentStateAdapter {

    // ⭐️ SỬA 1: Nhận vào Fragment, không phải FragmentActivity
    public AppointmentsPagerAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                // ⭐️ SỬA 2: Đảm bảo class name này đúng
                return AppointmentsListFragment.newInstance("Upcoming");
            case 1:
                return AppointmentsListFragment.newInstance("History");
            case 2:
                return AppointmentsListFragment.newInstance("Canceled");
            default:
                // Trả về một fragment rỗng để tránh crash
                return new Fragment();
        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}
