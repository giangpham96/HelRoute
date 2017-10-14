package leo.me.la.finroute.searchPlaces;

import android.support.annotation.NonNull;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;
import io.realm.Sort;
import leo.me.la.finroute.http.GetPlaceApiService;
import leo.me.la.finroute.http.apiModel.Feature;
import leo.me.la.finroute.http.apiModel.Response;
import rx.Observable;
import rx.functions.Func1;

class SearchPlaceModel implements SearchPlaceMVP.Model {
    private final float BOTTOM_BOUNDARY = 59.78543f;
    private final float TOP_BOUNDARY = 60.50623f;
    private final float LEFT_BOUNDARY = 24.26056f;
    private final float RIGHT_BOUNDARY = 25.52875f;
    private Realm realm;
    private RealmConfiguration config;
    private GetPlaceApiService getPlaceApiService;

    SearchPlaceModel(GetPlaceApiService getPlaceApiService) {
        this.getPlaceApiService = getPlaceApiService;
        config = new RealmConfiguration.Builder()
                .name("recent-search.realm")
                .deleteRealmIfMigrationNeeded()
                .build();
        realm = Realm.getInstance(config);
    }

    @Override
    public Observable<Feature> current(double lat, double lon) {
        return getPlaceApiService.getPlacesFromLatLong(lat, lon, 1, "fi").concatMap(new Func1<Response, Observable<? extends Feature>>() {
            @Override
            public Observable<? extends Feature> call(Response response) {
                return Observable.just(response.getFeatures().get(0));
            }
        });
    }

    @Override
    public Observable<Feature> result(String name) {
        return getPlaceApiService.getPlaces(name,
                10,
                "fi",
                BOTTOM_BOUNDARY,
                TOP_BOUNDARY,
                LEFT_BOUNDARY,
                RIGHT_BOUNDARY).concatMap(new Func1<Response, Observable<Feature>>() {
            @Override
            public Observable<Feature> call(Response response) {
                return Observable.from(response.getFeatures());
            }
        });
    }

    @Override
    public RealmResults<leo.me.la.finroute.searchPlaces.realmModel.Feature> recent() {
        return realm.where(leo.me.la.finroute.searchPlaces.realmModel.Feature.class)
                .findAllSortedAsync("timestamp", Sort.DESCENDING);
    }

    @Override
    public void removeRecent() {
        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(@NonNull Realm realm) {
                realm.where(leo.me.la.finroute.searchPlaces.realmModel.Feature.class)
                        .findAll()
                        .deleteAllFromRealm();
            }
        });
    }

    @Override
    public void addRecent(Feature feature) {
        leo.me.la.finroute.searchPlaces.realmModel.Feature saved = realm
                .where(leo.me.la.finroute.searchPlaces.realmModel.Feature.class)
                .equalTo("properties.id", feature.getProperties().getId())
                .findFirst();
        if (saved != null) {
            realm.beginTransaction();
            saved.getGeometry().deleteFromRealm();
            saved.getProperties().deleteFromRealm();
            saved.deleteFromRealm();
            realm.commitTransaction();
        }
        RealmResults<leo.me.la.finroute.searchPlaces.realmModel.Feature> recents = realm
                .where(leo.me.la.finroute.searchPlaces.realmModel.Feature.class)
                .findAllSorted("timestamp", Sort.DESCENDING);
        if (recents.size() >= 10) {
            realm.beginTransaction();
            recents.deleteLastFromRealm();
            realm.commitTransaction();
        }
        leo.me.la.finroute.searchPlaces.realmModel.Feature save = feature.convertToRealmFeature();
        realm.beginTransaction();
        realm.copyToRealm(save);
        realm.commitTransaction();
    }

    @Override
    public void close() {
        if (realm != null && !realm.isClosed()) {
            if (realm.isInTransaction()) {
                realm.cancelTransaction();
            }
            realm.close();
            Realm.compactRealm(config);
        }
    }
}
