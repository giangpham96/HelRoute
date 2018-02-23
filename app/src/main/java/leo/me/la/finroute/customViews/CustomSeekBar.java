package leo.me.la.finroute.customViews;

import android.content.Context;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import leo.me.la.finroute.R;

public class CustomSeekBar {

    private int maxCount, textColor, startValue;
    private Context context;
    private LinearLayout labelContainer;
    private SeekBar mSeekBar;
    private String labels[];

    public CustomSeekBar(Context context, int maxCount, int textColor, int startValue, String labels[]) {
        this.context = context;
        this.maxCount = maxCount;
        this.textColor = textColor;
        this.startValue = startValue;
        this.labels = labels;
    }

    public void addSeekBar(LinearLayout parent) {
        mSeekBar = new SeekBar(context);
        mSeekBar.setMax(maxCount - startValue);
        mSeekBar.setOnTouchListener(new ListView.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        // Disallow Drawer to intercept touch events.
                        v.getParent().requestDisallowInterceptTouchEvent(true);
                        break;

                    case MotionEvent.ACTION_UP:
                        // Allow Drawer to intercept touch events.
                        v.getParent().requestDisallowInterceptTouchEvent(false);
                        break;
                }

                // Handle seekbar touch events.
                v.onTouchEvent(event);
                return true;
            }
        });
        // Add LinearLayout for labels below SeekBar
        labelContainer = new LinearLayout(context);
        labelContainer.setOrientation(LinearLayout.HORIZONTAL);
//        labelContainer.setPadding(10, 0, 10, 0);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        int margin = context.getResources().getDimensionPixelSize(R.dimen.mp_normal);
        params.setMargins(margin, 0, margin, 0);
        labelContainer.setLayoutParams(params);

        addLabelsBelowSeekBar();
        parent.addView(mSeekBar);
        parent.addView(labelContainer);
    }

    private void addLabelsBelowSeekBar() {
        for (int i = 0; i < labels.length; i++) {
            TextView textView = new TextView(context);
            textView.setText(labels[i]);
            textView.setTextColor(textColor);
            textView.setTextSize(10f);
            textView.setGravity(Gravity.START);
            labelContainer.addView(textView);
            textView.setLayoutParams((i == labels.length - 1) ? getLayoutParams(0.0f) : getLayoutParams(1.0f));
        }
    }

    private LinearLayout.LayoutParams getLayoutParams(float weight) {
        return new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT, weight);
    }

    public int getProgress() {
        if (mSeekBar != null)
            return mSeekBar.getProgress() + startValue;
        return startValue;
    }

    public void setProgress(int progress) {
        if (mSeekBar != null)
            mSeekBar.setProgress(progress - startValue);
    }
}
