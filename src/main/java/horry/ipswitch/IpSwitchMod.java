package horry.ipswitch;

import arc.util.Log;
import mindustry.mod.Mod;

public class IpSwitchMod extends Mod {

    static {
        Log.info("[IP Switch] STATIC BLOCK RUNS");
    }

    public IpSwitchMod() {
        Log.info("[IP Switch] CONSTRUCTOR RUNS");
    }

    @Override
    public void init() {
        Log.info("[IP Switch] INIT RUNS");
    }
}
