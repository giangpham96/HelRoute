package leo.me.la.finroute.http.apiModel;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

import io.realm.RealmList;
import leo.me.la.finroute.searchPlaces.realmModel.RealmDouble;

public class Geometry implements Serializable {

    @SerializedName("coordinates")
    @Expose
    private List<Double> coordinates = null;

    public Geometry(List<Double> coordinates) {
        this.coordinates = coordinates;
    }

    public Geometry() {
    }

    public List<Double> getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(List<Double> coordinates) {
        this.coordinates = coordinates;
    }

    leo.me.la.finroute.searchPlaces.realmModel.Geometry convertToRealmGeoMetry() {
        RealmList<RealmDouble> coordinates = new RealmList<>();
        for (Double d : this.coordinates) {
            coordinates.add(new RealmDouble(d));
        }
        return new leo.me.la.finroute.searchPlaces.realmModel.Geometry(coordinates);
    }
}
