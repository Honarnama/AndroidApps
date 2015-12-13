package net.honarnama.browse.fragments;

import net.honarnama.browse.R;
import net.honarnama.browse.activity.LoginActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

/**
 * Created by elnaz on 12/8/15.
 */
public class RecommendedItemsFragment extends Fragment {

    private static RecommendedItemsFragment mRecommendedItemsFragment;
    public synchronized static RecommendedItemsFragment getInstance() {
        if (mRecommendedItemsFragment == null) {
            mRecommendedItemsFragment = new RecommendedItemsFragment();
        }
        return mRecommendedItemsFragment;
    }

    public String getTitle(Context context) {
        return context.getString(R.string.recommended_items);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_recommended_items, container, false);

        Button loginButton = (Button) rootView.findViewById(R.id.customer_login_button);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                startActivity(intent);
            }
        });

        return rootView;

    }
}
