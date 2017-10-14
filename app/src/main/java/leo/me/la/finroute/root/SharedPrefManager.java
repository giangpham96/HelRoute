package leo.me.la.finroute.root;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPrefManager {
    private static final String sharedPrefName = "HelRoute.la.me.leo";
    private static final String MAX_TRANSFER = "Max.transfer.la.me.leo";
    private static final String WALK_RELUCTANCE = "Walk.reluctance.la.me.leo";
    private static final String WALK_SPEED = "Walk.speed.la.me.leo";
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

    public static void writeTransportMode(String mode, boolean isSelected) {
        sharedPreferences.edit()
                .putBoolean(mode, isSelected)
                .apply();
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

    public static void writeMaxTransfer(int value) {
        sharedPreferences.edit().putInt(MAX_TRANSFER, value).apply();
    }

    public static void writeWalkReluctance(int value) {
        sharedPreferences.edit().putInt(WALK_RELUCTANCE, value).apply();
    }

    public static void writeWalkingSpeed(int value) {
        sharedPreferences.edit().putInt(WALK_SPEED, value).apply();
    }
}
