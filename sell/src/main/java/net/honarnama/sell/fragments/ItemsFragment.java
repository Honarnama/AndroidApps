package net.honarnama.sell.fragments;

import com.parse.ParseObject;

import net.honarnama.HonarnamaBaseApp;
import net.honarnama.base.BuildConfig;
import net.honarnama.core.fragment.HonarnamaBaseFragment;
import net.honarnama.sell.R;
import net.honarnama.sell.activity.ControlPanelActivity;
import net.honarnama.sell.adapter.ItemsAdapter;
import net.honarnama.sell.model.Item;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.List;

import bolts.Continuation;
import bolts.Task;


public class ItemsFragment extends HonarnamaBaseFragment implements AdapterView.OnItemClickListener {

    ItemsAdapter mAdapter;

    public static ItemsFragment mItemsFragment;

    @Override
    public String getTitle(Context context) {
        return context.getString(R.string.nav_title_items);
    }

    public synchronized static ItemsFragment getInstance() {
        if (mItemsFragment == null) {
            mItemsFragment = new ItemsFragment();
        }
        return mItemsFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_items, container, false);
        final ListView listView = (ListView) rootView.findViewById(R.id.items_listView);

        final ProgressDialog progressDialog = new ProgressDialog(getActivity());
        progressDialog.setCancelable(false);
        progressDialog.setMessage(getString(R.string.please_wait));
        progressDialog.show();

        Item.getUserItems(getActivity()).continueWith(new Continuation<List<Item>, Object>() {
            @Override
            public Object then(Task<List<Item>> task) throws Exception {
                progressDialog.dismiss();
                if (task.isFaulted()) {
                    if (BuildConfig.DEBUG) {
                        Log.e(HonarnamaBaseApp.PRODUCTION_TAG + "/" + getClass().getSimpleName(),
                                "Getting User Items Failed." +
                                        "//" + task.getError().getMessage(), task.getError());
                    } else {
                        Log.e(HonarnamaBaseApp.PRODUCTION_TAG, "Getting User Items Failed. // " + task.getError().getMessage());
                    }
                    Toast.makeText(getActivity(), R.string.error_getting_items_list + R.string.please_check_internet_connection, Toast.LENGTH_LONG);
                } else {
                    mAdapter = new ItemsAdapter(getActivity(), task.getResult());
                    listView.setAdapter(mAdapter);

                }
                return null;
            }
        });
        listView.setOnItemClickListener(this);
        return rootView;
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        Item item = (Item) mAdapter.getItem(i);
        ControlPanelActivity controlPanelActivity = (ControlPanelActivity) getActivity();
        controlPanelActivity.switchFragmentToEditItem(item.getObjectId());
    }
}
