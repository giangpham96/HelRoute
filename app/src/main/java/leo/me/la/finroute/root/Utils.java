package leo.me.la.finroute.root;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.view.Display;
import android.view.WindowManager;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import leo.me.la.finroute.R;
import leo.me.la.finroute.type.Mode;

public class Utils {
    private static final NumberFormat kmFormatter = new DecimalFormat("#0.0");
    private volatile static SimpleDateFormat shortDateFormat;
    private volatile static SimpleDateFormat shortTimeFormat;
    private volatile static SimpleDateFormat queryDateFormat;
    private volatile static SimpleDateFormat queryTimeFormat;

    public static SimpleDateFormat getShortDateFormat() {
        if (shortDateFormat == null) {
            synchronized (Utils.class) {
                shortDateFormat = new SimpleDateFormat("EE, MMM dd");
                shortDateFormat.setTimeZone(TimeZone.getTimeZone("Europe/Helsinki"));
            }
        }
        return shortDateFormat;
    }

    public static SimpleDateFormat getShortTimeFormat() {
        if (shortTimeFormat == null) {
            synchronized (Utils.class) {
                shortTimeFormat = new SimpleDateFormat("HH:mm");
                shortTimeFormat.setTimeZone(TimeZone.getTimeZone("Europe/Helsinki"));
            }
        }
        return shortTimeFormat;
    }

    public static SimpleDateFormat getQueryTimeFormat() {
        if (queryTimeFormat == null) {
            synchronized (Utils.class) {
                queryTimeFormat = new SimpleDateFormat("HH:mm:ss");
                queryTimeFormat.setTimeZone(TimeZone.getTimeZone("Europe/Helsinki"));
            }
        }
        return queryTimeFormat;
    }

    public static SimpleDateFormat getQueryDateFormat() {
        if (queryDateFormat == null) {
            synchronized (Utils.class) {
                queryDateFormat = new SimpleDateFormat("yyyy-MM-dd");
                queryDateFormat.setTimeZone(TimeZone.getTimeZone("Europe/Helsinki"));
            }
        }
        return queryDateFormat;
    }

    public static boolean isToday(long when) {
        GregorianCalendar time = new GregorianCalendar();
        time.setTimeInMillis(when);

        int thenYear = time.get(Calendar.YEAR);
        int thenMonth = time.get(Calendar.MONTH);
        int thenMonthDay = time.get(Calendar.DAY_OF_MONTH);

        time.setTimeInMillis(System.currentTimeMillis());
        return (thenYear == time.get(Calendar.YEAR))
                && (thenMonth == time.get(Calendar.MONTH))
                && (thenMonthDay == time.get(Calendar.DAY_OF_MONTH));
    }

    public static boolean isTomorrow(long when) {
        GregorianCalendar time = new GregorianCalendar(TimeZone.getTimeZone("Europe/Helsinki"));
        time.setTimeInMillis(when);

        int thenYear = time.get(Calendar.YEAR);
        int thenMonth = time.get(Calendar.MONTH);
        int thenMonthDay = time.get(Calendar.DAY_OF_MONTH);

        time.setTimeInMillis(System.currentTimeMillis());
        return (thenYear == time.get(Calendar.YEAR))
                && (thenMonth == time.get(Calendar.MONTH))
                && (thenMonthDay == time.get(Calendar.DAY_OF_MONTH) + 1);
    }

    public static String getReadableDistance(@NonNull Double distance) {
        return (distance.intValue() >= 1000)
                ? kmFormatter.format(distance / 1000) + " km"
                : distance.intValue() + " m";
    }

    public static String getReadableDuration(Context context, @NonNull Long duration) {
        String durationText;
        if (duration >= 3600)
            durationText = duration / 3600 + " " + context.getString(R.string.hour) +
                    " " + (duration % 3600) / 60 + " " + context.getString(R.string.min);
        else if (duration >= 60)
            durationText = duration / 60 + " " + context.getString(R.string.min);
        else
            durationText = duration + " " + context.getString(R.string.sec);
        return durationText;
    }

    public static Bitmap getBitmapFromVectorDrawable(Context context, int drawableId) {
        Drawable drawable = ContextCompat.getDrawable(context, drawableId);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            drawable = (DrawableCompat.wrap(drawable)).mutate();
        }

        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    public static int getScreenWidth(Context context) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size.x;
    }

    public static int getScreenHeight(Context context) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size.y;
    }

    public static int getModeDrawableId(Mode mode) {
        int imgResource;
        switch (mode) {
            case BUS:
                imgResource = R.drawable.ic_bus_1;
                break;
            case RAIL:
                imgResource = R.drawable.ic_rail_1;
                break;
            case TRAM:
                imgResource = R.drawable.ic_tram_1;
                break;
            case FERRY:
                imgResource = R.drawable.ic_ferry_1;
                break;
            case SUBWAY:
                imgResource = R.drawable.ic_subway_1;
                break;
            default:
                imgResource = R.drawable.ic_walk_1;
                break;
        }
        return imgResource;
    }


    public static int getModeColorId(Mode mode) {
        int colorId;
        switch (mode) {
            case BUS:
                colorId = R.color.bus;
                break;
            case RAIL:
                colorId = R.color.rail;
                break;
            case TRAM:
                colorId = R.color.tram;
                break;
            case SUBWAY:
                colorId = R.color.subway;
                break;
            case FERRY:
                colorId = R.color.ferry;
                break;
            default:
                colorId = R.color.walk;
        }
        return colorId;
    }

    public static int getModeProgressId(Mode mode) {
        int progressId;
        switch (mode) {
            case BUS:
                progressId = R.drawable.progress_bus;
                break;
            case RAIL:
                progressId = R.drawable.progress_rail;
                break;
            case TRAM:
                progressId = R.drawable.progress_tram;
                break;
            case SUBWAY:
                progressId = R.drawable.progress_subway;
                break;
            case FERRY:
                progressId = R.drawable.progress_ferry;
                break;
            default:
                progressId = R.drawable.progress_walk;
        }
        return progressId;
    }

    public static int getModeFromIcon(Mode mode) {
        int fromIcon = R.drawable.ic_origin_rail;
        switch (mode) {
            case RAIL:
                break;
            case BUS:
                fromIcon = R.drawable.ic_origin_bus;
                break;
            case SUBWAY:
                fromIcon = R.drawable.ic_origin_subway;
                break;
            case FERRY:
                fromIcon = R.drawable.ic_origin_ferry;
                break;
            case TRAM:
                fromIcon = R.drawable.ic_origin_tram;
                break;
            default:
                fromIcon = R.drawable.ic_origin_walk;
        }
        return fromIcon;
    }
}
