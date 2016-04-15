package net.honarnama.sell.fragments;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import net.honarnama.GRPCUtils;
import net.honarnama.core.fragment.HonarnamaBaseFragment;
import net.honarnama.core.model.Item;
import net.honarnama.core.utils.NetworkManager;
import net.honarnama.nano.GetItemReply;
import net.honarnama.nano.GetItemsReply;
import net.honarnama.nano.ReplyProperties;
import net.honarnama.nano.RequestProperties;
import net.honarnama.nano.SellServiceGrpc;
import net.honarnama.nano.SimpleRequest;
import net.honarnama.sell.HonarnamaSellApp;
import net.honarnama.sell.R;
import net.honarnama.sell.activity.ControlPanelActivity;
import net.honarnama.sell.adapter.ItemsAdapter;
import net.honarnama.sell.model.HonarnamaUser;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import io.fabric.sdk.android.services.concurrency.AsyncTask;


public class ItemsFragment extends HonarnamaBaseFragment implements AdapterView.OnItemClickListener {

    ItemsAdapter mAdapter;
    public static ItemsFragment mItemsFragment;
    private Tracker mTracker;

    ProgressDialog mProgressDialog;
    private View mEmptyListView;

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

        mTracker = HonarnamaSellApp.getInstance().getDefaultTracker();
        mTracker.setScreenName("ItemsFragment");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if (!NetworkManager.getInstance().isNetworkEnabled(true)) {
            Intent intent = new Intent(getActivity(), ControlPanelActivity.class);
            getActivity().finish();
            startActivity(intent);
        }

        final View rootView = inflater.inflate(R.layout.fragment_items, container, false);
        ListView listView = (ListView) rootView.findViewById(R.id.items_listView);
        mEmptyListView = rootView.findViewById(R.id.empty_list_view);
        listView.setEmptyView(mEmptyListView);

        new getItemsAsync().execute();

        mAdapter = new ItemsAdapter(getActivity());
        listView.setAdapter(mAdapter);
        listView.setOnItemClickListener(this);

        return rootView;
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        net.honarnama.nano.Item item = (net.honarnama.nano.Item) mAdapter.getItem(i);
        ControlPanelActivity controlPanelActivity = (ControlPanelActivity) getActivity();
        controlPanelActivity.switchFragmentToEditItem(item.id);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main, menu);
    }

    public class getItemsAsync extends AsyncTask<Void, Void, GetItemsReply> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (mProgressDialog == null) {
                mProgressDialog = new ProgressDialog(getActivity());
                mProgressDialog.setCancelable(false);
                mProgressDialog.setMessage(getString(R.string.please_wait));
            }
            if (getActivity() != null && isVisible()) {
                //TODO check if this checking prevents exception or not
                //TODO  if this checking prevents add to others dialog too
                mProgressDialog.show();
            }
        }

        @Override
        protected GetItemsReply doInBackground(Void... voids) {
            RequestProperties rp = GRPCUtils.newRPWithDeviceInfo();
            SimpleRequest simpleRequest = new SimpleRequest();
            simpleRequest.requestProperties = rp;

            GetItemsReply getItemsReply;

            try {
                SellServiceGrpc.SellServiceBlockingStub stub = GRPCUtils.getInstance().getSellServiceGrpc();
                getItemsReply = stub.getItems(simpleRequest);
                return getItemsReply;
            } catch (InterruptedException e) {
                logE("Error getting user info. Error: " + e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(GetItemsReply getItemsReply) {
            super.onPostExecute(getItemsReply);
            dismissProgressDialog();
            if (getItemsReply != null) {
                switch (getItemsReply.replyProperties.statusCode) {
                    case ReplyProperties.UPGRADE_REQUIRED:
                        ControlPanelActivity controlPanelActivity = ((ControlPanelActivity) getActivity());
                        if (controlPanelActivity != null) {
                            controlPanelActivity.displayUpgradeRequiredDialog();
                        }
                        break;
                    case ReplyProperties.CLIENT_ERROR:
                        switch (getItemsReply.errorCode) {
                            case GetItemsReply.STORE_NOT_FOUND:
                                //TODO does this error arise at all?
                                break;

                            case GetItemReply.NO_CLIENT_ERROR:
                                //TODO bug report
                                break;
                        }
                        break;

                    case ReplyProperties.SERVER_ERROR:
                        if (isVisible()) {
                            Toast.makeText(getActivity(), getString(R.string.error_getting_items_list) + getString(R.string.please_check_internet_connection), Toast.LENGTH_LONG);
                        }
                        break;

                    case ReplyProperties.NOT_AUTHORIZED:
                        //TODO toast
                        HonarnamaUser.logout(getActivity());
                        break;

                    case ReplyProperties.OK:
                        net.honarnama.nano.Item itemList[] = getItemsReply.items;
                        mAdapter.setItems(itemList);
                        TextView emptyListTextView = (TextView) mEmptyListView;
                        emptyListTextView.setText(getString(R.string.has_not_registered_any_store));
                        mAdapter.notifyDataSetChanged();
                        break;
                }

            } else {
                //TODO toast
            }
        }
    }

    private void dismissProgressDialog() {
        if (!getActivity().isFinishing()) {
            if (mProgressDialog != null && mProgressDialog.isShowing()) {
                mProgressDialog.dismiss();
            }
        }
    }
}
