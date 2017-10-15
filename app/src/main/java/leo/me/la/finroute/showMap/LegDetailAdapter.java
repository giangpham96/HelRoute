package leo.me.la.finroute.showMap;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Optional;
import leo.me.la.finroute.R;
import leo.me.la.finroute.RouteQuery;
import leo.me.la.finroute.root.Utils;
import leo.me.la.finroute.type.Mode;

class LegDetailAdapter extends RecyclerView.Adapter<LegDetailAdapter.LegVH> implements HolderAdapterBridge {
    private Context context;
    private List<RouteQuery.Leg> legs;
    private SparseBooleanArray state;
    private SparseArray<StopAdapter> childStopAdapters;
    private RecyclerView recyclerView;

    LegDetailAdapter(Context context, List<RouteQuery.Leg> legs) {
        this.context = context;
        this.legs = legs;
        state = new SparseBooleanArray();
        childStopAdapters = new SparseArray<>();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        this.recyclerView = recyclerView;
    }

    @Override
    public int getItemViewType(int position) {
        return (legs.get(position).mode() == Mode.WALK) ? 0 : 1;
    }

    @Override
    public LegVH onCreateViewHolder(ViewGroup parent, int viewType) {
        int layout = (viewType == 0) ? R.layout.item_leg_walk : R.layout.item_leg_transport;
        View itemView = LayoutInflater.from(context).inflate(layout, parent, false);
        return new LegVH(itemView, this);
    }

    @Override
    public void onBindViewHolder(final LegVH holder, int position) {
        holder.setPosition(position);
        RouteQuery.Leg prev = (position == 0) ? null : legs.get(position - 1);
        holder.bind(legs.get(position), prev);
    }

    @Override
    public int getItemCount() {
        return legs.size();
    }

    @Override
    public void onVisibilityChange(int position, boolean isShown) {
        state.put(position, isShown);
    }

    @Override
    public boolean currentVisibility(int position) {
        return state.get(position, false);
    }

    @Override
    public void update(int position) {
        notifyItemChanged(position);
    }

    @Override
    public StopAdapter getChildAdapter(int position) {
        return childStopAdapters.get(position);
    }

    @Override
    public void setChildAdapter(int position, StopAdapter adapter) {
        childStopAdapters.put(position, adapter);
    }

    @Override
    public void requestLayout() {
        if (recyclerView != null)
            recyclerView.requestLayout();
    }

    static class LegVH extends RecyclerView.ViewHolder {
        @BindView(R.id.transfer)
        @Nullable
        View transfer;
        @BindView(R.id.tvStartTransfer)
        @Nullable
        TextView tvStartTransfer;
        @BindView(R.id.tvFromTransfer)
        @Nullable
        TextView tvFromTransfer;
        @BindView(R.id.tvStopCodeTransfer)
        @Nullable
        TextView tvStopCodeTransfer;
        @BindView(R.id.tvPlatformTransfer)
        @Nullable
        TextView tvPlatformTransfer;
        @BindView(R.id.tvContentTransfer)
        @Nullable
        TextView tvContentTransfer;
        @BindView(R.id.tvCode)
        @Nullable
        TextView tvCode;
        @BindView(R.id.rcvStop)
        @Nullable
        RecyclerView rcvStop;
        @BindView(R.id.tvStartTime)
        TextView tvStartTime;
        @BindView(R.id.tvStopCode)
        TextView tvStopCode;
        @BindView(R.id.tvPlatform)
        TextView tvPlatform;
        @BindView(R.id.tvContent)
        TextView tvContent;
        @BindView(R.id.tvFrom)
        TextView tvFrom;
        @BindView(R.id.imgFrom)
        ImageView imgFrom;
        @BindView(R.id.view)
        View view;
        @BindView(R.id.destination)
        View destination;
        @BindView(R.id.tvEndTime)
        TextView tvEndTime;
        @BindView(R.id.tvDuration)
        TextView tvDuration;
        @Nullable
        @BindView(R.id.tvShowHide)
        TextView tvShowHide;

        private int position;
        private HolderAdapterBridge bridge;

