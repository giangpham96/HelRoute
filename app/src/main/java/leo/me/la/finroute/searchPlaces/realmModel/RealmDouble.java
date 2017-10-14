package leo.me.la.finroute.searchPlaces.realmModel;

import io.realm.RealmObject;

public class RealmDouble extends RealmObject {
    private Double value;

    public RealmDouble(Double value) {
        this.value = value;
    }

    public RealmDouble() {
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }
}
