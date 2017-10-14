package leo.me.la.finroute.showAlert;

import javax.inject.Singleton;

import dagger.Component;
import leo.me.la.finroute.http.ApiModule;

@Singleton
@Component(modules = {ApiModule.class})
interface AlertComponent {
    void inject(AlertActivity target);
}
