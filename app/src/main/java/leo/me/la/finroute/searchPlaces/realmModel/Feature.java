package leo.me.la.finroute.searchPlaces.realmModel;

import io.realm.RealmObject;
import io.realm.annotations.Index;

public class Feature extends RealmObject {

    private Geometry geometry;

    private Properties properties;
    @Index
    private long timestamp;

    public Feature(Geometry geometry, Properties properties, long timestamp) {
        this.geometry = geometry;
        this.properties = properties;
        this.timestamp = timestamp;
    }

    public Feature() {
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public Geometry getGeometry() {
        return geometry;
    }

    public void setGeometry(Geometry geometry) {
        this.geometry = geometry;
    }

    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    public leo.me.la.finroute.http.apiModel.Feature convertToApiFeature() {
        return new leo.me.la.finroute.http.apiModel.Feature(
                geometry.convertToApiGeoMetry(),
                properties.convertToApiProperties()
        );
    }
}
