package net.honarnama.browse.fragment;

import net.honarnama.browse.R;
import net.honarnama.browse.activity.ControlPanelActivity;
import net.honarnama.browse.widget.MainTabBar;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.lang.reflect.Field;

import static net.honarnama.browse.widget.MainTabBar.TAB_HOME;


/**
 * Created by elnaz on 2/11/16.
 */
public class ChildFragment extends HonarnamaBrowseFragment {

    private static String CHILD_FRAGMENT_TAG = "child_fragment_tag";

    private int mTag;

    private long mLastVisitTime = 0;

    private static final long TIME_DELAY_TO_RESET_TAB = 5 * 60 * 1000; // 5 minutes

    public static ChildFragment mChildFragment;

    public static ChildFragment getInstance(int tag) {
        ChildFragment childFragment = new ChildFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(CHILD_FRAGMENT_TAG, tag);
        childFragment.setArguments(bundle);
        return childFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View root = inflater.inflate(R.layout.fragment_child, container, false);
        mTag = getArguments().getInt(CHILD_FRAGMENT_TAG);
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            //Restore the fragment's state here
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //Save the fragment's state here
    }

    @Override
    public String getTitle(Context context) {
        return null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public boolean hasContent() {
        return getChildFragmentManager().getBackStackEntryCount() > 0;
    }

    public int getNumberOfChild() {
        return getChildFragmentManager().getBackStackEntryCount();
    }


    public boolean back() {
        if (getChildFragmentManager().getBackStackEntryCount() > 1) {
            Fragment fragment = getChildFragmentManager().getFragments()
                    .get(getChildFragmentManager().getBackStackEntryCount() - 1);
            if (fragment != null) {
                getChildFragmentManager().popBackStackImmediate();
                TextView toolbarTitle = (TextView) ((ControlPanelActivity) getActivity()).findViewById(R.id.toolbar_title);
                toolbarTitle.setText(getString(R.string.hornama));

                MainTabBar mainTabBar = (MainTabBar) ((ControlPanelActivity) getActivity()).findViewById(R.id.tab_bar);
                return true;
            }

        }
        return false;
    }

    /**
     * pop all fragment and keep first one only
     */
    public void popAllFragment() {
        while (getChildFragmentManager().getBackStackEntryCount() > 1) {
            getChildFragmentManager().popBackStackImmediate();
        }
    }

    /**
     * pop all dialog fragment
     *
     * public void popAllDialogFragment() {
     * if (getChildFragmentManager().getFragments() != null) {
     * for (Fragment fragment : getChildFragmentManager().getFragments()) {
     * if (fragment != null && fragment.getTag() != null && fragment.getTag()
     * .equals(HomeActivity.DIALOG_FRAGMENT_TAG)) {
     * ((DialogFragment) fragment).dismiss();
     * }
     * }
     * }
     * }
     */

    @Override
    public void onDetach() {
        super.onDetach();
        try {
            Field childFragmentManager = Fragment.class.getDeclaredField("mChildFragmentManager");
            childFragmentManager.setAccessible(true);
            childFragmentManager.set(this, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onSelectedTabClick() {
        if (getChildFragmentManager().getBackStackEntryCount() > 1) {
            popAllFragment();
        } else {
            if (getChildFragmentManager().getFragments() != null
                    && getChildFragmentManager().getFragments().size() > 0) {
                ((HonarnamaBrowseFragment) getChildFragmentManager().getFragments().get(0))
                        .onSelectedTabClick();
            }
        }
    }

    public void onTabClick() {
        long now = System.currentTimeMillis();
        if (mLastVisitTime > 0) {
            if (now - mLastVisitTime > TIME_DELAY_TO_RESET_TAB) { // there's plenty of time that user hasn't visited this tab, she wouldn't remember existing content, so reset this tab
                popAllFragment();
            }
        }
        mLastVisitTime = now;

//        if (mTag == MainTabBar.TAB_SEARCH && getChildFragmentManager().getFragments() != null
//                && getChildFragmentManager().getFragments().size() > 0) {
//            ((SearchFragment) getChildFragmentManager().getFragments().get(0))
//                    .updateKeyboardStatus();
//        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (getChildFragmentManager() != null
                && getChildFragmentManager().getBackStackEntryCount() > 0) {
            getChildFragmentManager().getFragments()
                    .get(getChildFragmentManager().getBackStackEntryCount() - 1).onActivityResult(
                    requestCode, resultCode, data);
        }
    }
}