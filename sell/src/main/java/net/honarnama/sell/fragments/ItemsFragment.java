package net.honarnama.sell.fragments;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import net.honarnama.GRPCUtils;
import net.honarnama.base.BuildConfig;
import net.honarnama.core.fragment.HonarnamaBaseFragment;
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

import android.app.Activity;
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
        net.honarnama.nano.Item item = mAdapter.getItem(i);
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
            displayProgressDialog();
        }

        @Override
        protected GetItemsReply doInBackground(Void... voids) {
            RequestProperties rp = GRPCUtils.newRPWithDeviceInfo();
            SimpleRequest simpleRequest = new SimpleRequest();
            simpleRequest.requestProperties = rp;

            GetItemsReply getItemsReply;
            if (BuildConfig.DEBUG) {
                logD("Request for getting items is: " + simpleRequest);
            }
            try {
                SellServiceGrpc.SellServiceBlockingStub stub = GRPCUtils.getInstance().getSellServiceGrpc();
                getItemsReply = stub.getItems(simpleRequest);
                return getItemsReply;
            } catch (InterruptedException e) {
                logE("Error running getItems request. Error: " + e);
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
                            case GetItemsReply.STORE_NOT_CREATED:
                                //TODO ask server to turn this to Stroe_not_created
                                break;

                            case GetItemReply.NO_CLIENT_ERROR:
                                logE("Got NO_CLIENT_ERROR code for getItemsReply. User id: " + HonarnamaUser.getId());
                                displayShortToast(getString(R.string.error_occured));
                                break;
                        }
                        break;

                    case ReplyProperties.SERVER_ERROR:
                        displayShortToast(getString(R.string.server_error_try_again));
                        break;

                    case ReplyProperties.NOT_AUTHORIZED:
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
                displayLongToast(getString(R.string.error_connecting_to_Server) + getString(R.string.check_net_connection));
            }
        }
    }

    private void dismissProgressDialog() {
        Activity activity = getActivity();
        if (activity != null && !activity.isFinishing()) {
            if (mProgressDialog != null && mProgressDialog.isShowing()) {
                mProgressDialog.dismiss();
            }
        }
    }

    private void displayProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(getActivity());
            mProgressDialog.setCancelable(false);
            mProgressDialog.setMessage(getString(R.string.please_wait));
        }
        if (getActivity() != null && isVisible()) {
            mProgressDialog.show();
        }
    }
}
