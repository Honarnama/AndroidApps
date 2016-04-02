package net.honarnama.sell.adapter;

import com.parse.GetDataCallback;
import com.parse.ImageSelector;
import com.parse.ParseException;

import net.honarnama.core.model.Item;
import net.honarnama.sell.R;
import net.honarnama.sell.activity.ControlPanelActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import bolts.Continuation;
import bolts.Task;

/**
 * Created by reza on 11/5/15.
 */
public class ItemsAdapter extends BaseAdapter {

    Context mContext;
    List<Item> mItems;
    private static LayoutInflater mInflater = null;

    public ItemsAdapter(Context context) {
        mContext = context;
        mItems = new ArrayList<Item>();
        mInflater = LayoutInflater.from(mContext);
    }

    @Override
    public int getCount() {
        return mItems.size();
    }

    @Override
    public Object getItem(int position) {
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

        final Item item = mItems.get(position);
        // Setting all values in listview
        mViewHolder.title.setText(item.getName());

        if (item.getStatus() == Item.STATUS_CODE_CONFIRMATION_WAITING) {
            mViewHolder.waitingToBeConfirmedTextView.setVisibility(View.VISIBLE);
        }

        if (item.getStatus() == Item.STATUS_CODE_NOT_VERIFIED) {
            mViewHolder.itemRowContainer.setBackgroundResource(R.drawable.red_borderd_background);
            mViewHolder.waitingToBeConfirmedTextView.setVisibility(View.VISIBLE);
            mViewHolder.waitingToBeConfirmedTextView.setText("این آگهی تایید نشد");

        }

        mViewHolder.itemIcomLoadingPanel.setVisibility(View.VISIBLE);
        mViewHolder.icon.setVisibility(View.GONE);
//        mViewHolder.icon.loadInBackground(item.getImage1(), new GetDataCallback() {
//            @Override
//            public void done(byte[] data, ParseException e) {
//                mViewHolder.itemIcomLoadingPanel.setVisibility(View.GONE);
//                mViewHolder.icon.setVisibility(View.VISIBLE);
//            }
//        });

        mViewHolder.deleteContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new AlertDialog.Builder(new ContextThemeWrapper(mContext, R.style.DialogStyle))
                        .setTitle("تایید حذف")
                        .setMessage("آگهی " + item.getName() + " را حذف میکنید؟")
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton("بله خذف میکنم.", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                final ProgressDialog progressDialog = new ProgressDialog(mContext);
                                progressDialog.setCancelable(false);
                                progressDialog.setMessage(mContext.getString(R.string.please_wait));
                                progressDialog.show();


                                Item.deleteItem(mContext, item.getId()).continueWith(new Continuation<Void, Object>() {
                                    @Override
                                    public Object then(Task<Void> task) throws Exception {
                                        progressDialog.dismiss();
                                        if (task.isFaulted()) {
                                            if (mContext != null) {
                                                Toast.makeText(mContext, "حذف محصول با خطا مواجه شد." + mContext.getString(R.string.please_check_internet_connection), Toast.LENGTH_SHORT).show();
                                            }
                                        } else {
                                            mItems.remove(position);
                                            notifyDataSetChanged();
                                        }
                                        return null;
                                    }
                                });

                            }
                        })
                        .setNegativeButton("نه اشتباه شد.", null).show();

            }
        });

        mViewHolder.editContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Item item = (Item) getItem(position);
                ControlPanelActivity controlPanelActivity = (ControlPanelActivity) mContext;
                controlPanelActivity.switchFragmentToEditItem(item.getId());
            }
        });
        return convertView;

    }

    public void setItems(List<Item> itemList) {
        mItems = itemList;
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
}
