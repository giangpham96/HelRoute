package leo.me.la.finroute.getRoutes;

import android.content.Context;
import android.content.Intent;
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
import leo.me.la.finroute.root.Utils;
import leo.me.la.finroute.showMap.ItineraryHolder;
import leo.me.la.finroute.showMap.MapActivity;

import static leo.me.la.finroute.showMap.MapActivity.ITINERARY_ID;

class RoutesAdapter extends RecyclerView.Adapter<RoutesAdapter.ViewHolder> {
    private Context context;
    private List<RouteQuery.Itinerary> itineraries;

    RoutesAdapter(Context context, List<RouteQuery.Itinerary> itineraries) {
        this.context = context;
        this.itineraries = itineraries;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.item_route, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bind(itineraries.get(position));
    }

    @Override
    public int getItemCount() {
        return itineraries.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.tvFrom)
        TextView tvFrom;
        @BindView(R.id.tvTo)
        TextView tvTo;
        @BindView(R.id.tvTotalTime)
        TextView tvTotalTime;
        @BindView(R.id.rcvLeg)
        RecyclerView rcvLeg;
        @BindView(R.id.additionalInfo)
        View additionalInfo;
        @BindView(R.id.tvPrice)
        TextView tvPrice;
        @BindView(R.id.tvWalk)
        TextView tvWalk;

        SimpleLegAdapter adapter;

        ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void bind(final RouteQuery.Itinerary itinerary) {
            tvFrom.setText(Utils.getShortTimeFormat().format(itinerary.startTime()));
            tvTo.setText(Utils.getShortTimeFormat().format(itinerary.endTime()));
            Long duration = itinerary.duration();
            String durationText = (duration != null)
                    ? Utils.getReadableDuration(itemView.getContext(), duration)
                    : "";
            tvTotalTime.setText(durationText);
            additionalInfo.setVisibility((itinerary.fares() == null) ? View.GONE : View.VISIBLE);
            if (itinerary.fares() != null && !itinerary.fares().isEmpty())
                tvPrice.setText((float) itinerary.fares().get(0).cents() / 100 + " " + itemView.getContext().getString(R.string.euro));
            if (itinerary.walkDistance() != null) {
                String distance = Utils.getReadableDistance(itinerary.walkDistance());
                tvWalk.setText(distance);
            } else {
                tvWalk.setText("");
            }
            adapter = new SimpleLegAdapter(itemView.getContext(), itinerary.legs());
            rcvLeg.setAdapter(adapter);
            View.OnClickListener clickListener = new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    long id = System.currentTimeMillis();
                    ItineraryHolder.getInstance().save(id, itinerary);
                    Intent intent = new Intent(itemView.getContext(), MapActivity.class);
                    intent.putExtra(ITINERARY_ID, id);
                    itemView.getContext().startActivity(intent);
                }
            };
            itemView.setOnClickListener(clickListener);
        }
    }
}
