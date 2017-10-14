package leo.me.la.finroute.showMap;

import android.support.v4.util.LongSparseArray;

import java.lang.ref.WeakReference;

import leo.me.la.finroute.RouteQuery;

public class ItineraryHolder {
    private static ItineraryHolder holder = new ItineraryHolder();
    private LongSparseArray<WeakReference<RouteQuery.Itinerary>> itineraries = new LongSparseArray<>();

    private ItineraryHolder() {
    }

    public static ItineraryHolder getInstance() {
        return holder;
    }

    public void save(long id, RouteQuery.Itinerary itinerary) {
        itineraries.put(id, new WeakReference<>(itinerary));
    }

    public RouteQuery.Itinerary retrieve(long id) {
        if (itineraries.get(id) != null)
            return itineraries.get(id).get();
        return null;
    }
}
