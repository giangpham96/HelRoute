package leo.me.la.finroute.root;

import android.app.Application;

import io.realm.Realm;

public class HelRoute extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        SharedPrefManager.init(this);
        Realm.init(this);
    }
}
