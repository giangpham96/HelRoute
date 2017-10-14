package leo.me.la.finroute.showMap;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import leo.me.la.finroute.R;
import leo.me.la.finroute.RouteQuery;
import leo.me.la.finroute.root.Utils;
import leo.me.la.finroute.type.Mode;

import static android.support.design.widget.BottomSheetBehavior.STATE_COLLAPSED;
import static android.support.design.widget.BottomSheetBehavior.STATE_EXPANDED;
import static leo.me.la.finroute.type.Mode.WALK;

public class MapActivity extends AppCompatActivity
        implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {
    public static final String ITINERARY_ID = "la.me.leo.itinerary.id";
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    @BindView(R.id.bottomSheetContainer)
    View bottomSheetContainer;
    @BindView(R.id.tvTotalTime)
    TextView tvTotalTime;
    @BindView(R.id.tvPrice)
    TextView tvPrice;
    @BindView(R.id.rcvLeg)
    RecyclerView rcvLeg;
    LegDetailAdapter adapter;
    private RouteQuery.Itinerary itinerary;
    private BottomSheetBehavior bottomSheetBehavior;
    private List<LegData> legDataList;
    private List<Circle> circleList;
    private GoogleMap mGoogleMap;
    private GoogleApiClient mGoogleApiClient;

    @OnClick(R.id.bottomSheetContainer)
    void onBottomSheetClick() {
        if (bottomSheetBehavior != null && bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
            bottomSheetBehavior.setState(STATE_EXPANDED);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        long id = getIntent().getLongExtra(ITINERARY_ID, 0);
        itinerary = ItineraryHolder.getInstance().retrieve(id);
        if (itinerary == null)
            finish();
        legDataList = new ArrayList<>();
        circleList = new ArrayList<>();
        ButterKnife.bind(this);
        initData();
        initMap();
        initBottomSheet();
    }

    private void initData() {
        for (RouteQuery.Leg leg : itinerary.legs()) {
            if (leg.legGeometry() != null && leg.legGeometry().points() != null && leg.mode() != null) {
                LatLng start;
                String displayText = "";
                if (legDataList.size() > 0)
                    start = legDataList.get(legDataList.size() - 1).getLastPoint();
                else
                    start = null;
                if (!(leg.mode() == WALK) && !(leg.mode() == Mode.BICYCLE)) {
                    if (leg.route() != null && leg.route().shortName() != null)
                        displayText = leg.route().shortName();
                }
                legDataList.add(new LegData(decodePoly(start, leg), leg.mode(), displayText));
            }
        }
    }

    private void initMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private void initBottomSheet() {
        String durationText = (itinerary.duration() != null)
                ? Utils.getReadableDuration(this, itinerary.duration())
                : "";
        tvTotalTime.setText(durationText);
        if (itinerary.fares() != null && !itinerary.fares().isEmpty())
            tvPrice.setText((float) itinerary.fares().get(0).cents() / 100 + " " + getString(R.string.euro));
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetContainer);
        tvTotalTime.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                    tvTotalTime.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                } else {
                    tvTotalTime.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
                int height = tvTotalTime.getHeight();
                int margin = MapActivity.this.getResources().getDimensionPixelSize(R.dimen.mp_normal);
                bottomSheetBehavior.setPeekHeight(height + margin * 2);
            }
        });
        adapter = new LegDetailAdapter(this, itinerary.legs());
        rcvLeg.setAdapter(adapter);
        rcvLeg.getLayoutManager().setItemPrefetchEnabled(true);
        ((LinearLayoutManager) rcvLeg.getLayoutManager()).setInitialPrefetchItemCount(5);
        rcvLeg.setHasFixedSize(true);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (bottomSheetBehavior.getState() == STATE_EXPANDED) {

                Rect outRect = new Rect();
                bottomSheetContainer.getGlobalVisibleRect(outRect);

                if (!outRect.contains((int) event.getRawX(), (int) event.getRawY()))
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
        }

        return super.dispatchTouchEvent(event);
    }

    @Override
    public void onBackPressed() {
        if (bottomSheetBehavior.getState() == STATE_EXPANDED)
            bottomSheetBehavior.setState(STATE_COLLAPSED);
        else
            super.onBackPressed();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {
                        if (mGoogleApiClient == null) {
                            buildGoogleApiClient();
                        }
                        mGoogleMap.setMyLocationEnabled(true);
                    }

                }
                break;
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                //Location Permission already granted
                buildGoogleApiClient();
                mGoogleMap.setMyLocationEnabled(true);
            } else {
                //Request Location Permission
                checkLocationPermission();
            }
        } else {
            buildGoogleApiClient();
            mGoogleMap.setMyLocationEnabled(true);
        }

        if (legDataList.size() > 0) {
            addOriginMarker(googleMap);
            addDestinationMarker(googleMap);
        }
        LatLngBounds.Builder boundBuilder = LatLngBounds.builder();
        for (final LegData data : legDataList) {
            for (LatLng latLng : data.getLatLngs()) {
                boundBuilder.include(latLng);
            }
            int color = ContextCompat.getColor(MapActivity.this, Utils.getModeColorId(data.getMode()));
            googleMap.addPolyline(getPolylineOptions(data.getLatLngs(), color));
            if (data.getMode() != WALK && data.getLatLngs().size() > 0) {
                Circle start = googleMap.addCircle(getCircleOptions(data.getLatLngs().get(0), color));
                Circle end = googleMap.addCircle(getCircleOptions(data.getLastPoint(), color));
                circleList.add(start);
                circleList.add(end);
            }
            if (data.getMode() != WALK) {
                googleMap.addMarker(new MarkerOptions()
                        .position(data.getMidPoint())
                        .zIndex(2f)
                        .icon(BitmapDescriptorFactory.fromBitmap(getMarkerBitmapFromView(data))));
            }
        }
        googleMap.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
            @Override
            public void onCameraMove() {
                updateCirclesRadius(mGoogleMap.getCameraPosition().zoom);
            }
        });
        googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(boundBuilder.build(),
                Utils.getScreenWidth(MapActivity.this),
                Utils.getScreenHeight(MapActivity.this),
                30));
        updateCirclesRadius(googleMap.getCameraPosition().zoom);
        googleMap.setMinZoomPreference(googleMap.getCameraPosition().zoom - 0.5f);
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    private CircleOptions getCircleOptions(LatLng latLng, int color) {
        return new CircleOptions()
                .center(latLng)
                .fillColor(Color.WHITE)
                .strokeWidth(3f)
                .strokeColor(color)
                .zIndex(3f);
    }

    private PolylineOptions getPolylineOptions(List<LatLng> latLngs, int color) {
        return new PolylineOptions()
                .addAll(latLngs)
                .color(color)
                .width(15f)
                .geodesic(true);
    }

    private void addDestinationMarker(GoogleMap googleMap) {
        LatLng destination = legDataList.get(legDataList.size() - 1).getLastPoint();
        if (destination != null)
            googleMap.addMarker(new MarkerOptions().position(destination)
                    .icon(BitmapDescriptorFactory.fromBitmap(
                            Utils.getBitmapFromVectorDrawable(
                                    MapActivity.this,
                                    R.drawable.ic_destination_mark
                            )
                    ))
                    .title("Destination"));
    }

    private void addOriginMarker(GoogleMap googleMap) {
        if (legDataList.get(0).getLatLngs().size() > 0) {
            LatLng origin = legDataList.get(0).getLatLngs().get(0);
            googleMap.addMarker(new MarkerOptions().position(origin)
                    .icon(BitmapDescriptorFactory.fromBitmap(
                            Utils.getBitmapFromVectorDrawable(
                                    MapActivity.this,
                                    R.drawable.ic_origin_mark
                            )
                    ))
                    .title("Origin"));
        }
    }

    private void updateCirclesRadius(float zoomLevel) {
        for (Circle circle : circleList) {
            circle.setRadius(
                    calculateCircleRadiusMeterForMapCircle(
                            8, circle.getCenter().latitude,
                            zoomLevel
                    )
            );
        }
    }

    private List<LatLng> decodePoly(LatLng start, RouteQuery.Leg leg) {
        int index = 0, len = leg.legGeometry().points().length();
        int lat = 0, lng = 0;

        List<LatLng> latLngs = new ArrayList<>();
        if (start != null)
            latLngs.add(start);
        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = leg.legGeometry().points().charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;
            shift = 0;
            result = 0;
            do {
                b = leg.legGeometry().points().charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((double) lat / 1E5, (double) lng / 1E5);
            latLngs.add(p);
        }
        return latLngs;
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_LOCATION);
        }
    }

    private double calculateCircleRadiusMeterForMapCircle(final int _targetRadiusDip
            , final double _circleCenterLatitude, final float _currentMapZoom) {
        //That base value seems to work for computing the meter length of a DIP
        final double arbitraryValueForDip = 156000D;
        final double oneDipDistance = Math.abs(Math.cos(Math.toRadians(_circleCenterLatitude)))
                * arbitraryValueForDip / Math.pow(2, _currentMapZoom);
        return oneDipDistance * (double) _targetRadiusDip;
    }

    private Bitmap getMarkerBitmapFromView(LegData legData) {
        int color = ContextCompat.getColor(MapActivity.this, Utils.getModeColorId(legData.getMode()));
        int iconDrawable = Utils.getModeDrawableId(legData.getMode());
        View customMarkerView = getLayoutInflater().inflate(R.layout.marker_leg, null);
        TextView tvCode = customMarkerView.findViewById(R.id.tvCode);
        tvCode.setText(legData.getDisplayText());
        tvCode.setTextColor(color);
        tvCode.setCompoundDrawablesWithIntrinsicBounds(0, iconDrawable, 0, 0);
        customMarkerView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        customMarkerView.layout(0, 0, customMarkerView.getMeasuredWidth(), customMarkerView.getMeasuredHeight());
        customMarkerView.buildDrawingCache();
        Bitmap returnedBitmap = Bitmap.createBitmap(customMarkerView.getMeasuredWidth(), customMarkerView.getMeasuredHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(returnedBitmap);
        canvas.drawColor(Color.WHITE, PorterDuff.Mode.SRC_IN);
        Drawable drawable = customMarkerView.getBackground();
        if (drawable != null)
            drawable.draw(canvas);
        customMarkerView.draw(canvas);
        return returnedBitmap;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
