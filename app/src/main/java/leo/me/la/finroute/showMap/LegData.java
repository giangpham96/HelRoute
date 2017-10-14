package leo.me.la.finroute.showMap;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

import leo.me.la.finroute.type.Mode;


class LegData {
    private List<LatLng> latLngs;
    private Mode mode;
    private String displayText;

    LegData(List<LatLng> latLngs, Mode mode, String displayText) {
        this.latLngs = latLngs;
        this.mode = mode;
        this.displayText = displayText;
    }

    LatLng getLastPoint() {
        if (latLngs.size() > 0)
            return latLngs.get(latLngs.size() - 1);
        return null;
    }

    LatLng getMidPoint() {
        if (latLngs.size() > 0)
            return latLngs.get(latLngs.size() / 2);
        return null;
    }

    List<LatLng> getLatLngs() {
        return latLngs;
    }

    public Mode getMode() {
        return mode;
    }

    String getDisplayText() {
        return displayText;
    }
}
