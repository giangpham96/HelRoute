package leo.me.la.finroute.showAlert;

import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import leo.me.la.finroute.AlertQuery;
import leo.me.la.finroute.R;

public class AlertActivity extends AppCompatActivity {
    @Inject
    ApolloClient client;
    @BindView(R.id.rcvAlert)
    RecyclerView rcvAlert;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.annouce)
    View annouce;
    @BindView(R.id.tvAnnounce)
    TextView tvAnnounce;
    @BindView(R.id.tvSubAnnouce)
    TextView tvSubAnnouce;
    @BindView(R.id.progress)
    ProgressBar progress;
    @BindView(R.id.imgAnnounce)
    ImageView imgAnnounce;
    AlertAdapter adapter;

    @OnClick(R.id.tvSubAnnouce)
    void onRetry() {
        requestAlerts();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alert);
        ButterKnife.bind(this);
        DaggerAlertComponent.create().inject(this);
        setupToolbar();
        requestAlerts();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }

    private void requestAlerts() {
        progress.setVisibility(View.VISIBLE);
        rcvAlert.setVisibility(View.GONE);
        annouce.setVisibility(View.GONE);
        AlertQuery query = AlertQuery.builder().build();
        client.query(query).enqueue(new ApolloCall.Callback<AlertQuery.Data>() {
            @Override
            public void onResponse(@Nonnull Response<AlertQuery.Data> response) {
                if (response.hasErrors()) {
                    AlertActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showErrors();
                        }
                    });
                    return;
                }
                if (response.data().alerts() == null || response.data().alerts().size() == 0) {
                    AlertActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showEmpty();
                        }
                    });
                    return;
                }
                adapter = new AlertAdapter(AlertActivity.this, response.data().alerts());
                AlertActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        annouce.setVisibility(View.GONE);
                        progress.setVisibility(View.GONE);
                        rcvAlert.setVisibility(View.VISIBLE);
                        rcvAlert.setAdapter(adapter);
                    }
                });
            }

            @Override
            public void onFailure(@Nonnull ApolloException e) {
                AlertActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showErrors();
                    }
                });
            }
        });
    }

    private void showEmpty() {
        showAnnouncement();
        imgAnnounce.setVisibility(View.GONE);
        tvSubAnnouce.setVisibility(View.GONE);
        tvAnnounce.setText(R.string.no_disruption);
        tvAnnounce.setTextColor(ContextCompat.getColor(this, R.color.accent));
    }

    private void showErrors() {
        showAnnouncement();
        imgAnnounce.setVisibility(View.VISIBLE);
        tvSubAnnouce.setVisibility(View.VISIBLE);
        tvAnnounce.setText(R.string.error_occurs);
        tvAnnounce.setTextColor(ContextCompat.getColor(this, R.color.dark_red));
        tvSubAnnouce.setText(R.string.tap_to_retry);
    }

    private void showAnnouncement() {
        rcvAlert.setVisibility(View.GONE);
        progress.setVisibility(View.GONE);
        annouce.setVisibility(View.VISIBLE);
    }
}
