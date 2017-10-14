package leo.me.la.finroute.getRoutes;

import com.apollographql.apollo.ApolloClient;

import dagger.Module;
import dagger.Provides;

@Module
class GetRoutesModule {
    @Provides
    GetRoutesMVP.Model provideModel(ApolloClient client) {
        return new GetRoutesModel(client);
    }

    @Provides
    GetRoutesMVP.Presenter providePresenter(GetRoutesMVP.Model model) {
        return new GetRoutesPresenter(model);
    }
}
