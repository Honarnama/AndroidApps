package net.honarnama.core.fragment;

import net.honarnama.HonarnamaBaseApp;
import net.honarnama.base.R;
import net.honarnama.core.utils.GenericGravityTextWatcher;
import net.honarnama.core.utils.NetworkManager;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

public class ContactFragment extends HonarnamaBaseFragment {

    public static ContactFragment mContactFragment;

    private TextView mContactText;
    private EditText mSubject;
    private EditText mBody;
    private EditText mPhone;
    private EditText mEmail;
    private Button mContactButton;
    private CheckBox mSendDeviceInfo;

    public synchronized static ContactFragment getInstance(String appKey) {
//        if (mContactFragment == null) {
        mContactFragment = new ContactFragment();
        Bundle args = new Bundle();
        args.putString("appKey", appKey);
        mContactFragment.setArguments(args);
//        }
        return mContactFragment;
    }


    @Override
    public String getTitle(Context context) {
        return context.getString(R.string.contact_us);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_contact, container, false);
        mContactText = (TextView) rootView.findViewById(R.id.contact_text);
        mSubject = (EditText) rootView.findViewById(R.id.contact_subject);
        mBody = (EditText) rootView.findViewById(R.id.contact_body);
        mPhone = (EditText) rootView.findViewById(R.id.contact_phone);
        mEmail = (EditText) rootView.findViewById(R.id.contact_email);
        mContactButton = (Button) rootView.findViewById(R.id.contact_butoon);
        mSendDeviceInfo = (CheckBox) rootView.findViewById(R.id.send_device_info_check_box);

        mPhone.addTextChangedListener(new GenericGravityTextWatcher(mPhone));
        mEmail.addTextChangedListener(new GenericGravityTextWatcher(mEmail));
        mContactText.setMovementMethod(LinkMovementMethod.getInstance());

        mSubject.setError(null);
        mBody.setError(null);

        if (getArguments().getString("appKey") == HonarnamaBaseApp.PREF_NAME_BROWSE_APP) {
            mContactButton.setBackgroundColor(getResources().getColor(R.color.dark_cyan));
        }
        mContactButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String subject = mSubject.getText().toString().trim();
                String body = mBody.getText().toString().trim();
                String phone = mPhone.getText().toString().trim();
                String email = mEmail.getText().toString().trim();

                if (!NetworkManager.getInstance().isNetworkEnabled(true)) {
                    return;
                }
                if (subject.length() == 0) {
                    mSubject.setError("موضوع پیام نمی‌تواند خالی باشد.");
                    return;
                }
                if (body.length() == 0) {
                    mBody.setError("متن پیام نمی‌تواند خالی باشد.");
                    return;
                }

//                Intent i = new Intent(Intent.ACTION_SEND);
//                i.setType("message/rfc822");
//                i.putExtra(Intent.EXTRA_EMAIL, new String[]{"support@honarnama.net"});
//                i.putExtra(Intent.EXTRA_SUBJECT, subject);
//                i.putExtra(Intent.EXTRA_TEXT, body);
//                try {
//                    startActivity(Intent.createChooser(i, "Send mail..."));
//                } catch (android.content.ActivityNotFoundException ex) {
//                    Toast.makeText(getActivity(), "There are no email clients installed.", Toast.LENGTH_SHORT).show();
//                }

                mContactButton.setText(getString(R.string.sending));
                mContactButton.setEnabled(false);
                mContactButton.setClickable(false);
                mSubject.setEnabled(false);
                mBody.setEnabled(false);
                mPhone.setEnabled(false);
                mEmail.setEnabled(false);

                Map<String, String> params = new HashMap<>();

                if (mSendDeviceInfo.isChecked()) {
                    String s = "\n Debug-infos:";
                    s += "\n OS Version: " + System.getProperty("os.version") + "(" + android.os.Build.VERSION.INCREMENTAL + ")";
                    s += "\n OS API Level: " + android.os.Build.VERSION.SDK_INT;
                    s += "\n Device: " + android.os.Build.DEVICE;
                    s += "\n Model (and Product): " + android.os.Build.MODEL + " (" + android.os.Build.PRODUCT + ")";
                    s += "\n RELEASE: " + android.os.Build.VERSION.RELEASE;
                    s += "\n BRAND: " + android.os.Build.BRAND;
                    s += "\n DISPLAY: " + android.os.Build.DISPLAY;
                    s += "\n CPU_ABI: " + android.os.Build.CPU_ABI;
                    s += "\n CPU_ABI2: " + android.os.Build.CPU_ABI2;
                    s += "\n UNKNOWN: " + android.os.Build.UNKNOWN;
                    s += "\n HARDWARE: " + android.os.Build.HARDWARE;
                    s += "\n Build ID: " + android.os.Build.ID;
                    s += "\n MANUFACTURER: " + android.os.Build.MANUFACTURER;
                    s += "\n SERIAL: " + android.os.Build.SERIAL;
                    s += "\n USER: " + android.os.Build.USER;
                    s += "\n HOST: " + android.os.Build.HOST;
                    body = body + s;
                }
                params.put("text", body + "\n \n Phone: " + phone);
                params.put("subject", subject);
                params.put("fromEmail", email);

//                ParseCloud.callFunctionInBackground("sendMail", params, new FunctionCallback<Object>() {
//                    @Override
//                    public void done(Object response, ParseException parseException) {
//
//                        mContactButton.setText(getString(R.string.send));
//                        mContactButton.setEnabled(true);
//                        mContactButton.setClickable(true);
//
//                        mSubject.setEnabled(true);
//                        mBody.setEnabled(true);
//                        mPhone.setEnabled(true);
//                        mEmail.setEnabled(true);
//
//                        if (parseException != null) {
//                            logE("Sending Mail Failed. " + "Response: " + response, "", parseException);
//                            if (isVisible()) {
//                                Toast.makeText(getActivity(), getString(R.string.error_occured) + getString(R.string.please_check_internet_connection), Toast.LENGTH_LONG).show();
//                            }
//                        } else {
//                            mSubject.setText("");
//                            mBody.setText("");
//                            if (isVisible()) {
//                                Toast.makeText(getActivity(), getString(R.string.contact_sent_successfully), Toast.LENGTH_LONG).show();
//                            }
//                        }
//                    }
//                });


            }
        });
        return rootView;
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }


}
