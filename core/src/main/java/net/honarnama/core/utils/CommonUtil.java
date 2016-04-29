package net.honarnama.core.utils;

import net.honarnama.HonarnamaBaseApp;

import android.content.Context;
import android.content.pm.PackageManager;

/**
 * Created by elnaz on 2/6/16.
 */
public class CommonUtil {
    public static boolean isPackageInstalled(String packagename) {
        PackageManager pm = HonarnamaBaseApp.getInstance().getPackageManager();
        try {
            pm.getPackageInfo(packagename, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }
}
