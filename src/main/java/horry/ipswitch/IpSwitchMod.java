package horry.ipswitch;

import arc.Core;
import arc.Events;
import arc.files.Fi;
import arc.util.Log;
import mindustry.Vars;
import mindustry.game.EventType.ClientLoadEvent;
import mindustry.game.EventType.ConnectPacketEvent;
import mindustry.gen.Icon;
import mindustry.mod.Mod;

public class IpSwitchMod extends Mod {

    public static IpSwitchMod instance;
    public static ProxyConfig config;

    private Fi configFile;
    private boolean pauseButtonAdded = false;

    @Override
    public void init() {
        instance = this;

        configFile = Core.files.local("ip_switch.json");
        config = new ProxyConfig(configFile);

        if (config.active() != null) {
            Log.info("[IP Switch] активный прокси при старте: @", config.active().name);
            SocketHook.install(config);
        }

        if (config.spoofUuid) {
            UuidSwapper.rotate();
        }

        Events.on(ClientLoadEvent.class, e -> {
            Log.info("[IP Switch] клиент загружен. прокси: @, spoofUuid=@",
                config.active() == null ? "нет" : config.active().name,
                config.spoofUuid);

            // Кнопка в главном меню
            try {
                Vars.ui.menufrag.addButton("IP Switch", Icon.settings, IpSwitchMenu::show);
                Log.info("[IP Switch] кнопка в главном меню добавлена");
            } catch (Throwable t) {
                Log.err("[IP Switch] главное меню: @", t.getMessage());
            }

            // Кнопка в меню паузы
            try {
                Vars.ui.paused.cont.row();
                Vars.ui.paused.cont.button("IP Switch", Icon.settings, IpSwitchMenu::show)
                    .width(300f).height(60f).pad(6f).row();
                pauseButtonAdded = true;
                Log.info("[IP Switch] кнопка в меню паузы добавлена");
            } catch (Throwable t) {
                Log.err("[IP Switch] меню паузы: @", t.getMessage());
            }
        });

        Events.on(ConnectPacketEvent.class, e -> {
            if (config.spoofUuid) UuidSwapper.apply();
            if (config.active() != null) {
                SocketHook.patchNetClient(config);
            }
        });

        Log.info("[IP Switch] ядро мода инициализировано.");
    }

    @Override
    public void registerClientCommands(arc.util.CommandHandler handler) {
        // не регистрируем — на Android чат-команды мода не вызываются.
    }
}
