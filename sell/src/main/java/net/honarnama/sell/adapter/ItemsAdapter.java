package net.honarnama.sell.adapter;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;
import com.bumptech.glide.signature.StringSignature;
import com.crashlytics.android.Crashlytics;
import com.mikepenz.iconics.view.IconicsImageView;
import com.parse.ImageSelector;

import net.honarnama.GRPCUtils;
import net.honarnama.HonarnamaBaseApp;
import net.honarnama.base.BuildConfig;
import net.honarnama.base.utils.NetworkManager;
import net.honarnama.nano.DeleteItemReply;
import net.honarnama.nano.GetOrDeleteItemRequest;
import net.honarnama.nano.HonarnamaProto;
import net.honarnama.nano.ReplyProperties;
import net.honarnama.nano.RequestProperties;
import net.honarnama.nano.SellServiceGrpc;
import net.honarnama.sell.R;
import net.honarnama.sell.activity.ControlPanelActivity;
import net.honarnama.sell.fragments.ItemsFragment;
import net.honarnama.sell.model.HonarnamaUser;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;

import io.fabric.sdk.android.services.concurrency.AsyncTask;

/**
 * Created by reza on 11/5/15.
 */
public class ItemsAdapter extends BaseAdapter {

    Context mContext;
    ArrayList<net.honarnama.nano.Item> mItems;
    private static LayoutInflater mInflater = null;
    ProgressDialog mProgressDialog;
    public final static String DEBUG_TAG = HonarnamaBaseApp.PRODUCTION_TAG + "/itemAdapter";
    ItemsFragment mItemsFragment;

    public ItemsAdapter(Context context) {
        mContext = context;
        mItems = new ArrayList();
        mInflater = LayoutInflater.from(mContext);
        mItemsFragment = ItemsFragment.getInstance();
    }

    @Override
    public int getCount() {
        return mItems.size();
    }

