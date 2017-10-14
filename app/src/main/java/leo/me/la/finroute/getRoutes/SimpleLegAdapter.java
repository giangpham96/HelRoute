package leo.me.la.finroute.getRoutes;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v4.content.ContextCompat;
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
import leo.me.la.finroute.type.Mode;

class SimpleLegAdapter extends RecyclerView.Adapter<SimpleLegAdapter.ViewHolder> {

    private Context context;
    private List<RouteQuery.Leg> legs;

    SimpleLegAdapter(Context context, List<RouteQuery.Leg> legs) {
        this.context = context;
        this.legs = legs;
    }

    @Override
    public SimpleLegAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.item_transport, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(SimpleLegAdapter.ViewHolder holder, int position) {
        holder.bind(legs.get(position));
    }

    @Override
    public int getItemCount() {
        return legs.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.tvCode)
        TextView tvCode;

        ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        void bind(RouteQuery.Leg leg) {
            int imgResource = Utils.getModeDrawableId(leg.mode());
            int colorId = Utils.getModeColorId(leg.mode());
            String subtitle = "";
            if (leg.mode() == Mode.WALK || leg.mode() == Mode.BICYCLE) {
                if (leg.distance() != null)
                    subtitle = Utils.getReadableDistance(leg.distance());
                tvCode.setTypeface(Typeface.DEFAULT);
            } else {
                if (leg.route() != null && leg.route().shortName() != null)
                    subtitle = leg.route().shortName();
                tvCode.setTypeface(Typeface.DEFAULT_BOLD);
            }
            tvCode.setText(subtitle);
            tvCode.setTextColor(ContextCompat.getColor(itemView.getContext(), colorId));
            tvCode.setCompoundDrawablesWithIntrinsicBounds(0, imgResource, 0, 0);
        }
    }
}
