package calebpaul.quietpocket;

import android.app.Application;

import io.realm.Realm;

/**
 * Created by calebpaul on 12/26/16.
 */

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Realm.init(this);
    }
}
