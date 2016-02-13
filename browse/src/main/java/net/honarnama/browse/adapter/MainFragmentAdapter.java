package net.honarnama.browse.adapter;

import net.honarnama.browse.fragment.CatsFragment;
import net.honarnama.browse.fragment.ChildFragment;
import net.honarnama.browse.fragment.FavsFragment;
import net.honarnama.browse.fragment.ItemsFragment;
import net.honarnama.browse.fragment.ShopsFragment;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

import static net.honarnama.browse.widget.MainTabBar.TAB_CATS;
import static net.honarnama.browse.widget.MainTabBar.TAB_FAVS;
import static net.honarnama.browse.widget.MainTabBar.TAB_HOME;
import static net.honarnama.browse.widget.MainTabBar.TAB_SHOPS;

/**
 * Created by elnaz on 2/13/16.
 */
public class MainFragmentAdapter extends FragmentPagerAdapter {
    public List<ChildFragment> fragmentsList = new ArrayList<>();


    public MainFragmentAdapter(FragmentManager fm) {
        super(fm);
        fragmentsList.add(ChildFragment.getInstance(TAB_HOME));
        fragmentsList.add(ChildFragment.getInstance(TAB_CATS));
        fragmentsList.add(ChildFragment.getInstance(TAB_SHOPS));
        fragmentsList.add(ChildFragment.getInstance(TAB_FAVS));
    }

    @Override
    public ChildFragment getItem(int position) {
        //return MyFragment.newInstance();
        return fragmentsList.get(position);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        //return CONTENT[position % CONTENT.length].toUpperCase();
        return "Child Fragment " + position;
    }

    @Override
    public int getCount() {
        // return CONTENT.length;
        return 4;
    }

    public Fragment getDefaultFragmentForTab(int tabTag) {
        switch (tabTag) {
            case TAB_HOME:
                return ItemsFragment.getInstance();

            case TAB_CATS:
                return CatsFragment.getInstance();

            case TAB_SHOPS:
                return ShopsFragment.getInstance();

            case TAB_FAVS:
                return FavsFragment.getInstance();
        }
        return null;
    }
}
