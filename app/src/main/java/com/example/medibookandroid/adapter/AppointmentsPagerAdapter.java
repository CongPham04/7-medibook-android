package com.example.medibookandroid.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import com.example.medibookandroid.AppointmentsListFragment;

public class AppointmentsPagerAdapter extends FragmentStateAdapter {

    public AppointmentsPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return AppointmentsListFragment.newInstance("Upcoming");
            case 1:
                return AppointmentsListFragment.newInstance("History");
            case 2:
                return AppointmentsListFragment.newInstance("Canceled");
            default:
                return null;
        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}
