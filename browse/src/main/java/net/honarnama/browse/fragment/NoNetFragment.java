package net.honarnama.browse.fragment;

import net.honarnama.browse.R;
import net.honarnama.browse.activity.ControlPanelActivity;
import net.honarnama.base.utils.NetworkManager;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

/**
 * Created by elnaz on 3/12/16.
 */
public class NoNetFragment extends HonarnamaBrowseFragment implements View.OnClickListener {
    public ImageView mRetryIcon;
    public Context mContext;
    public static NoNetFragment mNoNetFragment;

    public synchronized static NoNetFragment getInstance() {
//        if (mNoNetFragment == null) {
        mNoNetFragment = new NoNetFragment();
//        }
        return mNoNetFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_no_network, container, false);
        mRetryIcon = (ImageView) rootView.findViewById(R.id.no_network_fragment_retry_icon);
        mRetryIcon.setOnClickListener(this);
        return rootView;
    }

    @Override
    public String getTitle(Context context) {
        mContext = context;
        return context.getString(R.string.hornama);
    }

    @Override
    public void onSelectedTabClick() {

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.no_network_fragment_retry_icon:
                if (!NetworkManager.getInstance().isNetworkEnabled(true)) {
                    return;
                }
                ControlPanelActivity controlPanelActivity = (ControlPanelActivity) getActivity();
                controlPanelActivity.refreshNoNetFragment();
                break;
        }
    }
}
