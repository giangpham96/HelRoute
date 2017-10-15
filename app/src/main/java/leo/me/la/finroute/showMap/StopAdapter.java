package leo.me.la.finroute.showMap;

import android.content.Context;
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
    private List<RouteQuery.IntermediateStop> stops;

    StopAdapter(Context context, List<RouteQuery.IntermediateStop> stops) {
        this.context = context;
        this.stops = stops;
    }


    @Override
    public StopVH onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.item_stop, parent, false);
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
        @BindView(R.id.tvStopName)
        TextView tvStopName;
        @BindView(R.id.tvStopCode)
        TextView tvStopCode;

        StopVH(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void bind(RouteQuery.IntermediateStop stop) {
            tvStopName.setText(stop.name());

            tvStopCode.setText((stop.code() != null) ? stop.code() : "");
        }
    }
}
