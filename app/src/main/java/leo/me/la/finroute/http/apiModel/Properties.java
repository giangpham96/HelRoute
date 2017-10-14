package leo.me.la.finroute.http.apiModel;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Properties implements Serializable {

    @SerializedName("id")
    @Expose
    private String id;
    @SerializedName("layer")
    @Expose
    private String layer;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("postalcode")
    @Expose
    private String postalcode;
    @SerializedName("country")
    @Expose
    private String country;
    @SerializedName("region")
    @Expose
    private String region;
    @SerializedName("localadmin")
    @Expose
    private String localadmin;
    @SerializedName("locality")
    @Expose
    private String locality;
    @SerializedName("label")
    @Expose
    private String label;

    public Properties(String id, String layer, String name, String postalcode, String country, String region, String localadmin, String locality, String label) {
        this.id = id;
        this.layer = layer;
        this.name = name;
        this.postalcode = postalcode;
        this.country = country;
        this.region = region;
        this.localadmin = localadmin;
        this.locality = locality;
        this.label = label;
    }

    public Properties() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLayer() {
        return layer;
    }

    public void setLayer(String layer) {
        this.layer = layer;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPostalcode() {
        return postalcode;
    }

    public void setPostalcode(String postalcode) {
        this.postalcode = postalcode;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getLocaladmin() {
        return localadmin;
    }

    public void setLocaladmin(String localadmin) {
        this.localadmin = localadmin;
    }

    public String getLocality() {
        return locality;
    }

    public void setLocality(String locality) {
        this.locality = locality;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    leo.me.la.finroute.searchPlaces.realmModel.Properties convertToRealmProperties() {
        return new leo.me.la.finroute.searchPlaces.realmModel.Properties(id, layer, name, postalcode, country, region, localadmin, locality, label);
    }
}
