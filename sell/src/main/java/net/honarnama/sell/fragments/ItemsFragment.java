package net.honarnama.sell.fragments;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import net.honarnama.GRPCUtils;
import net.honarnama.base.BuildConfig;
import net.honarnama.base.fragment.HonarnamaBaseFragment;
import net.honarnama.base.utils.NetworkManager;
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
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import io.fabric.sdk.android.services.concurrency.AsyncTask;

public class ItemsFragment extends HonarnamaBaseFragment implements AdapterView.OnItemClickListener {

    ItemsAdapter mAdapter;
    public static ItemsFragment mItemsFragment;
    private Tracker mTracker;

    private View mEmptyListView;
    Snackbar mSnackbar;
    private CoordinatorLayout mCoordinatorLayout;

    @Override
    public String getTitle() {
        return getStringInFragment(R.string.nav_title_items);
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

        final View rootView = inflater.inflate(R.layout.fragment_items, container, false);
        ListView listView = (ListView) rootView.findViewById(R.id.items_listView);
        mEmptyListView = rootView.findViewById(R.id.empty_list_view);
        mCoordinatorLayout = (CoordinatorLayout) rootView.findViewById(R.id
                .coordinatorLayout);
        listView.setEmptyView(mEmptyListView);


        Activity activity = getActivity();
        if (activity != null) {
            mAdapter = new ItemsAdapter(activity);
            listView.setAdapter(mAdapter);
            new getItemsAsync().execute();
            listView.setOnItemClickListener(this);
        }
        return rootView;
    }


