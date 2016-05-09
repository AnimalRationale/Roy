package pl.appnode.roy;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import static pl.appnode.roy.Constants.PREF_ACCOUNT_NAME;

/**
 * Shows information dialog with application's launcher icon, name, version and code version
 */
class AboutDialog {

    private static String LOGTAG = "AboutDialog";
    private static String sVersionName;
    private static String sVersionCode;
    private static String sSavedAccountName;
    private static String sCredentialsAccountName;
    private static AlertDialog sAboutDialog;

    private static void versionInfo(Context context) {
        try {
            PackageManager manager = context.getPackageManager();
            PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
            sVersionName = info.versionName;
            sVersionCode = String.valueOf(info.versionCode);
        }
        catch (PackageManager.NameNotFoundException ex) {
            sVersionName = context.getResources().getString(R.string.dialog_about_version_name_error);
            sVersionCode = context.getResources().getString(R.string.dialog_about_version_code_error);
        }
    }

    private static void accountNameSavedInPreferences(Activity context) {
        SharedPreferences settings = context.getPreferences(Context.MODE_PRIVATE);
        sSavedAccountName = settings.getString(PREF_ACCOUNT_NAME, null);
        Log.d(LOGTAG, "Account name: " + sSavedAccountName);
    }

    private static void accountNameInCredentials() {
        sCredentialsAccountName = MainActivity.getCredentialsAccountName();
        Log.d(LOGTAG, "Account name in credentials: " + sCredentialsAccountName);
    }

    public static void showDialog(Activity callingActivity) {
        versionInfo(callingActivity);
        accountNameSavedInPreferences(callingActivity);
        accountNameInCredentials();
        String aboutVersion = sVersionName + "." + sVersionCode;
        LayoutInflater layoutInflater = LayoutInflater.from(callingActivity);
        View aboutDialogView = layoutInflater.inflate(R.layout.dialog_about, null) ;
        TextView textAbout = (TextView) aboutDialogView.findViewById(R.id.aboutDialogInfo);
        textAbout.setText(aboutVersion);
        if (sSavedAccountName != null && sCredentialsAccountName != null) {
            View accountInfo = aboutDialogView.findViewById(R.id.aboutDialogGoogleAccountInfo);
            accountInfo.setVisibility(View.VISIBLE);
            TextView textSavedAccountName = (TextView) aboutDialogView.findViewById(R.id.aboutDialogSavedAccountName);
            textSavedAccountName.setText(sSavedAccountName);
            TextView textCredentialsAccountName = (TextView) aboutDialogView.findViewById(R.id.aboutDialogCredentialsAccountName);
            textCredentialsAccountName.setText(sCredentialsAccountName);
        }
        TextView textRemoteDatabse = (TextView) aboutDialogView.findViewById(R.id.aboutDialogFirebaseAddress);
        textRemoteDatabse.setText(BuildConfig.FB_BASE_ADDRESS);
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(callingActivity);
        alertDialog.setTitle(callingActivity.getResources().getString(R.string.dialog_about_title) + callingActivity.getString(R.string.app_name))
                .setIcon(R.mipmap.ic_launcher)
                .setCancelable(true)
                .setPositiveButton(callingActivity.getResources().getString(R.string.dialog_about_ok), null)
                .setView(aboutDialogView);
        sAboutDialog = alertDialog.show();
    }

    public static void dismissDialog() {
        if (sAboutDialog != null) {
            sAboutDialog.dismiss();
        }
    }
}