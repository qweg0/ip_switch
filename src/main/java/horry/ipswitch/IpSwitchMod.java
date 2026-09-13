```java
package horry.ipswitch;

import arc.Core;
import arc.Events;
import arc.files.Fi;
import arc.util.Log;
import mindustry.game.EventType.ClientLoadEvent;
import mindustry.game.EventType.ConnectPacketEvent;
import mindustry.mod.Mod;

public class IpSwitchMod extends Mod {

    public static IpSwitchMod instance;
    public static ProxyConfig config;
    public static SwitchCommand commands;

    private Fi configFile;

    @Override
    public void init() {
        instance = this;

        configFile = Core.files.local("ip_switch.json");
        config = new ProxyConfig(configFile);
        commands = new SwitchCommand(config);

        if (config.active() != null) {
            Log.info("[IP Switch] активный прокси при старте: @", config.active().name);
            SocketHook.install(config);
        }

        if (config.spoofUuid) {
            UuidSwapper.rotate();
        }

        Events.on(ClientLoadEvent.class, e ->
            Log.info("[IP Switch] клиент загружен. прокси: @, spoofUuid=@",
                config.active() == null ? "нет" : config.active().name,
                config.spoofUuid));

        Events.on(ConnectPacketEvent.class, e -> {
            if (config.spoofUuid) UuidSwapper.apply();
            if (config.active() != null) {
                boolean ok = SocketHook.patchNetClient(config);
                if (!ok) {
                    Log.info("[IP Switch] прокси не применён автоматически");
                }
            }
        });

        Log.info("[IP Switch] ядро мода инициализировано.");
    }

    @Override
    public void registerClientCommands(arc.util.CommandHandler handler) {
        commands.register(handler);
    }
}
```

---
