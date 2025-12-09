// File 2: HelpPagerAdapter.java
package com.example.medibookandroid.ui.help;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class HelpPagerAdapter extends FragmentStateAdapter {
    public HelpPagerAdapter(@NonNull FragmentActivity fa) {
        super(fa);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return position == 0 ? new GuideMainFragment() : new FaqFragment();
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}