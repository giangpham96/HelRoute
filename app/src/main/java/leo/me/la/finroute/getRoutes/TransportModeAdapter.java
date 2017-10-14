package leo.me.la.finroute.getRoutes;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import leo.me.la.finroute.R;
import leo.me.la.finroute.root.SharedPrefManager;

class TransportModeAdapter extends RecyclerView.Adapter<TransportModeAdapter.ModeVH> {
    private Mode[] modes;
    private boolean[] isSelected;
    private Context context;

    TransportModeAdapter(Context context) {
        this.context = context;
        int[] icons = {
                R.drawable.ic_bus_1,
                R.drawable.ic_rail_1,
                R.drawable.ic_tram_1,
                R.drawable.ic_subway_1,
                R.drawable.ic_ferry_1,
        };
        String[] transportModes = context.getResources().getStringArray(R.array.transportModes);
        String[] names = context.getResources().getStringArray(R.array.transportName);
        modes = new Mode[transportModes.length];
        isSelected = new boolean[transportModes.length];
        for (int i = 0; i < modes.length; i++) {
            modes[i] = new Mode(icons[i], transportModes[i], names[i]);
            isSelected[i] = SharedPrefManager.readTransportMode(transportModes[i]);
        }
    }

    String getModes() {
        StringBuilder sb = new StringBuilder(64);
        sb.append("WALK");
        for (int i = 0; i < this.modes.length; i++) {
            if (isSelected[i]) {
                sb.append(",").append(this.modes[i].getMode());
            }
        }
        return sb.toString();
    }

    @Override
    public ModeVH onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.item_transport_mode, parent, false);
        return new ModeVH(itemView);
    }

    @Override
    public void onBindViewHolder(final ModeVH holder, int position) {
        final Mode mode = modes[position];
        holder.bind(mode, isSelected[position]);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isSelected[holder.getAdapterPosition()] = !isSelected[holder.getAdapterPosition()];
                notifyItemChanged(holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return modes.length;
    }

    void saveModes() {
        for (int i = 0; i < modes.length; i++) {
            SharedPrefManager.writeTransportMode(modes[i].getMode(), isSelected[i]);
        }
    }

    static class ModeVH extends RecyclerView.ViewHolder {
        @BindView(R.id.icon)
        ImageView icon;
        @BindView(R.id.name)
        TextView name;

        ModeVH(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        void bind(Mode mode, boolean isSelected) {
            if (!isSelected) {
                icon.setAlpha(0.3f);
                name.setAlpha(0.3f);
            } else {
                icon.setAlpha(1f);
                name.setAlpha(1f);
            }
            icon.setImageResource(mode.getIcon());
            name.setText(mode.getName());
        }
    }

    private static class Mode {
        private int icon;
        private String mode;
        private String name;

        Mode(int icon, String mode, String name) {
            this.icon = icon;
            this.mode = mode;
            this.name = name;
        }

        public int getIcon() {
            return icon;
        }

        public void setIcon(int icon) {
            this.icon = icon;
        }

        public String getMode() {
            return mode;
        }

        public void setMode(String mode) {
            this.mode = mode;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}