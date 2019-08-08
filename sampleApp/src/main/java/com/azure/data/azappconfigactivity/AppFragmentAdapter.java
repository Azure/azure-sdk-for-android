package com.azure.data.azappconfigactivity;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public class AppFragmentAdapter extends FragmentPagerAdapter {
    public AppFragmentAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        if (position == 0) {
            return AzAppConfigDemoFragment.newInstance();
        } else if (position == 1) {
            return AzCognitiveFragment.newInstance();
        } else {
            return null;
        }
    }

    @Override
    public CharSequence getPageTitle(int position) {
        if (position == 0) {
            return "azConfig";
        } else if (position == 1) {
            return "azCognitive";
        } else {
            return null;
        }
    }

    @Override
    public int getCount() {
        return 2;
    }
}
