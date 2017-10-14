package leo.me.la.finroute.searchPlaces;

import android.support.annotation.NonNull;

import io.realm.RealmChangeListener;
import io.realm.RealmResults;
import leo.me.la.finroute.http.apiModel.Feature;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

class SearchPlacePresenter implements SearchPlaceMVP.Presenter, RealmChangeListener<RealmResults<leo.me.la.finroute.searchPlaces.realmModel.Feature>> {
    private SearchPlaceMVP.View view;
    private SearchPlaceMVP.Model model;
    private Subscription subscriptionResult = null;
    private Subscription subscriptionCurrent = null;
    private RealmResults<leo.me.la.finroute.searchPlaces.realmModel.Feature> recent;

    SearchPlacePresenter(SearchPlaceMVP.Model model) {
        this.model = model;
    }

    @Override
    public void loadCurrent(double lat, double lon) {
        rxCurrentUnsubscribe();
//        view.disableMyLocation();
        subscriptionCurrent = model.current(lat, lon)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Feature>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        if (view != null)
                            view.setCurrentLocation(null);
                    }

                    @Override
                    public void onNext(Feature feature) {
                        if (view != null) {
                            view.setCurrentLocation(feature);
//                            view.enableMyLocation();
                        }
                    }
                });
    }

    @Override
    public void loadResult() {
        rxResultUnsubscribe();
        emptyRecent();
        view.showProgress();
        view.hideAnnouncement();
        view.hideContent();
        view.showResult();
        subscriptionResult = model.result(view.getQueryText())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Feature>() {
                    @Override
                    public void onCompleted() {
                        if (view != null && view.getPlacesListSize() == 0) {
                            view.hideContent();
                            view.hideProgress();
                            view.showEmptyResult();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (view != null) {
                            view.hideContent();
                            view.hideProgress();
                            view.showResultPlaceError();
                        }
                    }

                    @Override
                    public void onNext(Feature feature) {
                        String localadmin = feature.getProperties().getLocaladmin();
                        boolean isInHSLRegion = localadmin != null && (
                                localadmin.equals("Helsinki")
                                        || localadmin.equals("Espoo")
                                        || localadmin.equals("Kerava")
                                        || localadmin.equals("Kirkkonummi")
                                        || localadmin.equals("Vantaa")
                                        || localadmin.equals("Sipoo")
                                        || localadmin.equals("Kauniainen")
                        );
                        if (view != null && isInHSLRegion) {
                            view.hideProgress();
                            view.showContent();
                            view.hideAnnouncement();
                            view.updateResultPlaceItem(feature);
                        }
                    }
                });

    }

    private void emptyRecent() {
        if (recent != null) {
            recent.removeAllChangeListeners();
            recent = null;
        }
    }

    @Override
    public void loadRecent() {
        rxResultUnsubscribe();
        emptyRecent();
        view.hideProgress();
        view.showRecent();
        recent = model.recent();
        recent.addChangeListener(this);
    }

    @Override
    public void removeRecent() {
        model.removeRecent();
    }

    @Override
    public void saveRecent(Feature feature) {
        model.addRecent(feature);
    }

    @Override
    public void setView(SearchPlaceMVP.View view) {
        this.view = view;
    }

    @Override
    public void close() {
        rxCurrentUnsubscribe();
        rxResultUnsubscribe();
        emptyRecent();
        setView(null);
        model.close();
    }

    private void rxResultUnsubscribe() {
        if (subscriptionResult != null) {
            if (!subscriptionResult.isUnsubscribed()) {
                subscriptionResult.unsubscribe();
            }
        }
    }

    private void rxCurrentUnsubscribe() {
        if (subscriptionCurrent != null) {
            if (!subscriptionCurrent.isUnsubscribed()) {
                subscriptionCurrent.unsubscribe();
            }
        }
    }

    @Override
    public void onChange(@NonNull RealmResults<leo.me.la.finroute.searchPlaces.realmModel.Feature> features) {
        if (features.isLoaded()) {
            if (view != null) {
                if (features.isEmpty()) {
                    view.hideContent();
                    view.showEmptyRecent();
                }
                else {
                    view.showContent();
                    view.hideAnnouncement();
                }
                view.updateRecentPlaceItems(features);
            }
        }
    }
}