        LegVH(View itemView, @NonNull HolderAdapterBridge bridge) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            this.bridge = bridge;
        }

        public void setPosition(int position) {
            this.position = position;
        }

        @Optional
        @OnClick(R.id.tvShowHide)
        void onContentClick() {
            if (rcvStop != null && tvShowHide != null) {
                bridge.onVisibilityChange(position, !bridge.currentVisibility(position));
                if (bridge.currentVisibility(position)) {
                    rcvStop.setVisibility(View.VISIBLE);
                    Animation slideDown = AnimationUtils.loadAnimation(itemView.getContext(),
                            R.anim.slide_down);
                    rcvStop.startAnimation(slideDown);
                    tvShowHide.setText(itemView.getContext().getString(R.string.hide));
                    tvShowHide.setBackgroundResource(R.drawable.bg_hide);
                } else {
                    Animation slideUp = AnimationUtils.loadAnimation(itemView.getContext(),
                            R.anim.slide_up);
                    rcvStop.startAnimation(slideUp);
                    slideUp.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            bridge.update(position);
                            bridge.requestLayout();
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {

                        }
                    });
                }
            }
        }

        public void bind(RouteQuery.Leg leg, RouteQuery.Leg prev) {
            if (transfer != null) {
                bindTransfer(leg, prev);
            }
            bindInfo(leg);
            if (leg.mode() != null && leg.mode() != Mode.WALK && tvCode != null) {
                bindTransportMode(leg);
                bindTransportContent(leg);
            } else if (leg.mode() != null && leg.mode() == Mode.WALK) {
                bindWalkContent(leg);
            }
            bindOriginDestination(leg);
        }

        private void bindTransfer(RouteQuery.Leg leg, RouteQuery.Leg prev) {
            boolean isTransfer = prev != null
                    && prev.to().stop() != null
                    && leg.from().stop() != null
                    && prev.to().stop().code() != null
                    && leg.from().stop().code() != null
                    && prev.to().stop().code().equals(leg.from().stop().code());
            if (isTransfer && transfer != null) {
                transfer.setVisibility(View.VISIBLE);
                if (tvStartTransfer != null && prev.endTime() != null)
                    tvStartTransfer.setText(Utils.getShortTimeFormat().format(prev.endTime()));
                if (tvFromTransfer != null && prev.to().stop() != null) {
                    tvFromTransfer.setText(prev.to().stop().name());
                    tvFromTransfer.setSelected(true);
                }
                if (tvStopCodeTransfer != null) {
                    tvStopCodeTransfer.setText(prev.to().stop().code());
                }
                if (tvPlatformTransfer != null) {
                    if (prev.to().stop().platformCode() != null) {
                        tvPlatformTransfer.setText(itemView.getContext().getString(R.string.platform) + " " +
                                prev.to().stop().platformCode());
                        tvPlatformTransfer.setVisibility(View.VISIBLE);
                    } else
                        tvPlatformTransfer.setVisibility(View.GONE);
                }
                if (leg.startTime() != null && prev.endTime() != null
                        && tvContentTransfer != null) {
                    Long time = (leg.startTime() - prev.endTime()) / 1000;
                    if (time >= 60)
                        tvContentTransfer.setText(
                                itemView.getContext().getString(R.string.wait) + " " +
                                        Utils.getReadableDuration(itemView.getContext(), time));
                    else
                        transfer.setVisibility(View.GONE);
                }
            } else if (transfer != null) {
                transfer.setVisibility(View.GONE);
            }
        }

        private void bindInfo(RouteQuery.Leg leg) {
            tvStartTime.setText(Utils.getShortTimeFormat().format(leg.startTime()));
            tvFrom.setText(leg.from().name());
            tvFrom.setSelected(true);
            if (leg.from().stop() != null && leg.from().stop().code() != null) {
                tvStopCode.setVisibility(View.VISIBLE);
                tvStopCode.setText(leg.from().stop().code());
            } else
                tvStopCode.setVisibility(View.GONE);
            if (leg.from().stop() != null && leg.from().stop().platformCode() != null) {
                tvPlatform.setVisibility(View.VISIBLE);
                tvPlatform.setText(itemView.getContext().getString(R.string.platform) + " " + leg.from().stop().platformCode());
            } else
                tvPlatform.setVisibility(View.GONE);
        }

        private void bindTransportMode(RouteQuery.Leg leg) {
            int imgResource = Utils.getModeDrawableId(leg.mode());
            int progress = Utils.getModeProgressId(leg.mode());
            int fromIcon = Utils.getModeFromIcon(leg.mode());
            int colorId = Utils.getModeColorId(leg.mode());
            tvCode.setCompoundDrawablesWithIntrinsicBounds(0, imgResource, 0, 0);
            tvCode.setTextColor(ContextCompat.getColor(itemView.getContext(), colorId));
            if (leg.route() != null && leg.route().shortName() != null)
                tvCode.setText(leg.route().shortName());
            view.setBackgroundResource(progress);
            imgFrom.setImageResource(fromIcon);
        }

        private void bindTransportContent(RouteQuery.Leg leg) {
            String time;
            String stops;
            if (leg.distance() != null && leg.endTime() != null && leg.startTime() != null)
                time = "(" + Utils.getReadableDuration(itemView.getContext(),
                        (leg.endTime() - leg.startTime()) / 1000) + ")";
            else
                time = "";
            if (leg.intermediateStops() != null && leg.intermediateStops().size() > 0) {
                stops = leg.intermediateStops().size() + " " + itemView.getContext().getString(R.string.stop);
                bindStops(leg.intermediateStops());
            } else
                stops = itemView.getContext().getString(R.string.no_stop);
            tvContent.setText(stops);
            tvDuration.setText(time);
            if (tvShowHide != null && rcvStop != null && stops.equals(itemView.getContext().getString(R.string.no_stop))) {
                tvShowHide.setVisibility(View.GONE);
                rcvStop.setVisibility(View.GONE);
            } else if (tvShowHide != null && !stops.equals(itemView.getContext().getString(R.string.no_stop))) {
                tvShowHide.setVisibility(View.VISIBLE);
            }
        }

        private void bindStops(List<RouteQuery.IntermediateStop> stops) {
            if (rcvStop != null && tvShowHide != null) {
                if (bridge.getChildAdapter(position) == null) {
                    bridge.setChildAdapter(position, new StopAdapter(itemView.getContext(), stops));
                }
                rcvStop.swapAdapter(bridge.getChildAdapter(position), true);
                rcvStop.setVisibility(bridge.currentVisibility(position) ? View.VISIBLE : View.GONE);
                tvShowHide.setText(bridge.currentVisibility(position)
                        ? itemView.getContext().getString(R.string.hide)
                        : itemView.getContext().getString(R.string.show));
                tvShowHide.setBackgroundResource(bridge.currentVisibility(position) ? R.drawable.bg_hide : R.drawable.bg_show);
            }
        }

        private void bindWalkContent(RouteQuery.Leg leg) {
            String time;
            String distance;
            imgFrom.setImageResource(R.drawable.ic_origin_walk);
            if (leg.distance() != null && leg.endTime() != null && leg.startTime() != null)
                time = "(" + Utils.getReadableDuration(itemView.getContext(),
                        (leg.endTime() - leg.startTime()) / 1000) + ")";
            else
                time = "";
            if (leg.distance() != null)
                distance = itemView.getContext().getString(R.string.walk) + " "
                        + Utils.getReadableDistance(leg.distance());
            else
                distance = "";
            tvContent.setText(distance);
            tvDuration.setText(time);
        }

        private void bindOriginDestination(RouteQuery.Leg leg) {
            if (leg.from().name() != null && leg.from().name().equals("Origin")) {
                imgFrom.setImageResource(R.drawable.ic_origin_mark);
            }
            if (leg.to().name() != null && leg.to().name().equals("Destination")) {
                destination.setVisibility(View.VISIBLE);
                if (leg.endTime() != null)
                    tvEndTime.setText(Utils.getShortTimeFormat().format(leg.endTime()));
            } else {
                destination.setVisibility(View.GONE);
            }
        }
    }
}
