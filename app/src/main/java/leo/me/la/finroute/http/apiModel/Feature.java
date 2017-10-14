package leo.me.la.finroute.http.apiModel;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Feature implements Serializable {

    @SerializedName("geometry")
    @Expose
    private Geometry geometry;
    @SerializedName("properties")
    @Expose
    private Properties properties;

    public Feature(Geometry geometry, Properties properties) {
        this.geometry = geometry;
        this.properties = properties;
    }

    public Feature() {
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

    public leo.me.la.finroute.searchPlaces.realmModel.Feature convertToRealmFeature() {
        return new leo.me.la.finroute.searchPlaces.realmModel.Feature(
                geometry.convertToRealmGeoMetry(),
                properties.convertToRealmProperties(),
                System.currentTimeMillis()
        );
    }
}
