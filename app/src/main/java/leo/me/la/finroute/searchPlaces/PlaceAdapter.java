package leo.me.la.finroute.searchPlaces;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import leo.me.la.finroute.R;
import leo.me.la.finroute.http.apiModel.Feature;

class PlaceAdapter extends RecyclerView.Adapter<PlaceAdapter.PlaceViewHolder> {
    private List<Feature> features;
    private Context context;
    private OnItemClickListener onItemClickListener;

    PlaceAdapter(List<Feature> features, Context context) {
        this.features = features;
        this.context = context;
    }

    @Override
    public PlaceViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.item_place, parent, false);
        return new PlaceViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(PlaceViewHolder holder, int position) {
        holder.bind(features.get(position), onItemClickListener);
    }

    @Override
    public int getItemCount() {
        return features.size();
    }

    void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    interface OnItemClickListener {
        void onItemClick(Feature feature);
    }

    static class PlaceViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.icon)
        ImageView icon;
        @BindView(R.id.tvLabel)
        TextView tvLabel;
        @BindView(R.id.tvAddress)
        TextView tvAddress;

        PlaceViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        void bind(final Feature feature, final OnItemClickListener onItemClickListener) {
            int thumbnailId;
            String name = feature.getProperties().getName();
            String label = feature.getProperties().getLabel();
            String sub = label.replaceFirst(name + ", ", "");
            switch (feature.getProperties().getLayer()) {
                case "venue":
                    thumbnailId = R.drawable.ic_venue;
                    break;
                case "address":
                    thumbnailId = R.drawable.ic_address;
                    break;
                case "street":
                    thumbnailId = R.drawable.ic_street;
                    break;
                case "neighbourhood":
                case "borough":
                case "localadmin":
                case "locality":
                case "county":
                case "macrocounty":
                case "region":
                case "macroregion":
                    thumbnailId = R.drawable.ic_city;
                    break;
                case "country":
                    thumbnailId = R.drawable.ic_country;
                    break;
                case "station":
                    thumbnailId = R.drawable.ic_station;
                    break;
                case "stop":
                    thumbnailId = R.drawable.ic_stop;
                    break;
                default:
                    thumbnailId = R.drawable.ic_venue;
                    break;
            }
            icon.setImageResource(thumbnailId);
            tvLabel.setText(name);
            tvAddress.setText(sub);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onItemClickListener != null)
                        onItemClickListener.onItemClick(feature);
                }
            });
        }
    }
}
