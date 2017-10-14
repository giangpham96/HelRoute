package leo.me.la.finroute.showMap;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import leo.me.la.finroute.R;
import leo.me.la.finroute.RouteQuery;

class StopAdapter extends RecyclerView.Adapter<StopAdapter.StopVH> {
    private Context context;
    private List<RouteQuery.Stop> stops;
    private int from, to;

    StopAdapter(Context context, List<RouteQuery.Stop> stops, int from, int to) {
        this.context = context;
        this.stops = stops;
        this.from = from;
        this.to = to;
    }

    @Override
    public int getItemViewType(int position) {
        return (position > from && position < to) ? 1 : 0;
    }

    @Override
    public StopVH onCreateViewHolder(ViewGroup parent, int viewType) {
        int layoutId = (viewType == 1) ? R.layout.item_stop : R.layout.item_stop_hide;
        View itemView = LayoutInflater.from(context).inflate(layoutId, parent, false);
        return new StopVH(itemView);
    }

    @Override
    public void onBindViewHolder(StopVH holder, int position) {
        holder.bind(stops.get(position));
    }

    @Override
    public int getItemCount() {
        return stops.size();
    }

    static class StopVH extends RecyclerView.ViewHolder {
        @Nullable
        @BindView(R.id.tvStopName)
        TextView tvStopName;
        @Nullable
        @BindView(R.id.tvStopCode)
        TextView tvStopCode;

        StopVH(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void bind(RouteQuery.Stop stop) {
            if (tvStopName != null) {
                tvStopName.setText(stop.name());
            }
            if (tvStopCode != null) {
                tvStopCode.setText((stop.code() != null) ? stop.code() : "");
            }
        }
    }
}
