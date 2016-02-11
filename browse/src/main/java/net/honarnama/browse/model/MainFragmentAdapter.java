package net.honarnama.browse.model;

import net.honarnama.browse.fragment.ChildFragment;
import net.honarnama.browse.fragment.ItemsFragment;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import static net.honarnama.browse.widget.MainTabBar.TAB_HOME;
import static net.honarnama.browse.widget.MainTabBar.TAB_CATS;
import static net.honarnama.browse.widget.MainTabBar.TAB_SHOPS;
import static net.honarnama.browse.widget.MainTabBar.TAB_FAVS;

/**
 * Created by elnaz on 2/10/16.
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
    public Fragment getItem(int position) {
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

//            case TAB_CATS:
//                return CatsFragment.newInstance();
//
//            case TAB_SHOPS:
//                return ShopsFragment.newInstance(null);
//
//            case TAB_FAVS:
//                return FavsFragment.newInstance();
        }
        return null;
    }
}
