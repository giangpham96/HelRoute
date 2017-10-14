package leo.me.la.finroute.http;

import leo.me.la.finroute.http.apiModel.Response;
import retrofit2.http.GET;
import retrofit2.http.Query;
import rx.Observable;

public interface GetPlaceApiService {

    @GET("/geocoding/v1/search/")
    Observable<Response> getPlaces(@Query("text") String name,
                                   @Query("size") int size,
                                   @Query("lang") String lang,
                                   @Query("boundary.rect.min_lat") float minLat,
                                   @Query("boundary.rect.max_lat") float maxLat,
                                   @Query("boundary.rect.min_lon") float minLon,
                                   @Query("boundary.rect.max_lon") float maxLon);

    @GET("/geocoding/v1/reverse/")
    Observable<Response> getPlacesFromLatLong(@Query("point.lat") double lat,
                                              @Query("point.lon") double lon,
                                              @Query("size") int size,
                                              @Query("lang") String lang);
}
