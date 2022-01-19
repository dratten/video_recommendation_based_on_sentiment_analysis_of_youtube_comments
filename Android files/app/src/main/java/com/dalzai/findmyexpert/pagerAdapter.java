package com.dalzai.findmyexpert;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public class pagerAdapter extends FragmentPagerAdapter {
    private int myNumOfTabs;
    public pagerAdapter(@NonNull FragmentManager fm, int numOfTabs) {
        super(fm, numOfTabs);
        this.myNumOfTabs = numOfTabs;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position)
        {
            case 0: return new HomeFragment();
            case 1: return new SearchFragment();
            case 2: return new HomeFragment();
            case 3: return new HomeFragment();
            case 4: return new ProfileFragment();
            default:return null;
        }
    }

    @Override
    public int getCount() {
        return myNumOfTabs;
    }
}
