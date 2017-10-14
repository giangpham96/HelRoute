package leo.me.la.finroute.searchPlaces;

import javax.inject.Singleton;

import dagger.Component;
import leo.me.la.finroute.http.ApiModule;

@Singleton
@Component(modules = {ApiModule.class, SearchPlaceModule.class})
interface SearchPlaceComponent {
    void inject(SearchPlaceActivity target);
}
