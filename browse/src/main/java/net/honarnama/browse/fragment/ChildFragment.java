package net.honarnama.browse.fragment;

import net.honarnama.browse.R;
import net.honarnama.browse.activity.ControlPanelActivity;
import net.honarnama.browse.widget.MainTabBar;
import net.honarnama.core.fragment.HonarnamaBaseFragment;
import net.honarnama.core.utils.NetworkManager;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Field;
import java.util.List;


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
        FragmentManager childFragmentManager = getChildFragmentManager();

        List<Fragment> fragments = childFragmentManager.getFragments();
        if (fragments != null) {
            for (int i = 0; i < fragments.size(); i++) {
                if (fragments.get(i) != null)
                    logE("inja fragment " + i + " is " + fragments.get(i).getClass().getName());
            }
        }

        logE("inja backStackEntryCount() " + childFragmentManager.getBackStackEntryCount());


        if (childFragmentManager.getBackStackEntryCount() > 1) {
            HonarnamaBaseFragment topFragment = (HonarnamaBaseFragment) childFragmentManager.findFragmentById(R.id.child_fragment_root);
            if (topFragment != null) {
//                topFragment = (HonarnamaBaseFragment) childFragmentManager.findFragmentById(R.id.child_fragment_root);
                ControlPanelActivity controlPanelActivity = (ControlPanelActivity) getActivity();
                if (topFragment instanceof NoNetFragment) {
                    if (NetworkManager.getInstance().isNetworkEnabled(false)) {
                        controlPanelActivity.refreshNoNetFragment();
                        return true;
                    } else {

                        logE("inja removing topfrag " + topFragment.getClass().getName());
                        FragmentTransaction fragmentTransaction = childFragmentManager.beginTransaction();
//                        fragmentTransaction.remove(topFragment);
//                        fragmentTransaction.commitAllowingStateLoss();

                        if (childFragmentManager != null) {
                            childFragmentManager.popBackStack();
                            childFragmentManager.executePendingTransactions();
                        }

                        fragmentTransaction = childFragmentManager.beginTransaction();
                        topFragment = (HonarnamaBaseFragment) childFragmentManager.findFragmentById(R.id.child_fragment_root);

                        if (topFragment != null) {
                            logE("inja removing pre nonet frag " + topFragment.getClass().getName());
//                            fragmentTransaction.remove(topFragment);
//                            fragmentTransaction.commitAllowingStateLoss();
                            childFragmentManager.popBackStack();
                            if (childFragmentManager != null) {
                                childFragmentManager.executePendingTransactions();
                            }
                        }

                        logE("2 inja backStackEntryCount() " + childFragmentManager.getBackStackEntryCount());

                        if (childFragmentManager.getBackStackEntryCount() > 0) {
                            topFragment = (HonarnamaBaseFragment) childFragmentManager.findFragmentById(R.id.child_fragment_root);
                            TextView toolbarTitle = (TextView) controlPanelActivity.findViewById(R.id.toolbar_title);
                            toolbarTitle.setText(topFragment.getTitle(controlPanelActivity));
                            return true;

                        } else {
                            return false;
                        }
                    }
                } else {
                    childFragmentManager.popBackStackImmediate();
                    childFragmentManager.executePendingTransactions();
                    topFragment = (HonarnamaBaseFragment) childFragmentManager.findFragmentById(R.id.child_fragment_root);
                    TextView toolbarTitle = (TextView) controlPanelActivity.findViewById(R.id.toolbar_title);
                    toolbarTitle.setText(topFragment.getTitle(controlPanelActivity));

                }
//                TextView toolbarTitle = (TextView) controlPanelActivity.findViewById(R.id.toolbar_title);
//                toolbarTitle.setText(getString(R.string.hornama));

//                MainTabBar mainTabBar = (MainTabBar) ((ControlPanelActivity) getActivity()).findViewById(R.id.tab_bar);
                return true;
            }

        }
        return false;
    }

    /**
     * pop all fragment and keep first one only
     */
    public void popAllFragment() {
        try {
            while (getChildFragmentManager().getBackStackEntryCount() > 1) {
                getChildFragmentManager().popBackStackImmediate();
            }
        } catch (Exception e) {
            logE("Exception while popping all fragments: " + e);
        }
        ControlPanelActivity controlPanelActivity = (ControlPanelActivity) getActivity();
        HonarnamaBaseFragment topFragment = (HonarnamaBaseFragment) getChildFragmentManager().findFragmentById(R.id.child_fragment_root);
        controlPanelActivity.setTitle(topFragment.getTitle(controlPanelActivity));
    }

    @Override
    public void onDetach() {
        super.onDetach();
        try {
            Field childFragmentManager = Fragment.class.getDeclaredField("mChildFragmentManager");
            childFragmentManager.setAccessible(true);
            childFragmentManager.set(this, null);
        } catch (NoSuchFieldException e) {
            logE("NoSuchFieldException" + e);
        } catch (IllegalAccessException e) {
            logE("IllegalAccessException" + e);
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
//        ControlPanelActivity controlPanelActivity = (ControlPanelActivity) getActivity();
//        controlPanelActivity.refreshTopFragment();
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

        Toast.makeText(getActivity(), "inja requestCode in ChildFrag is " + requestCode, Toast.LENGTH_SHORT).show();

        FragmentManager childFragmentManager = getChildFragmentManager();
        if (childFragmentManager != null
                && childFragmentManager.getBackStackEntryCount() > 0) {

            List<Fragment> fragments = childFragmentManager.getFragments();
            if (fragments != null) {
                for (Fragment fragment : fragments) {
                    Toast.makeText(getActivity(), "inja Calling onActivityResult of " + fragment.getClass().getName(), Toast.LENGTH_SHORT).show();

                    fragment.onActivityResult(requestCode, resultCode, data);
                }
            }

//            HonarnamaBaseFragment topFragment = (HonarnamaBaseFragment) childFragmentManager.getFragments().get(childFragmentManager.getBackStackEntryCount()-1);
//
//            HonarnamaBaseFragment topFragment = (HonarnamaBaseFragment) childFragmentManager.findFragmentById(R.id.child_fragment_root);
//
//            Toast.makeText(getActivity(), "inja topFragment is " + topFragment.getClass().getName(), Toast.LENGTH_SHORT).show();
//
//
//            topFragment.onActivityResult(requestCode, resultCode, data);
        }

        super.onActivityResult(requestCode, resultCode, data);

    }
}