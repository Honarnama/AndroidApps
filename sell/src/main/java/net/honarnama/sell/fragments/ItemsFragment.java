package net.honarnama.sell.fragments;

import net.honarnama.HonarnamaBaseApp;
import net.honarnama.base.BuildConfig;
import net.honarnama.core.fragment.HonarnamaBaseFragment;
import net.honarnama.core.model.Item;
import net.honarnama.core.model.Store;
import net.honarnama.core.utils.HonarnamaUser;
import net.honarnama.core.utils.NetworkManager;
import net.honarnama.sell.HonarnamaSellApp;
import net.honarnama.sell.R;
import net.honarnama.sell.activity.ControlPanelActivity;
import net.honarnama.sell.adapter.ItemsAdapter;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
//        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        final SharedPreferences sharedPref = HonarnamaBaseApp.getInstance().getSharedPreferences(HonarnamaUser.getCurrentUser().getUsername(), Context.MODE_PRIVATE);
        if (!sharedPref.getBoolean(HonarnamaSellApp.PREF_LOCAL_DATA_STORE_FOR_ITEM_SYNCED, false)) {

            if (!NetworkManager.getInstance().isNetworkEnabled(true) || !sharedPref.getBoolean(HonarnamaSellApp.PREF_LOCAL_DATA_STORE_SYNCED, false)) {

                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putBoolean(HonarnamaBaseApp.PREF_LOCAL_DATA_STORE_SYNCED, false);
                editor.commit();

                Intent intent = new Intent(getActivity(), ControlPanelActivity.class);
                getActivity().finish();
                startActivity(intent);
            }

        }

        final View rootView = inflater.inflate(R.layout.fragment_items, container, false);
        ListView listView = (ListView) rootView.findViewById(R.id.items_listView);
        listView.setEmptyView(rootView.findViewById(R.id.empty_list_view));

        Store.checkIfUserHaveStore(getActivity()).continueWith(new Continuation<Boolean, Object>() {
            @Override
            public Object then(Task<Boolean> task) throws Exception {
                if ((task.isFaulted() || (task.isCompleted() && task.getResult() == false))) {
                    rootView.findViewById(R.id.no_store_warning_container).setVisibility(View.VISIBLE);
                }
                return null;
            }
        });


        final ProgressDialog progressDialog = new ProgressDialog(getActivity());
        progressDialog.setCancelable(false);
        progressDialog.setMessage(getString(R.string.please_wait));
        progressDialog.show();

        Item.getUserItems(getActivity()).continueWith(new Continuation<List<Item>, Object>() {
            @Override
            public Object then(Task<List<Item>> task) throws Exception {
                progressDialog.dismiss();
                if ((isVisible()) && !NetworkManager.getInstance().isNetworkEnabled(true)) {
                    Toast.makeText(getActivity(), getString(R.string.connec_to_see_updated_notif_message), Toast.LENGTH_LONG).show();
                }
                if (task.isFaulted()) {
                    logE("Getting User Items Failed. Error: "+ task.getError(), "", task.getError());
                    if (isVisible()) {
                        Toast.makeText(getActivity(), getString(R.string.error_getting_items_list) + getString(R.string.please_check_internet_connection), Toast.LENGTH_LONG);
                    }
                } else {
                    List<Item> itemList = task.getResult();
                    mAdapter.addAll(itemList);
                    TextView emptyListTextView = (TextView) rootView.findViewById(R.id.empty_list_view);
                    emptyListTextView.setText(getString(R.string.has_not_registered_any_store));
                    mAdapter.notifyDataSetChanged();
                }
                return null;
            }
        });


        mAdapter = new ItemsAdapter(getActivity());
        listView.setAdapter(mAdapter);
        listView.setOnItemClickListener(this);

        return rootView;
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        Item item = (Item) mAdapter.getItem(i);
        ControlPanelActivity controlPanelActivity = (ControlPanelActivity) getActivity();
        controlPanelActivity.switchFragmentToEditItem(item.getObjectId());
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main, menu);
    }
}
