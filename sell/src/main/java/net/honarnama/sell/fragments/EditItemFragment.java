package net.honarnama.sell.fragments;

import net.honarnama.sell.R;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


public class EditItemFragment extends Fragment {

    public static EditItemFragment newInstance() {
        return new EditItemFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_edit_item, container, false);
        return rootView;
    }

}
