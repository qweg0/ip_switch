package horry.ipswitch;

import arc.Core;
import arc.files.Fi;
import arc.util.Log;
import mindustry.mod.Mod;

public class IpSwitchMod extends Mod {

    // статический блок — срабатывает при загрузке класса
    static {
        Log.info("[IP Switch] === STATIC BLOCK RUNS ===");
    }

    // поле нужно, чтобы IpSwitchMenu.java компилировался
    public static IpSwitchMod instance;
    public static ProxyConfig config;

    public IpSwitchMod() {
        Log.info("[IP Switch] === CONSTRUCTOR RUNS ===");
    }

    @Override
    public void init() {
        Log.info("[IP Switch] === INIT RUNS ===");

        try {
            Fi file = Core.files.local("ip_switch.json");
            config = new ProxyConfig(file);
            Log.info("[IP Switch] === CONFIG LOADED ===");
        } catch (Throwable t) {
            Log.err("[IP Switch] config err: @", t.getMessage());
        }
    }
}
