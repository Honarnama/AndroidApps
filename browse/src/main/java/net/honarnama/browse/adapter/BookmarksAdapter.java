package net.honarnama.browse.adapter;

import com.parse.ImageSelector;

import net.honarnama.browse.R;
import net.honarnama.browse.model.Bookmark;
import net.honarnama.browse.model.Item;
import net.honarnama.base.model.ArtCategory;
import net.honarnama.base.utils.TextUtil;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by elnaz on 2/23/16.
 */
public class BookmarksAdapter extends BaseAdapter {
    //TODO  convert parseQueryAdapter to baseAdapter (What would happen to long lists)
    List<Bookmark> mBookmarks = new ArrayList<>();
    Context mContext;

    public BookmarksAdapter(Context context) {
        mContext = context;
    }

    public void setItems(List<Bookmark> items) {
        mBookmarks = items;
    }

    @Override
    public int getCount() {
        return 0;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Bookmark bookmark = mBookmarks.get(position);
        Item item = (Item) bookmark.getItem();

        final MyViewHolder mViewHolder;

        if (convertView == null) {
            convertView = View.inflate(mContext, R.layout.item_row, null);
            mViewHolder = new MyViewHolder(convertView);
            convertView.setTag(mViewHolder);
        } else {
            mViewHolder = (MyViewHolder) convertView.getTag();
        }

        mViewHolder.itemIconLoadingPanel.setVisibility(View.VISIBLE);
        mViewHolder.icon.setVisibility(View.GONE);

        //TODO
//        mViewHolder.icon.loadInBackground(item.getParseFile(Item.IMAGE_1), new GetDataCallback() {
//            @Override
//            public void done(byte[] data, ParseException e) {
//                mViewHolder.itemIconLoadingPanel.setVisibility(View.GONE);
//                mViewHolder.icon.setVisibility(View.VISIBLE);
//            }
//        });

        // Setting all values in listview
        mViewHolder.title.setText(TextUtil.convertEnNumberToFa(item.getName()));
        mViewHolder.desc.setText(TextUtil.convertEnNumberToFa(item.getDescription()));

        ArtCategory category = item.getCategory();
        mViewHolder.itemCat.setText(category.getName());

        return convertView;

    }

    private class MyViewHolder {
        TextView title;
        TextView desc;
        TextView itemCat;
        ImageSelector icon;
        RelativeLayout itemRowContainer;
        RelativeLayout itemIconLoadingPanel;


        public MyViewHolder(View view) {
            title = (TextView) view.findViewById(R.id.item_row_title);
            desc = (TextView) view.findViewById(R.id.item_row_desc);
            icon = (ImageSelector) view.findViewById(R.id.item_image_in_list);
            itemRowContainer = (RelativeLayout) view.findViewById(R.id.item_row_outer_container);
            itemIconLoadingPanel = (RelativeLayout) view.findViewById(R.id.item_icon_loading_panel);
            itemCat = (TextView) view.findViewById(R.id.item_row_cat);
        }
    }


}
