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
        switch (position) {
            case 0:
                return AzAppConfigDemoFragment.newInstance();
            case 1:
                return AzCognitiveDemoFragment.newInstance();
            default:
                return null;
        }
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "azConfig";
            case 1:
                return "azCognitive";
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return 2;
    }
}
