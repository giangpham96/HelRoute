package leo.me.la.finroute.getRoutes;

import javax.inject.Singleton;

import dagger.Component;
import leo.me.la.finroute.http.ApiModule;

@Singleton
@Component(modules = {ApiModule.class, GetRoutesModule.class})
interface GetRoutesComponent {
    void inject(GetRoutesActivity target);
}
