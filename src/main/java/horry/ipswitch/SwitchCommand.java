src/main/java/horry/ipswitch/SwitchCommand.java

```java
package horry.ipswitch;

import arc.util.CommandHandler;
import arc.util.Log;
import mindustry.gen.Player;

public class SwitchCommand {

    private final ProxyConfig cfg;

    public SwitchCommand(ProxyConfig cfg) {
        this.cfg = cfg;
    }

    public void register(CommandHandler handler) {
        handler.register("ipsw", "<add|del|use|off|list|show|uuid> [args...]",
            "Управление прокси-профилями и UUID", args -> {
            if (args.length == 0) {
                reply("ipsw: add <name> <host> <port> [user] [pass] | del <name> | use <name> | off | list | show | uuid");
                return;
            }

            String sub = args[0].toLowerCase();
            switch (sub) {
                case "add": {
                    if (args.length < 4) { reply("ipsw add <name> <host> <port> [user] [pass]"); return; }
                    int port;
                    try { port = Integer.parseInt(args[3]); }
                    catch (NumberFormatException e) { reply("порт — число"); return; }
                    String user = args.length > 4 ? args[4] : "";
                    String pass = args.length > 5 ? args[5] : "";
                    boolean ok = cfg.add(args[1], args[2], port, user, pass);
                    reply(ok ? "прокси добавлен: " + args[1] : "имя занято или пустое");
                    break;
                }
                case "del": {
                    if (args.length < 2) { reply("ipsw del <name>"); return; }
                    reply(cfg.remove(args[1]) ? "удалён: " + args[1] : "нет такого");
                    break;
                }
                case "use": {
                    if (args.length < 2) { reply("ipsw use <name>"); return; }
                    boolean ok = cfg.activate(args[1]);
                    if (ok) {
                        SocketHook.install(cfg);
                        reply("активен прокси: " + args[1]);
                    } else {
                        reply("нет такого профиля");
                    }
                    break;
                }
                case "off": {
                    cfg.deactivate();
                    reply("прокси выключен. обычный IP");
                    break;
                }
                case "list": {
                    if (cfg.proxies.isEmpty()) { reply("прокси нет"); return; }
                    StringBuilder sb = new StringBuilder("прокси:\n");
                    for (ProxyConfig.Proxy p : cfg.list()) {
                        sb.append(p.enabled ? " * " : "   ")
                          .append(p.name).append(" : ")
                          .append(p.host).append(':').append(p.port);
                        if (!p.user.isEmpty()) sb.append(" (auth)");
                        sb.append('\n');
                    }
                    reply(sb.toString());
                    break;
                }
                case "show": {
                    ProxyConfig.Proxy p = cfg.active();
                    reply(p == null ? "активного прокси нет"
                                    : "активен: " + p.name + " " + p.host + ":" + p.port
                                      + " | spoofUuid=" + cfg.spoofUuid);
                    break;
                }
                case "uuid": {
                    cfg.spoofUuid = !cfg.spoofUuid;
                    cfg.save();
                    if (cfg.spoofUuid) {
                        String u = UuidSwapper.rotate();
                        reply("spoofUuid ВКЛ. текущий: " + u);
                    } else {
                        UuidSwapper.reset();
                        reply("spoofUuid ВЫКЛ");
                    }
                    break;
                }
                default:
                    reply("неизвестно: " + sub);
            }
        });
    }

    private void reply(String msg) {
        Log.info("[IP Switch] @", msg);
        try {
            Class<?> ui = Class.forName("mindustry.ui.fragments.ChatFragment");
            Object instance = ui.getField("instance").get(null);
            if (instance != null) {
                ui.getMethod("addMessage", String.class, Player.class, String.class)
                  .invoke(instance, msg, null, null);
            }
        } catch (Throwable ignored) {}
    }
}
```

---
