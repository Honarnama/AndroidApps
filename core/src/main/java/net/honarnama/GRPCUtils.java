package net.honarnama;

import net.honarnama.nano.AndroidClientInfo;
import net.honarnama.nano.AuthServiceGrpc;
import net.honarnama.nano.MetaServiceGrpc;
import net.honarnama.nano.MetaReply;
import net.honarnama.nano.RequestProperties;
import net.honarnama.nano.SellServiceGrpc;
import net.honarnama.nano.SimpleRequest;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import java.util.concurrent.TimeUnit;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

/**
 * Created by reza on 3/26/16.
 */
public class GRPCUtils {

    private String mHost = "honarnama.net"; // TODO: read from gradle
    private int mPort = 443; // TODO: read from gradle
    private ManagedChannel mChannel;

    static private GRPCUtils singleton;

    public static GRPCUtils getInstanceIfCreated() {
        return singleton;
    }

    public synchronized static GRPCUtils getInstance() throws InterruptedException {
        singleton = new GRPCUtils();
        return singleton;
    }

    private GRPCUtils() throws InterruptedException {
        mChannel = ManagedChannelBuilder.forAddress(mHost, mPort)
                .build();
    }

    public void close() throws InterruptedException {
        mChannel.shutdown().awaitTermination(1, TimeUnit.SECONDS);
    }

    public static RequestProperties newRPWithDeviceInfo() {
        RequestProperties rp = new RequestProperties();
        rp.androidClientInfo = new AndroidClientInfo();

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

        // TODO: rp.userLanguage;
        // TODO: rp.userCountry
        // TODO: cache static values

        return rp;
    }

    // Not to be run in the UI thread
    public MetaReply getMetaData(long currentMetaVersion) {
        RequestProperties rp = newRPWithDeviceInfo();
        rp.ifNotMatchEtag = currentMetaVersion;
        SimpleRequest req = new SimpleRequest();
        req.requestProperties = rp;

        Log.w("GRPC-HN", "updateMetaData :: rp= " + rp);

        MetaServiceGrpc.MetaServiceBlockingStub stub = getMetaServiceGrpc();
        Log.w("GRPC-HN", "updateMetaData :: stub= " + stub);
        MetaReply reply = stub.meta(req);
        Log.w("GRPC-HN", "updateMetaData :: Got Reply");
        if (reply.replyProperties != null) {
            Log.w("GRPC-HN", "updateMetaData :: reply.statusCode= " + reply.replyProperties.statusCode);
            Log.w("GRPC-HN", "updateMetaData :: reply.serverVersion= " + reply.replyProperties.serverVersion);
        }
//
//        // Test Part
//        StringBuilder sb = new StringBuilder();
//        for (EventCategory ev : reply.eventCategories) {
//            sb.append(ev.name);
//            sb.append(" / ");
//        }
        return reply;
    }

    public MetaServiceGrpc.MetaServiceBlockingStub getMetaServiceGrpc() {
        // TODO: cache
        return MetaServiceGrpc.newBlockingStub(mChannel);
    }

    public AuthServiceGrpc.AuthServiceBlockingStub getAuthServiceGrpc() {
        // TODO: cache
        return AuthServiceGrpc.newBlockingStub(mChannel);
    }

    public SellServiceGrpc.SellServiceBlockingStub getSellServiceGrpc() {
        // TODO: cache
        return SellServiceGrpc.newBlockingStub(mChannel);
    }

    public void processReplyProperties()
    {
        //TODO
    }
}
