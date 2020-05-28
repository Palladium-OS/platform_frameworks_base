package android.content.res;

import android.graphics.Color;
import android.os.SystemProperties;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import android.content.res.Resources;
import android.content.Context;
import android.content.res.Configuration;
import android.app.ActivityThread;


/** @hide */
public class AccentUtils {
    private static final String TAG = "AccentUtils";

    private static ArrayList<String> accentResources = new ArrayList<>(
            Arrays.asList("accent_device_default",
                    "accent_device_default_light",
                    "accent_device_default_dark",
                    "material_pixel_blue_dark",
                    "material_pixel_blue_bright",
                    "colorAccent",
                    "alert_dialog_color_accent_light",
                    "alert_dialog_color_accent_dark",
                    "lockscreen_clock_accent_color",
                    "dismiss_all_icon_color",
                    "material_pixel_blue_dark",
                    "material_pixel_blue_bright",
                    "omni_color5",
                    "omni_color4",
                    "oneplus_accent_color",
                    "oneplus_accent_text_color",
                    "dialer_theme_color",
                    "dialer_theme_color_dark",
                    "dialer_theme_color_20pct",
                    "settings_accent_color",
                    "settingsHeaderColor",
                    "gradient_start"));

    private static ArrayList<String> systemuiResources = new ArrayList<>(
            Arrays.asList("systemui_panel",
                          "systemui_panel_dark"));

    private static ArrayList<String> notifResources = new ArrayList<>(
            Arrays.asList("notification_new",
                            "notification_new_dark"));

    private static final String ACCENT_COLOR_PROP = "persist.sys.theme.accentcolor";
    private static final String ACCENT_COLOR_PROP_DARK = "persist.sys.theme.accentcolordark";
    private static final String SYSTEMUI_COLOR_PROP = "persist.sys.theme.systemuicolor";
    private static final String SYSTEMUI_COLOR_PROP_DARK = "persist.sys.theme.systemuicolordark";
    private static final String NOTIF_COLOR_PROP = "persist.sys.theme.notifcolor";
    private static final String NOTIF_COLOR_PROP_DARK = "persist.sys.theme.notifcolordark";
    


    static boolean isResourceAccent(String resName) {
        for (String ar : accentResources)
            if (resName.contains(ar))
                return true;
        return false;
    }

    static boolean isResourceNotif(String resName) {
        for (String su : notifResources)
            if (resName.contains(su))
                return true;
        return false;
    }

    static boolean isResourceSystemUI(String resName) {
        for (String su : systemuiResources)
            if (resName.contains(su))
                return true;
        return false;
    }
    static boolean isDark(){
        final Context context = ActivityThread.currentApplication();
        Resources mResources = (Resources) context.getResources();
        int nightModeFlags =mResources.getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        boolean flg;       
        switch (nightModeFlags) {
            case Configuration.UI_MODE_NIGHT_YES:
                flg=true;
                break;
            case Configuration.UI_MODE_NIGHT_NO:
                flg=false;
                break;
            default:
                flg=false;
                break;
        }
        Log.e(TAG,"RGB"+flg); 
        return flg;
    }
    public static int getNewAccentColor(int defaultColor) {
        if(isDark()){
             return getAccentColor(defaultColor, ACCENT_COLOR_PROP_DARK);
        }
        else{
             return getAccentColor(defaultColor, ACCENT_COLOR_PROP);
        }
    }
    public static int getNewSystemUIColor(int defaultColor) {
        if(isDark()){
            return getAccentColor(defaultColor, SYSTEMUI_COLOR_PROP_DARK);
        }
        else{
            return getAccentColor(defaultColor, SYSTEMUI_COLOR_PROP);
        }
    }

    public static int getNewNotifColor(int defaultColor) {
        if(isDark()){
            return getAccentColor(defaultColor, NOTIF_COLOR_PROP_DARK);
        }
        else{
            return getAccentColor(defaultColor, NOTIF_COLOR_PROP);
        }
    }


    private static int getAccentColor(int defaultColor, String property) {
        try {
            String colorValue = SystemProperties.get(property, "-1");
            return "-1".equals(colorValue)
                    ? defaultColor
                    : Color.parseColor("#" + colorValue);
        } catch (Exception e) {
            Log.e(TAG, "Failed to set accent: " + e.getMessage() +
                    "\nSetting default: " + defaultColor);
            return defaultColor;
        }
    }
}
