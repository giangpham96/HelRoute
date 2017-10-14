package leo.me.la.finroute.searchPlaces;

import dagger.Module;
import dagger.Provides;
import leo.me.la.finroute.http.GetPlaceApiService;

@Module
class SearchPlaceModule {
    @Provides
    SearchPlaceMVP.Presenter providePresenter(SearchPlaceMVP.Model model) {
        return new SearchPlacePresenter(model);
    }

    @Provides
    SearchPlaceMVP.Model provideModel(GetPlaceApiService apiService) {
        return new SearchPlaceModel(apiService);
    }
}
