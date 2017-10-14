package leo.me.la.finroute.getRoutes;

import android.util.Log;

import leo.me.la.finroute.RouteQuery;
import leo.me.la.finroute.root.Utils;
import leo.me.la.finroute.type.InputCoordinates;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

class GetRoutesPresenter implements GetRoutesMVP.Presenter {

    private final int RESULT_NOW = 0;
    private final int RESULT_EARLIER = 1;
    private final int RESULT_LATER = 2;
    private GetRoutesMVP.Model model;
    private GetRoutesMVP.View view;
    private Subscription subscription;

    GetRoutesPresenter(GetRoutesMVP.Model model) {
        this.model = model;
    }

    @Override
    public void loadResult() {
        Log.e(getClass().getName(), "loadResult");
        rxUnsubscribe();
        if (view != null && view.getDestination() != null && view.getOrigin() != null) {
            view.hideAnnouncement();
            view.showProgress();
            load(view.getTransportMode(), view.getMaxTransfer(),
                    view.getWalkingReluctance(), view.getWalkingSpeed(),
                    view.getOrigin(), view.getDestination(),
                    view.isArriveBy(),
                    formatDate(view.getTimestamp()), formatTime(view.getTimestamp()),
                    RESULT_NOW);
        }
    }

    @Override
    public void loadEarlier() {
        rxUnsubscribe();
        if (view != null && view.getDestination() != null && view.getOrigin() != null) {
            view.hideAnnouncement();
            view.showProgress();
            load(view.getTransportMode(), view.getMaxTransfer(),
                    view.getWalkingReluctance(), view.getWalkingSpeed(),
                    view.getOrigin(), view.getDestination(),
                    true,
                    formatDate(view.getEarliestEndTime()), formatTime(view.getEarliestEndTime()),
                    RESULT_EARLIER);
        }
    }

    @Override
    public void loadLater() {
        rxUnsubscribe();
        if (view != null && view.getDestination() != null && view.getOrigin() != null) {
            view.hideAnnouncement();
            view.showProgress();
            load(view.getTransportMode(), view.getMaxTransfer(),
                    view.getWalkingReluctance(), view.getWalkingSpeed(),
                    view.getOrigin(), view.getDestination(),
                    false,
                    formatDate(view.getLatestStartTime()), formatTime(view.getLatestStartTime()),
                    RESULT_LATER);
        }
    }

    @Override
    public void rxUnsubscribe() {
        if (subscription != null && !subscription.isUnsubscribed()) {
            subscription.unsubscribe();
            subscription = null;
        }
    }

    @Override
    public void setView(GetRoutesMVP.View view) {
        this.view = view;
    }

    @Override
    public void close() {
        view = null;
        rxUnsubscribe();
        model.close();
    }

    private void load(String transportMode, int maxTransfer, double walkingReluctance, double walkingSpeed, InputCoordinates origin, InputCoordinates destination, boolean arriveBy, String date, String time, int mode) {
        Log.e(getClass().getName(), "load");
        view.clearList();
        subscription = model.result(
                transportMode, maxTransfer,
                walkingReluctance, walkingSpeed,
                origin, destination,
                arriveBy,
                date, time)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(getObserver(mode));
    }

    private Observer<RouteQuery.Itinerary> getObserver(final int mode) {
        return new Observer<RouteQuery.Itinerary>() {
            @Override
            public void onCompleted() {
                if (view != null) {
                    if (view.getListCount() == 0) {
                        view.hideContent();
                        view.hideProgress();
                        view.showNoResult();
                    }
                    switch (mode) {
                        case RESULT_LATER:
                            if (view != null) {
                                if (view.isArriveBy())
                                    view.reOrder();
                                view.updateTimestampAfterGetEarlierOrLater();
                            }
                            break;
                        case RESULT_EARLIER:
                            if (view != null) {
                                if (!view.isArriveBy())
                                    view.reOrder();
                                view.updateTimestampAfterGetEarlierOrLater();
                            }
                            break;
                        case RESULT_NOW:
                        default:
                            break;
                    }
                }
            }

            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
                if (view != null) {
                    view.hideProgress();
                    view.hideContent();
                    view.showError();
                }
            }

            @Override
            public void onNext(RouteQuery.Itinerary itinerary) {
                if (view != null) {
                    view.showContent();
                    view.hideProgress();
                    view.hideAnnouncement();
                    view.updateList(itinerary);
                }
            }
        };
    }

    private String formatDate(long timestamp) {
        return Utils.getQueryDateFormat().format(timestamp);
    }

    private String formatTime(long timestamp) {
        return Utils.getQueryTimeFormat().format(timestamp);
    }

}
