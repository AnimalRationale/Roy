package pl.appnode.roy;

import android.app.Application;
import android.content.Context;

/**
 * Provides application context.
 */
public class AppContextHelper extends Application {
    private static Context sContext;

    @Override
    public void onCreate() {
        super.onCreate();
        sContext = getApplicationContext();
    }

    /**
     * Returns application context.
     *
     * @return application context
     */
    public static Context getContext() {
        return sContext;
    }
}