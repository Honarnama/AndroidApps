package net.honarnama.base.dialog;

import net.honarnama.base.R;
import net.honarnama.base.adapter.OptionListAdapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class OptionDialog extends BaseDialog {

    private final ListView mListView;

    public OptionDialog(final Activity activity, String[] items, int titleResId) {
        super(activity, titleResId);

        View dialogBody = LayoutInflater.from(activity).inflate(R.layout.dialog_option, null);
        mListView = (ListView) dialogBody.findViewById(android.R.id.list);
//        mListView.setSelector(R.drawable.);
        mListView.setAdapter(new OptionListAdapter(activity, items));

        setContent(dialogBody);
    }

    public OptionDialog(Activity activity, ArrayAdapter adapter, int titleResId) {

        super(activity, titleResId);
        View dialogBody = LayoutInflater.from(activity).inflate(R.layout.dialog_option, null);

        mListView = (ListView) dialogBody.findViewById(android.R.id.list);
//        mListView.setSelector(R.drawable.);
        mListView.setAdapter(adapter);

        setContent(dialogBody);
    }

    public void setOnItemClickListener(AdapterView.OnItemClickListener onItemCliclListener) {
        mListView.setOnItemClickListener(onItemCliclListener);
    }

}
