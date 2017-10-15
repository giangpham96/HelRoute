package leo.me.la.finroute.root;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPrefManager {
    private static final String sharedPrefName = "HelRoute.la.me.leo";
    private static final String MAX_TRANSFER = "Max.transfer.la.me.leo";
    private static final String WALK_RELUCTANCE = "Walk.reluctance.la.me.leo";
    private static final String WALK_SPEED = "Walk.speed.la.me.leo";
    private static final String LAST_USED_TIME = "Last.used.la.me.leo";
    private static final String LAST_QUERY_TIME = "Last.query.la.me.leo";
    private static final String LAST_ARRIVE_MODE = "Last.arrive.la.me.leo";
    private static SharedPreferences sharedPreferences;

    static void init(Context context) {
        sharedPreferences = context.getSharedPreferences(sharedPrefName, Context.MODE_PRIVATE);
    }

    public static SharedPreferences getInstance() {
        return sharedPreferences;
    }

    public static boolean readTransportMode(String mode) {
        return sharedPreferences.getBoolean(mode, true);
    }

    public static int readMaxTransfer() {
        return sharedPreferences.getInt(MAX_TRANSFER, 2);
    }

    public static int readWalkReluctance() {
        return sharedPreferences.getInt(WALK_RELUCTANCE, 21);
    }

    public static int readWalkingSpeed() {
        return sharedPreferences.getInt(WALK_SPEED, 133);
    }

    public static long readLastUsedTime() {
        return sharedPreferences.getLong(LAST_USED_TIME, 0L);
    }

    public static long readLastQueryTime() {
        return sharedPreferences.getLong(LAST_QUERY_TIME, 0L);
    }

    public static boolean readLastQueryArriveMode() {
        return sharedPreferences.getBoolean(LAST_ARRIVE_MODE, false);
    }

    public static void writeTransportMode(String mode, boolean isSelected) {
        sharedPreferences.edit()
                .putBoolean(mode, isSelected)
                .apply();
    }

    public static void writeMaxTransfer(int value) {
        sharedPreferences.edit().putInt(MAX_TRANSFER, value).apply();
    }

    public static void writeWalkReluctance(int value) {
        sharedPreferences.edit().putInt(WALK_RELUCTANCE, value).apply();
    }

    public static void writeWalkingSpeed(int value) {
        sharedPreferences.edit().putInt(WALK_SPEED, value).apply();
    }

    public static void writeLastUsedTime() {
        sharedPreferences.edit().putLong(LAST_USED_TIME, System.currentTimeMillis()).apply();
    }

    public static void writeLastQueryTime(long time) {
        sharedPreferences.edit().putLong(LAST_QUERY_TIME, time).apply();
    }

    public static void writeLastArriveMode(boolean isArriveBy) {
        sharedPreferences.edit().putBoolean(LAST_ARRIVE_MODE, isArriveBy).apply();
    }
}
