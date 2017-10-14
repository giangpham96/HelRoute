package leo.me.la.finroute.http;

import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.CustomTypeAdapter;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import leo.me.la.finroute.type.CustomType;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

@Module
public class ApiModule {
    private final String BASE_PLACE_URL = "http://api.digitransit.fi/";
    private final String BASE_ROUTE_URL = "https://api.digitransit.fi/routing/v1/routers/hsl/index/graphql";

    @Singleton
    @Provides
    OkHttpClient provideClient() {

        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        return new OkHttpClient.Builder().addInterceptor(interceptor).build();
    }

    @Singleton
    @Provides
    Retrofit provideRetrofit(String baseURL, OkHttpClient client) {
        return new Retrofit.Builder()
                .baseUrl(baseURL)
                .client(client)
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    @Singleton
    @Provides
    GetPlaceApiService provideApiService() {
        return provideRetrofit(BASE_PLACE_URL, provideClient()).create(GetPlaceApiService.class);
    }

    @Provides
    @Singleton
    ApolloClient provideApolloClient() {
        CustomTypeAdapter<Long> customTypeAdapter = new CustomTypeAdapter<Long>() {

            @Override
            public Long decode(String value) {
                return Long.parseLong(value);
            }

            @Override
            public String encode(Long value) {
                return value.toString();
            }
        };
        return ApolloClient.builder()
                .serverUrl(BASE_ROUTE_URL)
                .okHttpClient(provideClient())
                .addCustomTypeAdapter(CustomType.LONG, customTypeAdapter)
                .build();
    }
}
