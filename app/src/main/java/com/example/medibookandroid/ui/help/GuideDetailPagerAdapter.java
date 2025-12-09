// File 4: GuideDetailPagerAdapter.java
package com.example.medibookandroid.ui.help;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class GuideDetailPagerAdapter extends FragmentStateAdapter {
    public GuideDetailPagerAdapter(@NonNull Fragment f) {
        super(f);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return GuideDetailFragment.newInstance(position == 0);
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}