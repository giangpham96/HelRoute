package leo.me.la.finroute.searchPlaces;

import io.realm.RealmResults;
import leo.me.la.finroute.http.apiModel.Feature;
import rx.Observable;

interface SearchPlaceMVP {
    interface View {
        void showRecent();

        void showResult();

        void updateResultPlaceItem(Feature feature);

        void updateRecentPlaceItems(RealmResults<leo.me.la.finroute.searchPlaces.realmModel.Feature> features);

        void showResultPlaceError();

        void showProgress();

        void hideProgress();

        void showContent();

        void hideContent();

        void enableMyLocation();

        void disableMyLocation();

        void setCurrentLocation(Feature feature);

        String getQueryText();

        void showEmptyRecent();

        void showEmptyResult();

        void hideAnnouncement();

        int getPlacesListSize();
    }

    interface Presenter {
        void loadCurrent(double lat, double lon);

        void loadResult();

        void loadRecent();

        void removeRecent();

        void setView(View view);

        void saveRecent(Feature feature);

        void close();
    }

    interface Model {
        Observable<Feature> current(double lat, double lon);

        Observable<Feature> result(String name);

        RealmResults<leo.me.la.finroute.searchPlaces.realmModel.Feature> recent();

        void removeRecent();

        void addRecent(Feature feature);

        void close();
    }
}
