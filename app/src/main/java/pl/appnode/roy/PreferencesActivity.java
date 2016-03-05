package pl.appnode.roy;

import android.app.ActionBar;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.annotation.LayoutRes;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatDelegate;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import static pl.appnode.roy.Constants.KEY_SETTINGS_ORIENTATION;
import static pl.appnode.roy.Constants.KEY_SETTINGS_THEME;
import static pl.appnode.roy.PreferencesSetupHelper.orientationSetup;
import static pl.appnode.roy.PreferencesSetupHelper.themeSetup;

/**
 * Displays application's preferences settings and handles changes in settings.
 */
public class PreferencesActivity extends PreferenceActivity
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    private AppCompatDelegate mAppCompatDelegate;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        themeSetup(this);
        super.onCreate(savedInstanceState);
        getDelegate().installViewFactory();
        getDelegate().onCreate(savedInstanceState);
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setIcon(R.mipmap.ic_launcher);
        }
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new RoyPreferenceFragment()).commit();
    }

    public static class RoyPreferenceFragment extends PreferenceFragment
    {
        @Override
        public void onCreate(final Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.activity_preferences);
        }
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                                          String key) {
        if (key.equals(KEY_SETTINGS_THEME)) {
            this.recreate();
        }
        if (key.equals(KEY_SETTINGS_ORIENTATION)) {
            orientationSetup(this);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        orientationSetup(this);
    }

    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        getDelegate().setContentView(layoutResID);
    }

    @Override
    public void setContentView(View view) {
        getDelegate().setContentView(view);
    }

    @Override
    public void setContentView(View view, ViewGroup.LayoutParams params) {
        getDelegate().setContentView(view, params);
    }

    @Override
    public void addContentView(View view, ViewGroup.LayoutParams params) {
        getDelegate().addContentView(view, params);
    }


    @Override
    protected void onPostResume() {
        super.onPostResume();
        getDelegate().onPostResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
        getDelegate().onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getDelegate().onDestroy();
    }

    private AppCompatDelegate getDelegate() {
        if (mAppCompatDelegate == null) {
            mAppCompatDelegate = AppCompatDelegate.create(this, null);
        }
        return mAppCompatDelegate;
    }
}
