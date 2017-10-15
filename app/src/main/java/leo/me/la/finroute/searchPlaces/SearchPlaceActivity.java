package leo.me.la.finroute.searchPlaces;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import io.realm.RealmResults;
import leo.me.la.finroute.R;
import leo.me.la.finroute.http.apiModel.Feature;
import leo.me.la.finroute.root.BaseActivity;

import static leo.me.la.finroute.getRoutes.GetRoutesActivity.TOOLBAR_TITLE;

public class SearchPlaceActivity extends BaseActivity implements SearchPlaceMVP.View,
        OnSuccessListener<LocationSettingsResponse>,
        OnFailureListener {
    public static final String FEATURE = "feature.la.me.leo";

    private static final int ACCESS_LOCATION_INTENT_ID = 3;
    private static final int REQUEST_CHECK_SETTINGS = 1;

    private final String TAG = SearchPlaceActivity.class.getName();
    @Inject
    SearchPlaceMVP.Presenter presenter;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.rcvPlace)
    RecyclerView rcvPlace;
    @BindView(R.id.etSearch)
    AppCompatEditText etSearch;
    @BindView(R.id.btnClear)
    ImageButton btnClear;
    @BindView(R.id.progress)
    ProgressBar progress;
    @BindView(R.id.tvResult)
    TextView tvResult;
    @BindView(R.id.tvAddress)
    TextView tvAddress;
    @BindView(R.id.myLocation)
    View myLocation;
    @BindView(R.id.annouce)
    View announce;
    @BindView(R.id.tvAnnounce)
    TextView tvAnnounce;
    @BindView(R.id.tvSubAnnouce)
    TextView tvSubAnnounce;
    @BindView(R.id.imgAnnounce)
    ImageView imgAnnounce;
    PlaceAdapter adapter;
    List<Feature> features;
    private Feature current;
    private FusedLocationProviderClient mFusedLocationClient;
    private SettingsClient mSettingsClient;
    private LocationCallback mLocationCallback;
    private LocationRequest mLocationRequest;
    private LocationSettingsRequest mLocationSettingsRequest;
    private Task<LocationSettingsResponse> task;

    @OnClick(R.id.tvSubAnnouce)
    void onRetry() {
        presenter.loadResult();
    }

    private void setupMyLocation() {
        tvAddress.setText(R.string.get_location);
        tvAddress.setTextColor(ContextCompat.getColor(this, R.color.secondary_text));
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mSettingsClient = LocationServices.getSettingsClient(this);

        createLocationCallback();
        createLocationRequest();
        buildLocationSettingsRequest();
    }

    private void setUpPlaceRecyclerView() {
        features = new ArrayList<>();
        adapter = new PlaceAdapter(features, this);
        adapter.setOnItemClickListener(new PlaceAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Feature feature) {
                presenter.saveRecent(feature);
                Intent returnIntent = getIntent();
                returnIntent.putExtra(FEATURE, feature);
                setResult(Activity.RESULT_OK, returnIntent);
                finish();
            }
        });
        rcvPlace.setAdapter(adapter);
        presenter.loadRecent();
    }

    private void setupToolbar(String title) {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle(title);
        }
    }

    private boolean checkPermissions() {
        return Build.VERSION.SDK_INT < 23 ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_place);
        ButterKnife.bind(this);
        DaggerSearchPlaceComponent.create().inject(this);
        presenter.setView(this);

        String title = getIntent().getStringExtra(TOOLBAR_TITLE);
        setupToolbar(title);
        setupMyLocation();
        setUpPlaceRecyclerView();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == ACCESS_LOCATION_INTENT_ID) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdates();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (task != null && task.isSuccessful()) {
            startLocationUpdates();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopLocationUpdates();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_place_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.remove:
                presenter.removeRecent();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        presenter.close();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    @Override
    public void showRecent() {
        tvResult.setText(R.string.recent_search);
        clearFeaturesList();
    }

    @Override
    public void showResult() {
        tvResult.setText(R.string.results);
        clearFeaturesList();
    }

    @Override
    public void updateResultPlaceItem(Feature feature) {
        features.add(feature);
        adapter.notifyItemInserted(features.size() - 1);
    }

    @Override
    public void updateRecentPlaceItems(RealmResults<leo.me.la.finroute.searchPlaces.realmModel.Feature> features) {
        clearFeaturesList();
        for (leo.me.la.finroute.searchPlaces.realmModel.Feature feature : features) {
            this.features.add(feature.convertToApiFeature());
            adapter.notifyItemInserted(this.features.size() - 1);
        }
    }

    private void clearFeaturesList() {
        int size = this.features.size();
        this.features.clear();
        adapter.notifyItemRangeRemoved(0, size);
    }

    @Override
    public void showResultPlaceError() {
        announce.setVisibility(View.VISIBLE);
        imgAnnounce.setImageResource(R.drawable.ic_error);
        tvAnnounce.setText(R.string.error_occurs);
        tvAnnounce.setTextColor(Color.parseColor("#ee2244"));
        tvSubAnnounce.setVisibility(View.VISIBLE);
        tvSubAnnounce.setText(R.string.tap_to_retry);
    }

    @Override
    public void showProgress() {
        progress.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideProgress() {
        progress.setVisibility(View.GONE);
    }

    @Override
    public void showContent() {
        hideAnnouncement();
        rcvPlace.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideContent() {
        rcvPlace.setVisibility(View.GONE);
    }

    @Override
    public void enableMyLocation() {
        myLocation.setClickable(true);
    }

    @Override
    public void disableMyLocation() {
        myLocation.setClickable(false);
    }

    @Override
    public void setCurrentLocation(Feature feature) {
        tvAddress.setText((feature == null)
                ? getString(R.string.unable_to_get_location)
                : feature.getProperties().getLabel());
        current = feature;
        tvAddress.setTextColor((feature == null)
                ? ContextCompat.getColor(this, R.color.dark_red)
                : ContextCompat.getColor(this, R.color.secondary_text));
    }

    @Override
    public String getQueryText() {
        return etSearch.getText().toString();
    }

    @Override
    public void showEmptyRecent() {
        announce.setVisibility(View.VISIBLE);
        imgAnnounce.setImageResource(R.drawable.ic_place);
        tvAnnounce.setText(R.string.empty_search_history);
        tvAnnounce.setTextColor(ContextCompat.getColor(this, R.color.accent));
        tvSubAnnounce.setVisibility(View.GONE);
    }

    @Override
    public void showEmptyResult() {
        announce.setVisibility(View.VISIBLE);
        imgAnnounce.setImageResource(R.drawable.ic_place);
        tvAnnounce.setText(R.string.no_result_found);
        tvAnnounce.setTextColor(ContextCompat.getColor(this, R.color.accent));
        tvSubAnnounce.setVisibility(View.GONE);
    }

    @Override
    public void hideAnnouncement() {
        announce.setVisibility(View.GONE);
    }

    @Override
    public int getPlacesListSize() {
        return features.size();
    }

    @OnClick(R.id.btnClear)
    void onClearText() {
        etSearch.getText().clear();
        etSearch.clearFocus();
    }

    @OnClick(R.id.myLocation)
    void onMyLocationClick() {
        if (current != null) {
            Intent returnIntent = getIntent();
            returnIntent.putExtra(FEATURE, current);
            setResult(Activity.RESULT_OK, returnIntent);
            finish();
            return;
        }
        if (!checkPermissions()) {
            requestLocationPermission();
            return;
        }
        startLocationUpdates();
    }

    @OnTextChanged(R.id.etSearch)
    void onSearchQueryChanged(CharSequence s, int start, int count, int after) {
        btnClear.setVisibility((s.length() == 0) ? View.INVISIBLE : View.VISIBLE);
        if (s.length() >= 3) {
            presenter.loadResult();
        }
        if (s.length() == 0) {
            showRecent();
            presenter.loadRecent();
        }
    }

    private void requestLocationPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Show an explanation to the user *asynchronously* -- don't block
            // this thread waiting for the user's response! After the user
            // sees the explanation, try again to request the permission.
            new AlertDialog.Builder(this)
                    .setMessage(R.string.request_sms)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(SearchPlaceActivity.this,
                                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                    ACCESS_LOCATION_INTENT_ID);
                        }
                    }).setNegativeButton(android.R.string.cancel, null)
                    .show();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    ACCESS_LOCATION_INTENT_ID);
        }
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(2000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @Override
    public void onFailure(@NonNull Exception e) {
        enableMyLocation();
        int statusCode = ((ApiException) e).getStatusCode();
        switch (statusCode) {
            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                try {
                    ResolvableApiException rae = (ResolvableApiException) e;
                    rae.startResolutionForResult(this, REQUEST_CHECK_SETTINGS);
                } catch (IntentSender.SendIntentException ignored) {
                }
                break;
            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                break;
        }
    }

    @Override
    public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
        enableMyLocation();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                mLocationCallback, Looper.myLooper());
        tvAddress.setText(R.string.fetch_location);
        tvAddress.setTextColor(ContextCompat.getColor(this, R.color.secondary_text));
    }

    private void createLocationCallback() {
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);

                Location location = locationResult.getLastLocation();
                presenter.loadCurrent(location.getLatitude(), location.getLongitude());
            }
        };
    }

    private void buildLocationSettingsRequest() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        mLocationSettingsRequest = builder.build();
    }

    private void startLocationUpdates() {
        // Begin by checking if the device has the necessary location settings.
        task = mSettingsClient.checkLocationSettings(mLocationSettingsRequest)
                .addOnSuccessListener(this, this)
                .addOnFailureListener(this, this);
        task.isSuccessful();
        disableMyLocation();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            // Check for the integer request code originally supplied to startResolutionForResult().
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        startLocationUpdates();
                        break;
                    case Activity.RESULT_CANCELED:
                        break;
                }
                break;
        }
    }

    private void stopLocationUpdates() {
        // It is a good practice to remove location requests when the activity is in a paused or
        // stopped state. Doing so helps battery performance and is especially
        // recommended in applications that request frequent location updates.
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
    }
}
