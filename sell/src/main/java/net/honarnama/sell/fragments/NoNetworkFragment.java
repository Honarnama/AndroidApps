package net.honarnama.sell.fragments;


import net.honarnama.core.fragment.HonarnamaBaseFragment;
import net.honarnama.core.utils.NetworkManager;
import net.honarnama.sell.R;
import net.honarnama.sell.activity.ControlPanelActivity;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

/**
 * A simple {@link Fragment} subclass.
 */
public class NoNetworkFragment extends HonarnamaBaseFragment implements View.OnClickListener {

    public static NoNetworkFragment mNoNetworkFragment;
    public ImageView mRetryIcon;

    public synchronized static NoNetworkFragment getInstance() {
        if (mNoNetworkFragment == null) {
            mNoNetworkFragment = new NoNetworkFragment();
        }
        return mNoNetworkFragment;
    }

    @Override
    public String getTitle(Context context) {
        return context.getString(R.string.default_app_title);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_no_network, container, false);
        mRetryIcon = (ImageView) rootView.findViewById(R.id.no_network_fragment_retry_icon);
        mRetryIcon.setOnClickListener(this);
        return rootView;
    }


    @Override
    public void onClick(View view) {

        switch (view.getId()) {

            case R.id.no_network_fragment_retry_icon:
                if (NetworkManager.getInstance().isNetworkEnabled(getActivity(), true)) {
                    Intent intent = new Intent(getActivity(), ControlPanelActivity.class);
                    getActivity().finish();
                    startActivity(intent);
                }
                break;
        }

    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }
}
