package leo.me.la.finroute.getRoutes;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.TimePicker;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import leo.me.la.finroute.R;
import leo.me.la.finroute.RouteQuery;
import leo.me.la.finroute.customViews.CustomSeekBar;
import leo.me.la.finroute.http.apiModel.Feature;
import leo.me.la.finroute.root.BaseActivity;
import leo.me.la.finroute.root.SharedPrefManager;
import leo.me.la.finroute.root.Utils;
import leo.me.la.finroute.searchPlaces.SearchPlaceActivity;
import leo.me.la.finroute.showAlert.AlertActivity;
import leo.me.la.finroute.type.InputCoordinates;

import static leo.me.la.finroute.searchPlaces.SearchPlaceActivity.FEATURE;

public class GetRoutesActivity extends BaseActivity implements GetRoutesMVP.View {
    public static final int REQUEST_ORIGIN = 0;
    public static final int REQUEST_DESTINATION = 1;
    public static final long CACHE_STALE_TIME = 180000;
    public static final String TOOLBAR_TITLE = "toolbar.title.la.me.leo";
    IntentFilter timeIntentFilter;
    @Inject
    GetRoutesMVP.Presenter presenter;
    @BindView(R.id.drawerLayout)
    DrawerLayout drawerLayout;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.rcvMode)
    RecyclerView rcvMode;
    @BindView(R.id.sbMaxTrans)
    LinearLayout sbMaxTrans;
    @BindView(R.id.sbWalkReluctance)
    LinearLayout sbWalkReluctance;
    @BindView(R.id.sbWalkSpeed)
    LinearLayout sbWalkSpeed;
    @BindView(R.id.tvLeaving)
    TextView tvLeaving;
    @BindView(R.id.tvArriving)
    TextView tvArriving;
    @BindView(R.id.tvTime)
    TextView tvTime;
    @BindView(R.id.tvOrigin)
    TextView tvOrigin;
    @BindView(R.id.tvDestination)
    TextView tvDestination;
    @BindView(R.id.tvDate)
    TextView tvDate;
    @BindView(R.id.options)
    LinearLayout options;
    @BindView(R.id.btnReverse)
    ImageButton btnReverse;
    @BindView(R.id.rcvResult)
    RecyclerView rcvResult;
    @BindView(R.id.progress)
    ProgressBar progress;
    @BindView(R.id.annouce)
    View annouce;
    @BindView(R.id.imgAnnounce)
    ImageView imgAnnounce;
    @BindView(R.id.tvAnnounce)
    TextView tvAnnounce;
    @BindView(R.id.tvSubAnnouce)
    TextView tvSubAnnouce;
    DatePickerDialog datePickerDialog;
    TimePickerDialog timePickerDialog;
    Calendar calendar;
    private final BroadcastReceiver timeChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (action.equals(Intent.ACTION_DATE_CHANGED)) {
                displayDate();
            }
        }
    };
    Feature origin, destination;
    private TransportModeAdapter transportModeAdapter;
    private CustomSeekBar csbMaxTrans;
    private CustomSeekBar csbWalkReluctance;
    private CustomSeekBar csbWalkSpeed;
    private boolean isArriveBy = false;
    private List<RouteQuery.Itinerary> itineraries;
    private RoutesAdapter routesAdapter;

    @OnClick(R.id.btnAlert)
    void onFabClick() {
        Intent intent = new Intent(this, AlertActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.tvSubAnnouce)
    void onRetry() {
        presenter.loadResult();
    }

    @OnClick(R.id.btnReverse)
    public void onReverseClick() {
        Feature temp = origin;
        ScaleAnimation animation = new ScaleAnimation(1, 0, 1, 0, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        animation.setRepeatMode(ValueAnimator.REVERSE);
        animation.setRepeatCount(1);
        animation.setDuration(100);
        RotateAnimation rotateAnimation = new RotateAnimation(0, 180, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        rotateAnimation.setDuration(100);
        tvOrigin.startAnimation(animation);
        tvDestination.startAnimation(animation);
        btnReverse.startAnimation(rotateAnimation);
        origin = destination;
        destination = temp;
        tvOrigin.setText((origin == null) ? getString(R.string.choose_origin) : origin.getProperties().getLabel());
        tvDestination.setText((destination == null) ? getString(R.string.choose_destination) : destination.getProperties().getLabel());
        presenter.loadResult();
    }

    @OnClick(R.id.btnSaveSettings)
    public void onSaveClick() {
        SharedPrefManager.writeMaxTransfer(csbMaxTrans.getProgress());
        SharedPrefManager.writeWalkingSpeed(csbWalkSpeed.getProgress());
        SharedPrefManager.writeWalkReluctance(csbWalkReluctance.getProgress());
        transportModeAdapter.saveModes();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DaggerGetRoutesComponent.create().inject(this);
        presenter.setView(this);
        setContentView(R.layout.activity_get_routes);
        ButterKnife.bind(this);
        setupBroadcastReceiver();
        initViews();
        showLastUsedPlan();
    }

    private void showLastUsedPlan() {
        long lastUsed = SharedPrefManager.readLastUsedTime();
        if (System.currentTimeMillis() - lastUsed > CACHE_STALE_TIME) {
            return;
        }
        long lastQueryTime = SharedPrefManager.readLastQueryTime();
        calendar.setTimeInMillis(lastQueryTime);
        displayTime();
        displayDate();
        readOrigin();
        readDestination();
        isArriveBy = SharedPrefManager.readLastQueryArriveMode();
        TextView selected, unselected;
        selected = isArriveBy ? tvArriving : tvLeaving;
        unselected = isArriveBy ? tvLeaving : tvArriving;
        Drawable background = ContextCompat.getDrawable(GetRoutesActivity.this, R.drawable.bg_selected);
        int selectedColor = ContextCompat.getColor(GetRoutesActivity.this, R.color.accent);
        int unselectedColor = ContextCompat.getColor(GetRoutesActivity.this, R.color.light_white);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            selected.setBackground(background);
            unselected.setBackground(null);
        } else {
            selected.setBackgroundDrawable(background);
            unselected.setBackgroundDrawable(null);
        }
        selected.setTextColor(selectedColor);
        unselected.setTextColor(unselectedColor);
        tvOrigin.setText((origin == null) ? getString(R.string.choose_origin) : origin.getProperties().getLabel());
        tvDestination.setText((destination == null) ? getString(R.string.choose_destination) : destination.getProperties().getLabel());
        presenter.loadResult();
    }

    @Override
    protected void onDestroy() {
        if (origin != null && destination != null) {
            SharedPrefManager.writeLastUsedTime();
            SharedPrefManager.writeLastQueryTime(calendar.getTimeInMillis());
            SharedPrefManager.writeLastArriveMode(isArriveBy);
            saveOrigin();
            saveDestination();
        }
        presenter.close();
        unregisterReceiver(timeChangeReceiver);
        super.onDestroy();
    }

    private void setupBroadcastReceiver() {
        timeIntentFilter = new IntentFilter();
        timeIntentFilter.addAction(Intent.ACTION_DATE_CHANGED);
        registerReceiver(timeChangeReceiver, timeIntentFilter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ORIGIN && resultCode == Activity.RESULT_OK) {
            origin = (Feature) data.getSerializableExtra(FEATURE);
            tvOrigin.setText(origin.getProperties().getLabel());
            presenter.loadResult();
        }
        if (requestCode == REQUEST_DESTINATION && resultCode == Activity.RESULT_OK) {
            destination = (Feature) data.getSerializableExtra(FEATURE);
            tvDestination.setText(destination.getProperties().getLabel());
            presenter.loadResult();
        }
    }

    private void initViews() {
        setupToolbar();
        setUpNavigation();
        setupDrawerToggle();
        setUpDateTimePicker();
        setUpContent();
    }

    private void setUpContent() {
        itineraries = new ArrayList<>();
        routesAdapter = new RoutesAdapter(this, itineraries);
        rcvResult.setAdapter(routesAdapter);
    }

    private void setUpDateTimePicker() {
        //tab that determines user want to depart at or arrive by specific time
        tvArriving.setOnClickListener(getTimeModeClick(tvArriving, tvLeaving));
        tvLeaving.setOnClickListener(getTimeModeClick(tvLeaving, tvArriving));
        //calendar instance for retrieving that specific time
        calendar = Calendar.getInstance(TimeZone.getTimeZone("Europe/Helsinki"));
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        //set up date picker
        DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int monthOfYear, int dayOfMonth) {
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, monthOfYear);
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                displayDate();
                presenter.loadResult();
            }
        };
        datePickerDialog = new DatePickerDialog(this,
                dateSetListener,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 10000);
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis() + 864000000L);
        //set up time picker
        TimePickerDialog.OnTimeSetListener timeSetListener = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int hour, int minute) {
                calendar.set(Calendar.HOUR_OF_DAY, hour);
                calendar.set(Calendar.MINUTE, minute);
                displayTime();
                presenter.loadResult();
            }
        };
        timePickerDialog = new TimePickerDialog(this,
                timeSetListener,
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true);
        //text views to display date and time
        tvDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                datePickerDialog.show();
            }
        });
        tvTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timePickerDialog.show();
            }
        });
        displayDate();
        displayTime();
    }

    private void setUpNavigation() {
        transportModeAdapter = new TransportModeAdapter(this);
        rcvMode.setAdapter(transportModeAdapter);
        //seek bar for maximum transfer
        csbMaxTrans = new CustomSeekBar(this, 5, Color.WHITE, 0,
                new String[]{"Avoid transfer", "Transfer allowed"});
        csbMaxTrans.addSeekBar(sbMaxTrans);
        csbMaxTrans.setProgress(SharedPrefManager.readMaxTransfer());

        //seek bar for walking reluctance
        csbWalkReluctance = new CustomSeekBar(this, 60, Color.WHITE, 0,
                new String[]{"Prefer walking", "Avoid walking"});
        csbWalkReluctance.addSeekBar(sbWalkReluctance);
        csbWalkReluctance.setProgress(SharedPrefManager.readWalkReluctance());

        //seek bar for walking speed
        csbWalkSpeed = new CustomSeekBar(this, 300, Color.WHITE, 50,
                new String[]{"Slow", "Run"});
        csbWalkSpeed.addSeekBar(sbWalkSpeed);
        csbWalkSpeed.setProgress(SharedPrefManager.readWalkingSpeed());
    }

    void setupToolbar() {
        setSupportActionBar(toolbar);
        tvOrigin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startSearchPlaceActivity(getString(R.string.from), REQUEST_ORIGIN);
            }
        });
        tvDestination.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startSearchPlaceActivity(getString(R.string.to), REQUEST_DESTINATION);
            }
        });
    }

    private void startSearchPlaceActivity(String toolbarTitle, int requestCode) {
        Intent intent = new Intent(GetRoutesActivity.this, SearchPlaceActivity.class);
        intent.putExtra(TOOLBAR_TITLE, toolbarTitle);
        startActivityForResult(intent, requestCode);
    }


    void setupDrawerToggle() {
        ActionBarDrawerToggle drawerToggle =
                new android.support.v7.app.ActionBarDrawerToggle(this,
                        drawerLayout, toolbar,
                        R.string.app_name, R.string.app_name) {
                    private String tempModes;
                    private int tempMaxTransfer;
                    private double tempWalkReluctance, tempWalkingSpeed;

                    @Override
                    public void onDrawerOpened(View drawerView) {
                        super.onDrawerOpened(drawerView);
                        tempModes = getTransportMode();
                        tempMaxTransfer = getMaxTransfer();
                        tempWalkReluctance = getWalkingReluctance();
                        tempWalkingSpeed = getWalkingSpeed();
                    }

                    @Override
                    public void onDrawerClosed(View drawerView) {
                        super.onDrawerClosed(drawerView);
                        boolean reload = !tempModes.equals(getTransportMode())
                                || tempMaxTransfer != getMaxTransfer()
                                || tempWalkingSpeed != getWalkingSpeed()
                                || tempWalkReluctance != getWalkingReluctance();
                        if (reload)
                            presenter.loadResult();
                    }
                };
        //This is necessary to change the icon of the Drawer Toggle upon state change.
        drawerToggle.setDrawerIndicatorEnabled(false);
        drawerToggle.syncState();
        drawerToggle.setToolbarNavigationClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });
        drawerToggle.setHomeAsUpIndicator(R.drawable.ic_controls);
        drawerLayout.addDrawerListener(drawerToggle);
    }

    private void displayTime() {
        tvTime.setText(Utils.getShortTimeFormat().format(new Date(calendar.getTimeInMillis())));
    }

    private void displayDate() {
        if (Utils.isToday(calendar.getTimeInMillis())) {
            tvDate.setText(R.string.today);
        } else if (Utils.isTomorrow(calendar.getTimeInMillis())) {
            tvDate.setText(R.string.tomorrow);
        } else {
            tvDate.setText(Utils.getShortDateFormat().format(new Date(calendar.getTimeInMillis())));
        }
    }

    private View.OnClickListener getTimeModeClick(final TextView selected, final TextView unselected) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Drawable background = ContextCompat.getDrawable(GetRoutesActivity.this, R.drawable.bg_selected);
                int selectedColor = ContextCompat.getColor(GetRoutesActivity.this, R.color.accent);
                int unselectedColor = ContextCompat.getColor(GetRoutesActivity.this, R.color.light_white);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    selected.setBackground(background);
                    unselected.setBackground(null);
                } else {
                    selected.setBackgroundDrawable(background);
                    unselected.setBackgroundDrawable(null);
                }
                selected.setTextColor(selectedColor);
                unselected.setTextColor(unselectedColor);
                isArriveBy = !isArriveBy;
                presenter.loadResult();
            }
        };
    }

    @Override
    public String getTransportMode() {
        return transportModeAdapter.getModes();
    }

    @Override
    public int getMaxTransfer() {
        return csbMaxTrans.getProgress();
    }

    @Override
    public double getWalkingReluctance() {
        return ((double) csbWalkReluctance.getProgress()) / 10;
    }

    @Override
    public double getWalkingSpeed() {
        return ((double) csbWalkSpeed.getProgress()) / 100;
    }

    @Override
    public InputCoordinates getOrigin() {
        if (origin == null) {
            return null;
        }
        return InputCoordinates.builder()
                .lat(origin.getGeometry().getCoordinates().get(1))
                .lon(origin.getGeometry().getCoordinates().get(0))
                .build();
    }

    @Override
    public InputCoordinates getDestination() {
        if (destination == null) {
            return null;
        }
        return InputCoordinates.builder()
                .lat(destination.getGeometry().getCoordinates().get(1))
                .lon(destination.getGeometry().getCoordinates().get(0))
                .build();
    }

    @Override
    public boolean isArriveBy() {
        return isArriveBy;
    }

    @Override
    public long getTimestamp() {
        return calendar.getTimeInMillis();
    }

    @Override
    public void updateTimestampAfterGetEarlierOrLater() {
        if (itineraries.isEmpty()) {
            return;
        }
        long timestamp = isArriveBy
                ? itineraries.get(0).endTime()
                : itineraries.get(0).startTime();
        calendar.setTimeInMillis(timestamp);
        displayTime();
        displayDate();
    }

    @Override
    public void updateList(RouteQuery.Itinerary itinerary) {
        itineraries.add(itinerary);
        routesAdapter.notifyItemInserted(itineraries.size() - 1);
    }

    @Override
    public void clearList() {
        itineraries.clear();
    }

    @Override
    public int getListCount() {
        return itineraries.size();
    }

    @Override
    public void showError() {
        annouce.setVisibility(View.VISIBLE);
        imgAnnounce.setImageResource(R.drawable.ic_error);
        tvAnnounce.setText(R.string.error_occurs);
        tvAnnounce.setTextColor(Color.parseColor("#ee2244"));
        tvSubAnnouce.setVisibility(View.VISIBLE);
        tvSubAnnouce.setText(R.string.tap_to_retry);
    }

    @Override
    public void showNoResult() {
        annouce.setVisibility(View.VISIBLE);
        imgAnnounce.setImageResource(R.drawable.ic_place);
        tvAnnounce.setText(R.string.no_result_found);
        tvAnnounce.setTextColor(ContextCompat.getColor(this, R.color.accent));
        tvSubAnnouce.setVisibility(View.GONE);
    }

    @Override
    public void hideAnnouncement() {
        annouce.setVisibility(View.GONE);
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
    public void hideContent() {
        rcvResult.setVisibility(View.GONE);
    }

    @Override
    public void showContent() {
        rcvResult.setVisibility(View.VISIBLE);
    }

    @Override
    public void reOrder() {
        Collections.reverse(itineraries);
        routesAdapter.notifyItemRangeChanged(0, itineraries.size());
    }

    @Override
    public Long getEarliestEndTime() {
        if (itineraries.isEmpty())
            return calendar.getTimeInMillis();
        long earliestEndTime = itineraries.get(0).endTime();
        for (RouteQuery.Itinerary itinerary : itineraries) {
            if (earliestEndTime > itinerary.endTime())
                earliestEndTime = itinerary.endTime();
        }
        return earliestEndTime - 1000L;
    }

    @Override
    public Long getLatestStartTime() {
        if (itineraries.isEmpty())
            return calendar.getTimeInMillis();
        long latestStartTime = itineraries.get(0).startTime();
        for (RouteQuery.Itinerary itinerary : itineraries) {
            if (latestStartTime < itinerary.startTime())
                latestStartTime = itinerary.startTime();
        }
        return latestStartTime + 1000L;
    }

    @OnClick(R.id.btnNow)
    public void onNowClick() {
        calendar.setTimeInMillis(System.currentTimeMillis());
        displayDate();
        displayTime();
        presenter.loadResult();
    }

    @OnClick(R.id.btnEarlier)
    public void onEarlierClick() {
        presenter.loadEarlier();
    }

    @OnClick(R.id.btnLater)
    public void onLaterClick() {
        presenter.loadLater();
    }

    public void saveOrigin() {
        if (origin == null)
            return;
        writeCache("origin", origin);
    }

    public void saveDestination() {
        if (destination == null)
            return;
        writeCache("destination", destination);
    }

    public void readOrigin() {
        origin = readCache("origin");
    }

    public void readDestination() {
        destination = readCache("destination");
    }

    private void writeCache(String fileName, Feature feature) {
        FileOutputStream fout = null;
        ObjectOutputStream oos = null;
        try {
            File file = new File(getCacheDir(), fileName);
            if (!file.createNewFile()) {
                PrintWriter pw = new PrintWriter(file);
                pw.close();
            }
            fout = new FileOutputStream(file);
            oos = new ObjectOutputStream(fout);
            oos.writeObject(feature);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fout != null) {
                try {
                    fout.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (oos != null) {
                try {
                    oos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private Feature readCache(String fileName) {
        Feature feature = null;
        FileInputStream fin = null;
        ObjectInputStream ois = null;

        try {

            fin = new FileInputStream(new File(getCacheDir(), fileName));
            ois = new ObjectInputStream(fin);
            feature = (Feature) ois.readObject();

        } catch (IOException | ClassNotFoundException ex) {
            ex.printStackTrace();
        } finally {

            if (fin != null) {
                try {
                    fin.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (ois != null) {
                try {
                    ois.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }

        return feature;
    }
}
