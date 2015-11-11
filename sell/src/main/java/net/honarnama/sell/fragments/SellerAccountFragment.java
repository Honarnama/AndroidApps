package net.honarnama.sell.fragments;


import com.parse.ImageSelector;

import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

import net.honarnama.sell.R;
import net.honarnama.utils.GenericGravityTextWatcher;

/**
 * A simple {@link Fragment} subclass.
 */
public class SellerAccountFragment extends Fragment {

    public static SellerAccountFragment mSellerAccountFragment;

    private TextView mAddNationalCardTextView;
    private ImageSelector mNationalCardImageView;

    private EditText mLastnameEditText;
    private EditText mFirstnameEditText;
    private EditText mMobileNumberEditText;
    private EditText mEmailAddressEditText;
    private EditText mCurrentPasswordEdiText;
    private EditText mNewPasswordEditText;
    private EditText mBankCardNumberEditText;

    private RadioButton mActivateWithEmail;
    private RadioButton mActivateWithMobileNumber;

    private Button mRegisterButton;


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
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_seller_account, container, false);

        mAddNationalCardTextView = (TextView) rootView.findViewById(R.id.seller_account_national_card_title_text_view);

        mNationalCardImageView = (ImageSelector) rootView.findViewById(R.id.seller_account_national_card_image_view);
        mNationalCardImageView.restore(savedInstanceState);

        mNationalCardImageView.setOnImageSelectedListener(new ImageSelector.OnImageSelectedListener() {
            @Override
            public boolean onImageSelected(Uri selectedImage, boolean cropped) {
                return true;
            }

            @Override
            public boolean onImageRemoved() {
                return false;
            }

            @Override
            public void onImageSelectionFailed() {
            }
        });

        mFirstnameEditText = (EditText) rootView.findViewById(R.id.seller_account_firstname_edit_text);
        mLastnameEditText = (EditText) rootView.findViewById(R.id.seller_account_lastname_edit_text);
        mCurrentPasswordEdiText = (EditText) rootView.findViewById(R.id.seller_account_current_password_edit_text);
        mNewPasswordEditText = (EditText) rootView.findViewById(R.id.seller_account_new_password_edit_text);
        mBankCardNumberEditText = (EditText) rootView.findViewById(R.id.seller_account_bank_card_number_edit_text);

        mNewPasswordEditText.addTextChangedListener(new GenericGravityTextWatcher(mNewPasswordEditText));
        mBankCardNumberEditText.addTextChangedListener(new GenericGravityTextWatcher(mBankCardNumberEditText));


        return rootView;
    }


}
