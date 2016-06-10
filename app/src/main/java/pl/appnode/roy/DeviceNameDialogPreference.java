package pl.appnode.roy;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import static pl.appnode.roy.PreferencesSetupHelper.isDarkTheme;

public class DeviceNameDialogPreference extends EditTextPreference {

    public DeviceNameDialogPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public DeviceNameDialogPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void showDialog(Bundle state) {
        super.showDialog(state);
        final Resources resources = getContext().getResources();
        final Window window = getDialog().getWindow();
        final int titleColor = ContextCompat.getColor(getContext(),R.color.colorPrimaryMaterial);
        final int titleId = resources.getIdentifier("alertTitle", "id", "android");
        final View title = window.findViewById(titleId);
        if (title != null) {
            ((TextView) title).setTextColor(titleColor);
        }
        final int titleDividerId = resources.getIdentifier("titleDivider", "id", "android");
        final View titleDivider = window.findViewById(titleDividerId);
        if (titleDivider != null) {
            titleDivider.setBackgroundColor(titleColor);
        }
        if (isDarkTheme(getContext())) {
            window.getDecorView().setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorDarkGrey));
            super.getEditText().setTextColor(ContextCompat.getColor(getContext(), R.color.colorWhite));
        }
    }
}