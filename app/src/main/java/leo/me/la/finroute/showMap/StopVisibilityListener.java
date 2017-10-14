package leo.me.la.finroute.showMap;

interface StopVisibilityListener {
    void onVisibilityChange(int position, boolean isShown);

    boolean currentVisibility(int position);

    void update(int position);

    StopAdapter getChildAdapter(int position);

    void setChildAdapter(int position, StopAdapter adapter);
}
