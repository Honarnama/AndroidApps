package net.honarnama.sell.fragments;


import com.parse.ImageSelector;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import net.honarnama.HonarNamaBaseApp;
import net.honarnama.sell.R;
import net.honarnama.utils.GenericGravityTextWatcher;
import net.honarnama.utils.NetworkManager;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

/**
 * A simple {@link Fragment} subclass.
 */
public class SellerAccountFragment extends Fragment implements View.OnClickListener {

    public static SellerAccountFragment mSellerAccountFragment;

    private ImageSelector mNationalCardImageView;
    private EditText mBankCardNumberEditText;
    private EditText mFirstnameEditText;
    private EditText mLastnameEditText;
    private Button mAlterNameButton;
    private ParseUser mParseUser;

    public synchronized static SellerAccountFragment getInstance() {
        if (mSellerAccountFragment == null) {
            mSellerAccountFragment = new SellerAccountFragment();
        }
        return mSellerAccountFragment;
    }

    public SellerAccountFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mParseUser = ParseUser.getCurrentUser();
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_seller_account, container, false);

        mFirstnameEditText = (EditText) rootView.findViewById(R.id.seller_account_firstname_edit_text);
        mLastnameEditText = (EditText) rootView.findViewById(R.id.seller_account_lastname_edit_text);

        mFirstnameEditText.setText(mParseUser.get("firstname").toString());
        mLastnameEditText.setText(mParseUser.get("lastname").toString());

        mAlterNameButton = (Button) rootView.findViewById(R.id.seller_account_alter_name_button);

        mAlterNameButton.setOnClickListener(this);
//        mNewPasswordEditText.addTextChangedListener(new GenericGravityTextWatcher(mNewPasswordEditText));
//        mBankCardNumberEditText.addTextChangedListener(new GenericGravityTextWatcher(mBankCardNumberEditText));


        return rootView;
    }


    @Override
    public void onClick(View view) {
        if (view.getId() == mAlterNameButton.getId()) {
            if (!NetworkManager.getInstance().isNetworkEnabled(getActivity(), true)) {
                return;
            }

            final ProgressDialog sendingDataProgressDialog = new ProgressDialog(getActivity());
            sendingDataProgressDialog.setCancelable(false);
            sendingDataProgressDialog.setMessage(getString(R.string.sending_data));
            sendingDataProgressDialog.show();


            mParseUser.put("firstname", mFirstnameEditText.getText().toString().trim());
            mParseUser.put("lastname", mLastnameEditText.getText().toString().trim());

            mParseUser.pinInBackground();
            mParseUser.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e == null) {
                        Toast.makeText(getActivity(), getActivity().getString(R.string.successfully_changed_user_name), Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getActivity(), getActivity().getString(R.string.changing_user_name_failed), Toast.LENGTH_LONG).show();
                        Log.e(HonarNamaBaseApp.PRODUCTION_TAG, e.getMessage());
                    }
                    sendingDataProgressDialog.dismiss();
                }
            });


        }
    }
}
