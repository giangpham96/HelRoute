package leo.me.la.finroute.getRoutes;

import leo.me.la.finroute.RouteQuery;
import leo.me.la.finroute.type.InputCoordinates;

interface GetRoutesMVP {
    interface Model {
        rx.Observable<RouteQuery.Itinerary> result(
                String transportMode,
                int maxTrans,
                double walkingReluctance,
                double walkingSpeed,
                InputCoordinates from,
                InputCoordinates to,
                boolean isArriveBy,
                String date,
                String time
        );

        void close();
    }

    interface View {
        String getTransportMode();

        int getMaxTransfer();

        double getWalkingReluctance();

        double getWalkingSpeed();

        InputCoordinates getOrigin();

        InputCoordinates getDestination();

        boolean isArriveBy();

        long getTimestamp();

        void updateTimestampAfterGetEarlierOrLater();

        void updateList(RouteQuery.Itinerary itinerary);

        void clearList();

        int getListCount();

        void showError();

        void showNoResult();

        void hideAnnouncement();

        void showProgress();

        void hideProgress();

        void hideContent();

        void showContent();

        void reOrder();

        Long getEarliestEndTime();

        Long getLatestStartTime();
    }

    interface Presenter {
        void loadResult();

        void loadEarlier();

        void loadLater();

        void rxUnsubscribe();

        void setView(View view);

        void close();
    }
}
