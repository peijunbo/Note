package com.example.note;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.List;

public class MyPagerAdapter extends FragmentStateAdapter {
    private List<Fragment> pageList;

    public MyPagerAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle, List<Fragment> pageList) {
        super(fragmentManager, lifecycle);
        this.pageList = pageList;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return pageList.get(position);
    }

    @Override
    public int getItemCount() {
        return pageList.size();
    }
}
