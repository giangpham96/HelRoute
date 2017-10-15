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
    public int getItemViewType(int position) {
        String text = alerts.get(position).alertDescriptionText();
        for (int i = 0; i < position; i++) {
            if (alerts.get(i).alertDescriptionText().equals(text)) {
                return 0;
            }
        }
        return 1;
    }

    @Override
    public AlertAdapter.AlertVH onCreateViewHolder(ViewGroup parent, int viewType) {
        int layoutId = (viewType == 0) ? R.layout.item_invisible : R.layout.item_alert;
        return new AlertVH(LayoutInflater.from(context)
                .inflate(layoutId, parent, false));
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
        TextView tvTranslate;

        AlertVH(View itemView) {
            super(itemView);
            tvAlert = itemView.findViewById(R.id.tvAlert);
            tvTranslate = itemView.findViewById(R.id.tvTranslate);
        }

        public void bind(AlertQuery.Alert alert) {
            if (tvTranslate != null) {
                tvTranslate.setVisibility(View.GONE);
                for (AlertQuery.AlertDescriptionTextTranslation adtt : alert.alertDescriptionTextTranslations()) {
                    if (adtt.language() != null && adtt.language().equals("en")) {
                        tvTranslate.setVisibility(View.VISIBLE);
                        tvTranslate.setText(adtt.text());
                        break;
                    }
                }
            }
            if (tvAlert != null)
                tvAlert.setText(alert.alertDescriptionText());
        }
    }
}
