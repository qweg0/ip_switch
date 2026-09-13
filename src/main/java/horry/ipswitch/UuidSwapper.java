
package horry.ipswitch;

import arc.util.Log;

import java.util.UUID;

public class UuidSwapper {

    private static String currentUuid = null;

    public static String rotate() {
        currentUuid = UUID.randomUUID().toString();
        Log.info("[IP Switch] UUID повёрнут: @", currentUuid);
        apply();
        return currentUuid;
    }

    public static String current() {
        return currentUuid;
    }

    public static void apply() {
        if (currentUuid == null) return;
        try {
            Object settings = arc.Core.settings;
            if (settings == null) return;
            settings.getClass()
                .getMethod("put", String.class, Object.class, Object.class)
                .invoke(settings, "uuid", currentUuid, null);
            Log.info("[IP Switch] UUID записан в settings");
        } catch (Throwable t) {
            Log.err("[IP Switch] UUID apply: @", t.getMessage());
        }
    }

    public static void reset() {
        currentUuid = null;
        Log.info("[IP Switch] UUID вернётся к стандартному при следующем запуске");
    }
}