    @Override
    public net.honarnama.nano.Item getItem(int position) {
        return mItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final MyViewHolder mViewHolder;

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.item_row, parent, false);
            mViewHolder = new MyViewHolder(convertView);
            convertView.setTag(mViewHolder);
        } else {
            mViewHolder = (MyViewHolder) convertView.getTag();
        }

        final net.honarnama.nano.Item item = mItems.get(position);
        // Setting all values in listview
        mViewHolder.title.setText(item.name);

        if (item.reviewStatus == HonarnamaProto.NOT_REVIEWED) {
            mViewHolder.waitingToBeConfirmedTextView.setVisibility(View.VISIBLE);
            mViewHolder.rejectReasonIcon.setVisibility(View.GONE);
            mViewHolder.waitingToBeConfirmedTextView.setText(mContext.getString(R.string.waiting_to_be_confirmed));
        } else if (item.reviewStatus == HonarnamaProto.CHANGES_NEEDED) {
            mViewHolder.itemRowContainer.setBackgroundResource(R.drawable.red_borderd_background);
            mViewHolder.waitingToBeConfirmedTextView.setVisibility(View.VISIBLE);
            mViewHolder.rejectReasonIcon.setVisibility(View.GONE);
            mViewHolder.waitingToBeConfirmedTextView.setText(mContext.getString(R.string.changes_needed));
        } else {
            mViewHolder.waitingToBeConfirmedTextView.setVisibility(View.GONE);
            mViewHolder.rejectReasonIcon.setVisibility(View.GONE);
        }

        String itemImage = "";
        if (item.images != null) {
            for (int i = 0; i < item.images.length; i++) {
                if (!TextUtils.isEmpty(item.images[i])) {
                    itemImage = item.images[i];
                    break;
                }
            }
        }

        if (!TextUtils.isEmpty(itemImage)) {
            mViewHolder.itemIcomLoadingPanel.setVisibility(View.VISIBLE);
            mViewHolder.icon.setVisibility(View.GONE);
            Glide.with(mContext).load(itemImage)
                    .error(R.drawable.camera_insta)
                    .skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .signature(new StringSignature(String.valueOf(System.currentTimeMillis())))
                    .into(new GlideDrawableImageViewTarget(mViewHolder.icon) {
                        @Override
                        public void onResourceReady(GlideDrawable resource, GlideAnimation<? super GlideDrawable> animation) {
                            super.onResourceReady(resource, animation);
                            mViewHolder.itemIcomLoadingPanel.setVisibility(View.GONE);
                            mViewHolder.icon.setVisibility(View.VISIBLE);
                        }

                        @Override
                        public void onLoadFailed(Exception e, Drawable errorDrawable) {
                            super.onLoadFailed(e, errorDrawable);
                            mViewHolder.itemIcomLoadingPanel.setVisibility(View.GONE);
                            mViewHolder.icon.setVisibility(View.VISIBLE);
                        }
                    });

        }
        mViewHolder.deleteContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (NetworkManager.getInstance().isNetworkEnabled(true)) {
                    new AlertDialog.Builder(new ContextThemeWrapper(mContext, R.style.DialogStyle))
                            .setTitle("تایید حذف")
                            .setMessage("آگهی " + item.name + " را حذف میکنید؟")
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setPositiveButton("بله خذف میکنم.", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    new deleteItemAsync().execute(position);
                                }
                            })
                            .setNegativeButton("نه اشتباه شد.", null).show();
                }

            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            mViewHolder.editContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (NetworkManager.getInstance().isNetworkEnabled(true)) {
                        ControlPanelActivity controlPanelActivity = (ControlPanelActivity) mContext;
                        controlPanelActivity.switchFragmentToEditItem(mItems.get(position).id);
                    }
                }
            });
        } else {
            mViewHolder.editContainer.setVisibility(View.GONE);
        }
        return convertView;

    }

    public void setItems(net.honarnama.nano.Item[] itemsArray) {
        if (itemsArray != null) {
            for (int i = 0; i < itemsArray.length; i++) {
                mItems.add(itemsArray[i]);
            }
        }
    }

    private class MyViewHolder {
        TextView title;
        ImageSelector icon;
        RelativeLayout deleteContainer;
        RelativeLayout editContainer;
        TextView waitingToBeConfirmedTextView;
        RelativeLayout itemRowContainer;
        RelativeLayout itemIcomLoadingPanel;
        IconicsImageView rejectReasonIcon;

        public MyViewHolder(View view) {
            title = (TextView) view.findViewById(R.id.item_title_in_list);
            icon = (ImageSelector) view.findViewById(R.id.item_image_in_list);
            deleteContainer = (RelativeLayout) view.findViewById(R.id.item_delete_container);
            editContainer = (RelativeLayout) view.findViewById(R.id.item_edit_container);
            waitingToBeConfirmedTextView = (TextView) view.findViewById(R.id.review_status_text_view);
            itemRowContainer = (RelativeLayout) view.findViewById(R.id.item_row_container);
            itemIcomLoadingPanel = (RelativeLayout) view.findViewById(R.id.item_icon_loading_panel);
            rejectReasonIcon = (IconicsImageView) view.findViewById(R.id.reject_reason_icon);
        }
    }


    public class deleteItemAsync extends AsyncTask<Integer, Void, DeleteItemReply> {
        int itemPosition;
        GetOrDeleteItemRequest getOrDeleteItemRequest;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            displayProgressDialog();
        }

        @Override
        protected DeleteItemReply doInBackground(Integer... position) {
            RequestProperties rp = GRPCUtils.newRPWithDeviceInfo();

            getOrDeleteItemRequest = new GetOrDeleteItemRequest();
            getOrDeleteItemRequest.requestProperties = rp;
            itemPosition = position[0];
            getOrDeleteItemRequest.id = mItems.get(itemPosition).id;
            DeleteItemReply deleteItemReply;
            if (BuildConfig.DEBUG) {
                Log.d(DEBUG_TAG, "getOrDeleteItemRequest is: " + getOrDeleteItemRequest);
            }
            try {
                SellServiceGrpc.SellServiceBlockingStub stub = GRPCUtils.getInstance().getSellServiceGrpc();
                deleteItemReply = stub.deleteItem(getOrDeleteItemRequest);
                return deleteItemReply;
            } catch (Exception e) {
                if (BuildConfig.DEBUG) {
                    Log.e(DEBUG_TAG, "Error running deleteItemAsync. request: " + getOrDeleteItemRequest + ". Error: " + e, e);
                } else {
                    StringWriter sw = new StringWriter();
                    e.printStackTrace(new PrintWriter(sw));
                    String stackTrace = sw.toString();
                    Crashlytics.log(Log.ERROR, DEBUG_TAG, "Error running deleteItemAsync. request: " + getOrDeleteItemRequest + ". Error: " + e + ". stackTrace: " + stackTrace);
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(DeleteItemReply deleteItemReply) {
            super.onPostExecute(deleteItemReply);
            dismissProgressDialog();
            if (deleteItemReply != null) {
                switch (deleteItemReply.replyProperties.statusCode) {
                    case ReplyProperties.UPGRADE_REQUIRED:
                        ControlPanelActivity controlPanelActivity = ((ControlPanelActivity) mItemsFragment.getActivity());
                        if (controlPanelActivity != null) {
                            controlPanelActivity.displayUpgradeRequiredDialog();
                        }
                        break;
                    case ReplyProperties.CLIENT_ERROR:
                        switch (deleteItemReply.errorCode) {
                            case DeleteItemReply.ITEM_NOT_FOUND:
                                mItemsFragment.displayLongToast(mContext.getString(R.string.item_not_found));
                                break;
                            case DeleteItemReply.FORBIDDEN:
                                mItemsFragment.displayLongToast(mContext.getString(R.string.not_allowed_to_do_this_action));
                                mItemsFragment.logE("Got FORBIDDEN reply while running deleteItem request.  getOrDeleteItemRequest: " + getOrDeleteItemRequest + ". User Id: " + HonarnamaUser.getId() + ".");
                                break;
                            case DeleteItemReply.NO_CLIENT_ERROR:
                                mItemsFragment.logE("Got NO_CLIENT_ERROR code for updating item. getOrDeleteItemRequest: " + getOrDeleteItemRequest + ". User Id: " + HonarnamaUser.getId() + ".");
                                mItemsFragment.displayShortToast(mContext.getString(R.string.error_getting_info));
                                break;
                        }
                        break;

                    case ReplyProperties.SERVER_ERROR:
                        dismissProgressDialog();
                        mItemsFragment.displayShortToast(mContext.getString(R.string.server_error_try_again));
                        if (BuildConfig.DEBUG) {
                            Log.e(DEBUG_TAG, "Server error occured trying to delete item. request: " + getOrDeleteItemRequest);
                        } else {
                            Crashlytics.logException(new Throwable("Server error occured trying to delete item. request: " + getOrDeleteItemRequest));
                        }
                        break;

                    case ReplyProperties.NOT_AUTHORIZED:
                        HonarnamaUser.logout(mItemsFragment.getActivity());
                        break;

                    case ReplyProperties.OK:
                        mItems.remove(itemPosition);
                        notifyDataSetChanged();
                        mItemsFragment.displayShortToast(mContext.getString(R.string.item_deleted));
                        break;
                }

            } else {
                mItemsFragment.displayLongToast(mContext.getString(R.string.error_connecting_server_try_again));
                mItemsFragment.displayLongToast("لطفا فعال بودن و به‌روز بودن خدمات Google Play را بررسی کنید.");
//                Activity activity = (Activity) mContext;
//                if (activity != null) {
//                    int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(activity);
//                    if (status != ConnectionResult.SUCCESS) {
//                        Log.e(DEBUG_TAG, "GooglePlayServices is not available. ConnectionResult: " + status);
//                        ((Dialog) GooglePlayServicesUtil.getErrorDialog(status, activity, 10)).show();
//                    }
//                }
            }
        }
    }

    private void dismissProgressDialog() {
        Activity activity = mItemsFragment.getActivity();
        if (activity != null && !activity.isFinishing()) {
            if (mProgressDialog != null && mProgressDialog.isShowing()) {
                mProgressDialog.dismiss();
            }
        }
    }

    private void displayProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(mItemsFragment.getActivity());
            mProgressDialog.setCancelable(false);
            mProgressDialog.setMessage(mItemsFragment.getString(R.string.please_wait));
        }
        Activity activity = mItemsFragment.getActivity();
        if (activity != null && !activity.isFinishing() && mItemsFragment.isVisible()) {
            mProgressDialog.show();
        }
    }
}
