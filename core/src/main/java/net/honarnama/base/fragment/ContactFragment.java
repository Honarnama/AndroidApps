package net.honarnama.base.fragment;

import net.honarnama.GRPCUtils;
import net.honarnama.HonarnamaBaseApp;
import net.honarnama.base.BuildConfig;
import net.honarnama.base.R;
import net.honarnama.base.utils.GravityTextWatcher;
import net.honarnama.base.utils.NetworkManager;
import net.honarnama.nano.CommunicationServiceGrpc;
import net.honarnama.nano.CreateMessageReply;
import net.honarnama.nano.CreateMessageRequest;
import net.honarnama.nano.ReplyProperties;
import net.honarnama.nano.RequestProperties;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;


public class ContactFragment extends HonarnamaBaseFragment {

    public static ContactFragment mContactFragment;

    private TextView mContactText;
    private EditText mSubject;
    private EditText mBody;
    private EditText mPhone;
    private EditText mEmail;
    private Button mContactButton;
    private CheckBox mSendDeviceInfo;

    public synchronized static ContactFragment getInstance() {
        mContactFragment = new ContactFragment();
//        Bundle args = new Bundle();
//        mContactFragment.setArguments(args);
        return mContactFragment;
    }

    @Override
    public String getTitle(Context context) {
        return getStringInFragment(R.string.contact_us);
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

        mPhone.addTextChangedListener(new GravityTextWatcher(mPhone));
        mEmail.addTextChangedListener(new GravityTextWatcher(mEmail));
        mContactText.setMovementMethod(LinkMovementMethod.getInstance());

        mSubject.setError(null);
        mBody.setError(null);

        if (HonarnamaBaseApp.PACKAGE_NAME.equals(HonarnamaBaseApp.BROWSE_PACKAGE_NAME)) {
            mContactButton.setBackgroundColor(getResources().getColor(R.color.dark_cyan));
        }
        mContactButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String subject = getTextInFragment(mSubject);
                String body = getTextInFragment(mBody);

                if (!NetworkManager.getInstance().isNetworkEnabled(true)) {
                    return;
                }
                if (subject.length() == 0) {
                    setErrorInFragment(mSubject, "موضوع پیام نمی‌تواند خالی باشد.");
                    return;
                }
                if (body.length() == 0) {
                    setErrorInFragment(mBody, "متن پیام نمی‌تواند خالی باشد.");
                    return;
                }

                new CreateMessageAsync().execute();

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

    public class CreateMessageAsync extends AsyncTask<Void, Void, CreateMessageReply> {

        String cBody;
        String cSubject;
        String cPhone;
        String cEmail;

        CreateMessageRequest createMessageRequest;
        String cToastMsg = "";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            cPhone = getTextInFragment(mPhone);
            cEmail = getTextInFragment(mEmail);
            cSubject = getTextInFragment(mSubject);

            cBody = cSubject;
            cBody += "\n <br> " + " From email: " + cEmail;
            cBody += "\n <br> " + " From phone: " + cPhone;
            cBody += "\n <br> " + getTextInFragment(mBody);

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
                cBody += "\n <br> Device Info:  \n" +
                        " <br>" + s;
            }

            displayProgressDialog(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    if (!TextUtils.isEmpty(cToastMsg)) {
                        displayLongToast(cToastMsg);
                        cToastMsg = "";
                    }
                }
            });
        }

        @Override
        protected CreateMessageReply doInBackground(Void... voids) {

            if (TextUtils.isEmpty(cBody)) {
                return null;
            }

            createMessageRequest = new CreateMessageRequest();

            RequestProperties rp = GRPCUtils.newRPWithDeviceInfo();
            createMessageRequest.requestProperties = rp;
            createMessageRequest.message = cBody;

            if (BuildConfig.DEBUG) {
                logD("createMessageRequest is: " + createMessageRequest);
            }
            CommunicationServiceGrpc.CommunicationServiceBlockingStub stub;
            try {
                stub = GRPCUtils.getInstance().getCommunicationServiceGrpc();
            } catch (Exception e) {
                logE("Error running CreateMessageRequest. createMessageRequest: " + createMessageRequest + ". Error:" + e, e);
                return null;
            }

            CreateMessageReply createMessageReply = stub.createMessage(createMessageRequest);
            return createMessageReply;
        }

        @Override
        protected void onPostExecute(CreateMessageReply createMessageReply) {
            super.onPostExecute(createMessageReply);

            Activity activity = getActivity();

            if (BuildConfig.DEBUG) {
                logD("createMessageReply is: " + createMessageReply);
            }

            if (createMessageReply != null) {
                switch (createMessageReply.replyProperties.statusCode) {
                    case ReplyProperties.CLIENT_ERROR:
                        switch (ReplyProperties.CLIENT_ERROR) {
                            case CreateMessageReply.NO_CLIENT_ERROR:
                                logE("Got NO_CLIENT_ERROR code createMessageReply. createMessageRequest: " + createMessageRequest);
                                cToastMsg = getStringInFragment(R.string.error_getting_info);
                                break;
                            case CreateMessageReply.INVALID_TO_ERROR:
                                //TODO not implemented yet
                                logE("Got INVALID_TO_ERROR code createMessageReply. createMessageRequest: " + createMessageRequest);
                                break;
                            case CreateMessageReply.TOO_MUCH_MESSAGES_ERROR:
                                cToastMsg = getStringInFragment(R.string.message_limit_exceeded);
                                break;
                        }
                        break;

                    case ReplyProperties.SERVER_ERROR:
                        cToastMsg = getStringInFragment(R.string.server_error_try_again);
                        break;

                    case ReplyProperties.NOT_AUTHORIZED:
                        displayLongToast(getStringInFragment(R.string.not_allowed_to_do_this_action));
                        break;

                    case ReplyProperties.OK:
                        cToastMsg = getStringInFragment(R.string.contact_sent_successfully);
                        break;
                }
            } else {
                cToastMsg = getStringInFragment(R.string.error_connecting_server_try_again);
//                if (activity != null) {
//                    int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(activity);
//                    if (status != ConnectionResult.SUCCESS) {
//                        logE("GooglePlayServices is not available. ConnectionResult: " + status);
//                        ((Dialog) GooglePlayServicesUtil.getErrorDialog(status, activity, 10)).show();
//                    }
//                }
            }

            dismissProgressDialog();
        }
    }
}
