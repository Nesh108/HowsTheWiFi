package com.kulplex.nesh.howsthewifi;


import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

public class PagerAdapter extends FragmentStatePagerAdapter {
    int mNumOfTabs;
    MainFragment mainFrag;
    MapFragment mapFrag;
    SettingsFragment settFrag;

    public PagerAdapter(FragmentManager fm, int NumOfTabs) {
        super(fm);
        this.mNumOfTabs = NumOfTabs;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                mainFrag = new MainFragment();
                return mainFrag;
            case 1:
                mapFrag = new MapFragment();
                return mapFrag;
            case 2:
                settFrag = new SettingsFragment();
                return settFrag;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return mNumOfTabs;
    }

    public MainFragment getMainFrag() {
        return mainFrag;
    }

    public MapFragment getMapFrag() {
        return mapFrag;
    }
}