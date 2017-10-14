package leo.me.la.finroute.showAlert;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import leo.me.la.finroute.AlertQuery;
import leo.me.la.finroute.R;

class AlertAdapter extends RecyclerView.Adapter<AlertAdapter.AlertVH> {
    private Context context;
    private List<AlertQuery.Alert> alerts;

    AlertAdapter(Context context, List<AlertQuery.Alert> alerts) {
        this.context = context;
        this.alerts = alerts;
    }

    @Override
    public AlertAdapter.AlertVH onCreateViewHolder(ViewGroup parent, int viewType) {
        return new AlertVH(LayoutInflater.from(context)
                .inflate(R.layout.item_alert, parent, false));
    }

    @Override
    public void onBindViewHolder(AlertAdapter.AlertVH holder, int position) {
        holder.bind(alerts.get(position));
    }

    @Override
    public int getItemCount() {
        return alerts.size();
    }

    class AlertVH extends RecyclerView.ViewHolder {
        TextView tvAlert;

        AlertVH(View itemView) {
            super(itemView);
            tvAlert = itemView.findViewById(R.id.tvAlert);
        }

        public void bind(AlertQuery.Alert alert) {
            tvAlert.setText(alert.alertDescriptionText());
        }
    }
}
