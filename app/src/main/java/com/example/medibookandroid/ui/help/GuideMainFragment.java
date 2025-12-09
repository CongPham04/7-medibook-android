// File 3: GuideMainFragment.java
package com.example.medibookandroid.ui.help;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import com.example.medibookandroid.databinding.FragmentGuideMainBinding;
import com.google.android.material.tabs.TabLayoutMediator;

public class GuideMainFragment extends Fragment {
    private FragmentGuideMainBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentGuideMainBinding.inflate(inflater, container, false);
        binding.vpGuide.setAdapter(new GuideDetailPagerAdapter(this));
        new TabLayoutMediator(binding.tabGuide, binding.vpGuide, (tab, pos) ->
                tab.setText(pos == 0 ? "Bệnh nhân" : "Bác sĩ")
        ).attach();
        return binding.getRoot();
    }
}