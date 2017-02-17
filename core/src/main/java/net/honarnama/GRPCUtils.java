package net.honarnama;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.security.ProviderInstaller;

import net.honarnama.base.BuildConfig;
import net.honarnama.nano.AndroidClientInfo;
import net.honarnama.nano.AuthServiceGrpc;
import net.honarnama.nano.BrowseServiceGrpc;
import net.honarnama.nano.CommunicationServiceGrpc;
import net.honarnama.nano.MetaServiceGrpc;
import net.honarnama.nano.RequestProperties;
import net.honarnama.nano.SellServiceGrpc;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

import io.grpc.CallOptions;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

/**
 * Created by reza on 3/26/16.
 */
public class GRPCUtils {

    private String mSecureHost = BuildConfig.SECURE_HOST;
    private String mInsecureHost = BuildConfig.INSECURE_HOST;
    private int mSecurePort = BuildConfig.SECURE_PORT;
    private int mInsecurePort = BuildConfig.INSECURE_PORT;
    private ManagedChannel mChannel;

    static private GRPCUtils singleton;

    public static GRPCUtils getInstanceIfCreated() {
        return singleton;
    }

    public synchronized static GRPCUtils getInstance() {
        singleton = new GRPCUtils();
        return singleton;
    }

    private GRPCUtils() {
        boolean throughTLS = true;
        try {
            ProviderInstaller.installIfNeeded(HonarnamaBaseApp.getInstance());
        } catch (Exception ex) {
            Log.i("GRPCUtils", "ProviderInstaller.installIfNeeded failed.", ex);
            int sdk = android.os.Build.VERSION.SDK_INT;
            if (sdk < android.os.Build.VERSION_CODES.LOLLIPOP) {
                throughTLS = false;
            }
        }
        // debug thro
        if (BuildConfig.DEBUG) {
            Log.i("GRPCUtils", "throughTLS: " + throughTLS);
        }

        if (throughTLS) {
            mChannel = ManagedChannelBuilder.forAddress(mSecureHost, mSecurePort)
                    .build();
        } else {
            mChannel = ManagedChannelBuilder.forAddress(mInsecureHost, mInsecurePort)
                    .usePlaintext(true)
                    .build();
        }
    }

    public void close() throws InterruptedException {
        mChannel.shutdown().awaitTermination(1, TimeUnit.SECONDS);
    }

    public static RequestProperties newRPWithDeviceInfo() {
        RequestProperties rp = new RequestProperties();
        rp.androidClientInfo = new AndroidClientInfo();
        rp.metaETag = HonarnamaBaseApp.getCurrentMetaVersion();

        Application app = HonarnamaBaseApp.getInstance();
        String packageName = app.getPackageName();
        rp.clientId = packageName;
        try {
            PackageInfo pInfo = app.getPackageManager().getPackageInfo(packageName, 0);
            rp.clientVersion = pInfo.versionCode;
        } catch (PackageManager.NameNotFoundException nnfe) {
            // Nevermind!
        }

        TelephonyManager tel = (TelephonyManager) HonarnamaBaseApp.getInstance().getSystemService(Context.TELEPHONY_SERVICE);
        String networkOperator = tel.getNetworkOperator();
        if (!TextUtils.isEmpty(networkOperator)) {
            rp.androidClientInfo.mccMnc = networkOperator;
        }

        String loginToken = HonarnamaBaseApp.getCommonSharedPref().getString(HonarnamaBaseApp.PREF_KEY_LOGIN_TOKEN, "");
        rp.userAuthToken = loginToken;

        rp.androidClientInfo.locale = Locale.getDefault().getLanguage();
        rp.androidClientInfo.country = Locale.getDefault().getCountry();

        // TODO: cache static values

        return rp;
    }

    public MetaServiceGrpc.MetaServiceBlockingStub getMetaServiceGrpc() {
        // TODO: cache
        return MetaServiceGrpc.newBlockingStub(mChannel).withDeadlineAfter(15, TimeUnit.SECONDS);
    }

    public AuthServiceGrpc.AuthServiceBlockingStub getAuthServiceGrpc() {
        // TODO: cache
        return AuthServiceGrpc.newBlockingStub(mChannel).withDeadlineAfter(15, TimeUnit.SECONDS);
    }

    public SellServiceGrpc.SellServiceBlockingStub getSellServiceGrpc() {
        // TODO: cache
        return SellServiceGrpc.newBlockingStub(mChannel).withDeadlineAfter(20, TimeUnit.SECONDS);
    }

    public CommunicationServiceGrpc.CommunicationServiceBlockingStub getCommunicationServiceGrpc() {
        // TODO: cache
        return CommunicationServiceGrpc.newBlockingStub(mChannel).withDeadlineAfter(10, TimeUnit.SECONDS);
    }

    public BrowseServiceGrpc.BrowseServiceBlockingStub getBrowseServiceGrpc() {
        // TODO: cache
        return BrowseServiceGrpc.newBlockingStub(mChannel).withDeadlineAfter(15, TimeUnit.SECONDS).withInterceptors();
    }


    public void processReplyProperties() {
        //TODO
    }
}
