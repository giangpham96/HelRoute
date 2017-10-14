package leo.me.la.finroute.searchPlaces.realmModel;

import java.util.ArrayList;

import io.realm.RealmList;
import io.realm.RealmObject;

public class Geometry extends RealmObject {

    private RealmList<RealmDouble> coordinates;

    public Geometry() {
    }

    public Geometry(RealmList<RealmDouble> coordinates) {
        this.coordinates = coordinates;
    }

    public RealmList<RealmDouble> getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(RealmList<RealmDouble> coordinates) {
        this.coordinates = coordinates;
    }

    leo.me.la.finroute.http.apiModel.Geometry convertToApiGeoMetry() {
        ArrayList<Double> coordinates = new ArrayList<>();
        for (RealmDouble value : this.coordinates) {
            coordinates.add(value.getValue());
        }
        return new leo.me.la.finroute.http.apiModel.Geometry(coordinates);
    }
}