    @Override
    public void onResume() {
        super.onResume();
        if (isAdded()) {
            ControlPanelActivity activity = (ControlPanelActivity) getActivity();
            if (activity != null) {
                activity.setTitle(getTitle());
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        if (NetworkManager.getInstance().isNetworkEnabled(true)) {
            Activity activity = getActivity();
            if (activity != null && mAdapter != null) {
                net.honarnama.nano.Item item = mAdapter.getItem(i);
                ControlPanelActivity controlPanelActivity = (ControlPanelActivity) activity;
                if (item != null) {
                    controlPanelActivity.switchFragmentToEditItem(item.id);
                }
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main, menu);
    }

    public class getItemsAsync extends AsyncTask<Void, Void, GetItemsReply> {
        SimpleRequest simpleRequest;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (isAdded()) {
                TextView emptyListTextView = (TextView) mEmptyListView;
                setTextInFragment(emptyListTextView, getStringInFragment(R.string.getting_items));
                displayProgressDialog(null);
            }
        }

        @Override
        protected GetItemsReply doInBackground(Void... voids) {
            RequestProperties rp = GRPCUtils.newRPWithDeviceInfo();
            simpleRequest = new SimpleRequest();
            simpleRequest.requestProperties = rp;

            GetItemsReply getItemsReply;
            if (BuildConfig.DEBUG) {
                logD("Request for getting items is: " + simpleRequest);
            }
            try {
                SellServiceGrpc.SellServiceBlockingStub stub = GRPCUtils.getInstance().getSellServiceGrpc();
                getItemsReply = stub.getItems(simpleRequest);
                return getItemsReply;
            } catch (Exception e) {
                logE("Error running getItems request. simpleRequest: " + simpleRequest + ". Error: " + e, e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(GetItemsReply getItemsReply) {
            super.onPostExecute(getItemsReply);

            Activity activity = getActivity();
            dismissProgressDialog();
            if (getItemsReply != null) {
                switch (getItemsReply.replyProperties.statusCode) {
                    case ReplyProperties.UPGRADE_REQUIRED:
                        if (activity != null) {
                            ControlPanelActivity controlPanelActivity = ((ControlPanelActivity) activity);
                            controlPanelActivity.displayUpgradeRequiredDialog();
                        } else {
                            displayLongToast(getStringInFragment(R.string.upgrade_to_new_version));
                        }
                        break;
                    case ReplyProperties.CLIENT_ERROR:
                        switch (getItemsReply.errorCode) {
                            case GetItemsReply.STORE_NOT_CREATED:
                                displayLongToast(getStringInFragment(R.string.store_not_created));
                                break;

                            case GetItemReply.NO_CLIENT_ERROR:
                                logE("Got NO_CLIENT_ERROR code for getItemsReply. simpleRequest: " + simpleRequest + ". User id: " + HonarnamaUser.getId());
                                displayShortToast(getStringInFragment(R.string.error_getting_info));
                                break;
                        }
                        break;

                    case ReplyProperties.SERVER_ERROR:
                        if (isAdded()) {
                            mAdapter.setItems(null);
                            TextView emptyListTextView = (TextView) mEmptyListView;
                            setTextInFragment(emptyListTextView, getStringInFragment(R.string.item_not_found));
                            mAdapter.notifyDataSetChanged();
                            displayRetrySnackbar();
                            displayLongToast(getStringInFragment(R.string.server_error_try_again));
                        }
                        break;

                    case ReplyProperties.NOT_AUTHORIZED:
                        HonarnamaUser.logout(activity);
                        displayLongToast(getStringInFragment(R.string.login_again));
                        break;

                    case ReplyProperties.OK:
                        if (isAdded()) {
                            net.honarnama.nano.Item itemList[] = getItemsReply.items;
                            mAdapter.setItems(itemList);
                            TextView emptyListTextView = (TextView) mEmptyListView;
                            setTextInFragment(emptyListTextView, getStringInFragment(R.string.has_not_registered_any_store));
                            mAdapter.notifyDataSetChanged();
                        }
                        break;
                }

            } else {
                if (isAdded()) {
                    mAdapter.setItems(null);
                    TextView emptyListTextView = (TextView) mEmptyListView;
                    setTextInFragment(emptyListTextView, getStringInFragment(R.string.item_not_found));
                    mAdapter.notifyDataSetChanged();
                    displayRetrySnackbar();
                }

            }
        }
    }

//    private void dismissProgressDialog() {
//        Activity activity = mActivity;
//        if (activity != null && !activity.isFinishing()) {
//            if (mProgressDialog != null && mProgressDialog.isShowing()) {
//                mProgressDialog.dismiss();
//            }
//        }
//    }
//
//    private void displayProgressDialog() {
//        if (mProgressDialog == null) {
//            mProgressDialog = new ProgressDialog(mActivity);
//            mProgressDialog.setCancelable(false);
//            mProgressDialog.setMessage(getStringInFragment(R.string.please_wait));
//        }
//        Activity activity = mActivity;
//        if (activity != null && !activity.isFinishing() && isVisible()) {
//            mProgressDialog.show();
//        }
//    }


    public void displayRetrySnackbar() {

        dismissSnackbar();

        Activity activity = getActivity();
        View sbView = null;
        TextView textView = null;

        SpannableStringBuilder builder = new SpannableStringBuilder();
        builder.append(" ").append(getStringInFragment(R.string.error_connecting_server_try_again)).append(" ");

        if (!isAdded()) {
            return;
        }

        mSnackbar = Snackbar.make(mCoordinatorLayout, builder, Snackbar.LENGTH_INDEFINITE);
        if (mSnackbar != null) {
            sbView = mSnackbar.getView();
        }

        if (sbView != null) {
            textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
            sbView.setBackgroundColor(getResources().getColor(R.color.amber));
        }

        if (textView != null) {
            textView.setBackgroundColor(getResources().getColor(R.color.amber));
            textView.setSingleLine(false);
            textView.setGravity(Gravity.CENTER);
            Spannable spannable = (Spannable) textView.getText();
            if (activity != null) {
                spannable.setSpan(new ImageSpan(activity, android.R.drawable.stat_notify_sync), textView.getText().length() - 1, textView.getText().length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            }
            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (NetworkManager.getInstance().isNetworkEnabled(true)) {
                        new getItemsAsync().execute();
                        if (mSnackbar != null && mSnackbar.isShown()) {
                            mSnackbar.dismiss();
                        }
                    }
                }
            });
        }
        if (isAdded() && mSnackbar != null) {
            mSnackbar.show();
        }
    }

    public void dismissSnackbar() {
        if (isAdded() && mSnackbar != null && mSnackbar.isShown()) {
            mSnackbar.dismiss();
        }
    }
}
