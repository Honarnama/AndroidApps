package net.honarnama.sell.adapter;

import com.parse.ImageSelector;
import com.squareup.picasso.Callback;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import net.honarnama.GRPCUtils;
import net.honarnama.core.model.Item;
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

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

import io.fabric.sdk.android.services.concurrency.AsyncTask;

/**
 * Created by reza on 11/5/15.
 */
public class ItemsAdapter extends BaseAdapter {

    Context mContext;
    ArrayList<net.honarnama.nano.Item> mItems;
    private static LayoutInflater mInflater = null;

    public ItemsAdapter(Context context) {
        mContext = context;
        mItems = new ArrayList();
        mInflater = LayoutInflater.from(mContext);
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
        }

        if (item.reviewStatus == HonarnamaProto.CHANGES_NEEDED) {
            mViewHolder.itemRowContainer.setBackgroundResource(R.drawable.red_borderd_background);
            mViewHolder.waitingToBeConfirmedTextView.setVisibility(View.VISIBLE);
            mViewHolder.waitingToBeConfirmedTextView.setText("این آگهی تایید نشد");
        }

        String itemImage = "";
        for (int i = 0; i < 4; i++) {
            if (!TextUtils.isEmpty(item.images[i])) {
                itemImage = item.images[i];
                break;
            }
        }
        if (!TextUtils.isEmpty(itemImage)) {
            mViewHolder.itemIcomLoadingPanel.setVisibility(View.VISIBLE);
            mViewHolder.icon.setVisibility(View.GONE);
            Picasso.with(mContext).load(itemImage)
                    .error(R.drawable.camera_insta)
                    .memoryPolicy(MemoryPolicy.NO_CACHE)
                    .networkPolicy(NetworkPolicy.NO_CACHE)
                    .into(mViewHolder.icon, new Callback() {
                        @Override
                        public void onSuccess() {
                            mViewHolder.itemIcomLoadingPanel.setVisibility(View.GONE);
                            mViewHolder.icon.setVisibility(View.VISIBLE);
                        }

                        @Override
                        public void onError() {
                            mViewHolder.itemIcomLoadingPanel.setVisibility(View.GONE);
                            mViewHolder.icon.setVisibility(View.VISIBLE);
                        }
                    });
        }
        mViewHolder.deleteContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

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
        });

        mViewHolder.editContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ControlPanelActivity controlPanelActivity = (ControlPanelActivity) mContext;
                controlPanelActivity.switchFragmentToEditItem(mItems.get(position).id);
            }
        });
        return convertView;

    }

    public void setItems(net.honarnama.nano.Item[] itemsArray) {
        for (int i = 0; i < itemsArray.length; i++) {
            mItems.add(itemsArray[i]);
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

        public MyViewHolder(View view) {
            title = (TextView) view.findViewById(R.id.item_title_in_list);
            icon = (ImageSelector) view.findViewById(R.id.item_image_in_list);
            deleteContainer = (RelativeLayout) view.findViewById(R.id.item_delete_container);
            editContainer = (RelativeLayout) view.findViewById(R.id.item_edit_container);
            waitingToBeConfirmedTextView = (TextView) view.findViewById(R.id.waiting_to_be_confirmed_text_view);
            itemRowContainer = (RelativeLayout) view.findViewById(R.id.item_row_container);
            itemIcomLoadingPanel = (RelativeLayout) view.findViewById(R.id.item_icon_loading_panel);
        }
    }


    public class deleteItemAsync extends AsyncTask<Integer, Void, DeleteItemReply> {
        ProgressDialog progressDialog;
        ItemsFragment itemsFragment = ItemsFragment.getInstance();
        int itemPosition;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(mContext);
            progressDialog.setCancelable(false);
            progressDialog.setMessage(mContext.getString(R.string.please_wait));
            if (ItemsFragment.getInstance().getActivity() != null && ItemsFragment.getInstance().isVisible()) {
                progressDialog.show();
            }
        }

        @Override
        protected DeleteItemReply doInBackground(Integer... position) {
            RequestProperties rp = GRPCUtils.newRPWithDeviceInfo();

            GetOrDeleteItemRequest getOrDeleteItemRequest = new GetOrDeleteItemRequest();
            getOrDeleteItemRequest.requestProperties = rp;
            itemPosition = position[0];
            getOrDeleteItemRequest.id = mItems.get(itemPosition).id;
            DeleteItemReply deleteItemReply;

            try {
                SellServiceGrpc.SellServiceBlockingStub stub = GRPCUtils.getInstance().getSellServiceGrpc();
                deleteItemReply = stub.deleteItem(getOrDeleteItemRequest);
                return deleteItemReply;
            } catch (InterruptedException e) {
                //TODO log
            }
            return null;
        }

        @Override
        protected void onPostExecute(DeleteItemReply deleteItemReply) {
            super.onPostExecute(deleteItemReply);
            Log.e("inja", "deleteItemReply is " + deleteItemReply);
            //TODO use this checking in other async too (In others it is reversed!!!!
            if (itemsFragment.getActivity() != null) {
                if (!itemsFragment.getActivity().isFinishing() && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
            }
            if (deleteItemReply != null) {
                switch (deleteItemReply.replyProperties.statusCode) {
                    case ReplyProperties.UPGRADE_REQUIRED:
                        ControlPanelActivity controlPanelActivity = ((ControlPanelActivity) itemsFragment.getActivity());
                        if (controlPanelActivity != null) {
                            controlPanelActivity.displayUpgradeRequiredDialog();
                        }
                        break;
                    case ReplyProperties.CLIENT_ERROR:
                        switch (deleteItemReply.errorCode) {
                            case DeleteItemReply.ITEM_NOT_FOUND:
                                //TODO does this error arise at all?
                                break;
                            case DeleteItemReply.FORBIDDEN:
                                //TODO
                                break;
                            case DeleteItemReply.NO_CLIENT_ERROR:
                                //TODO bug report
                                break;
                        }
                        break;

                    case ReplyProperties.SERVER_ERROR:
                        if (itemsFragment.isVisible()) {
                            //TODO
                        }
                        break;

                    case ReplyProperties.NOT_AUTHORIZED:
                        //TODO toast
                        HonarnamaUser.logout(itemsFragment.getActivity());
                        break;

                    case ReplyProperties.OK:
                        mItems.remove(itemPosition);
                        notifyDataSetChanged();
                        //TODO toast
                        break;
                }

            } else {
                //TODO toast
            }
        }
    }
}
