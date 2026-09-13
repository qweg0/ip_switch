package horry.ipswitch;

import arc.Core;
import arc.Events;
import arc.files.Fi;
import arc.util.Log;
import mindustry.Vars;
import mindustry.game.EventType.ClientLoadEvent;
import mindustry.game.EventType.ConnectPacketEvent;
import mindustry.game.EventType.ResizeEvent;
import mindustry.game.EventType.StateChangeEvent;
import mindustry.gen.Icon;
import mindustry.mod.Mod;

public class IpSwitchMod extends Mod {

    public static IpSwitchMod instance;
    public static ProxyConfig config;

    private Fi configFile;
    private boolean mainButtonAdded = false;
    private boolean pauseButtonAdded = false;

    @Override
    public void init() {
        instance = this;
        Log.info("[IP Switch] START init()");

        try {
            configFile = Core.files.local("ip_switch.json");
            config = new ProxyConfig(configFile);
            Log.info("[IP Switch] config loaded");
        } catch (Throwable t) {
            Log.err("[IP Switch] config error: @", t.getMessage());
        }

        Events.on(ClientLoadEvent.class, e -> {
            Log.info("[IP Switch] ClientLoadEvent fired");
            addButtons();
        });

        Events.on(ResizeEvent.class, e -> addButtons());

        Events.on(StateChangeEvent.class, e -> {
            pauseButtonAdded = false;
            addButtons();
        });

        Events.on(ConnectPacketEvent.class, e -> {
            if (config == null) return;
            if (config.spoofUuid) UuidSwapper.apply();
            if (config.active() != null) {
                SocketHook.patchNetClient(config);
            }
        });

        Log.info("[IP Switch] init() done");
    }

    private void addButtons() {
        if (!mainButtonAdded) {
            try {
                if (Vars.ui != null && Vars.ui.menufrag != null) {
                    Vars.ui.menufrag.addButton("IP Switch", Icon.settings, IpSwitchMenu::show);
                    mainButtonAdded = true;
                    Log.info("[IP Switch] main menu button added");
                }
            } catch (Throwable t) {
                Log.err("[IP Switch] main menu: @", t.getMessage());
            }
        }

        if (!pauseButtonAdded) {
            try {
                if (Vars.ui != null && Vars.ui.paused != null && Vars.ui.paused.cont != null) {
                    Vars.ui.paused.cont.row();
                    Vars.ui.paused.cont.button("IP Switch", Icon.settings, IpSwitchMenu::show)
                        .width(300f).height(60f).pad(6f).row();
                    pauseButtonAdded = true;
                    Log.info("[IP Switch] pause menu button added");
                }
            } catch (Throwable t) {
                Log.err("[IP Switch] pause menu: @", t.getMessage());
            }
        }
    }

    @Override
    public void registerClientCommands(arc.util.CommandHandler handler) {
    }
}
