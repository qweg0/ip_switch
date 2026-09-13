```java
package horry.ipswitch;

import arc.util.Log;

import java.lang.reflect.Field;
import java.net.Socket;

public class SocketHook {

    public static boolean install(ProxyConfig cfg) {
        ProxyConfig.Proxy p = cfg.active();
        if (p == null) {
            Log.info("[IP Switch] прокси не активен — обычный IP");
            return false;
        }
        Log.info("[IP Switch] активный прокси: @ (spoofUuid=@)", p.name, cfg.spoofUuid);
        return true;
    }

    public static boolean patchNetClient(ProxyConfig cfg) {
        ProxyConfig.Proxy p = cfg.active();
        if (p == null) return false;

        try {
            Object netClient = mindustry.Vars.netClient;
            if (netClient == null) return false;

            String host = null;
            int port = 6567;

            for (String fn : new String[]{"connect", "host", "address", "ip"}) {
                try {
                    Field f = netClient.getClass().getDeclaredField(fn);
                    f.setAccessible(true);
                    Object v = f.get(netClient);
                    if (v instanceof String s && !s.isEmpty()) {
                        host = s;
                        break;
                    }
                } catch (NoSuchFieldException ignored) {}
            }

            if (host == null) {
                Log.info("[IP Switch] host NetClient не найден — прокси не применён");
                return false;
            }

            for (String fn : new String[]{"port", "serverPort"}) {
                try {
                    Field f = netClient.getClass().getDeclaredField(fn);
                    f.setAccessible(true);
                    Object v = f.get(netClient);
                    if (v instanceof Integer i) { port = i; break; }
                } catch (NoSuchFieldException ignored) {}
            }

            Log.info("[IP Switch] цель: @:@ через @", host, port, p.name);

            Socket proxied = SocksConnector.connect(p, host, port, 15000);
            boolean swapped = trySwapSocket(netClient, proxied);
            if (!swapped) {
                Log.info("[IP Switch] прямая подмена сокета невозможна на этой сборке");
                proxied.close();
                return false;
            }

            return true;
        } catch (Throwable t) {
            Log.err("[IP Switch] patchNetClient: @", t.getMessage());
            return false;
        }
    }

    private static boolean trySwapSocket(Object netClient, Socket sock) {
        for (String fn : new String[]{"socket", "sock", "connection", "tcp", "netSocket"}) {
            try {
                Field f = netClient.getClass().getDeclaredField(fn);
                f.setAccessible(true);
                f.set(netClient, sock);
                Log.info("[IP Switch] сокет подменён через поле '@'", fn);
                return true;
            } catch (NoSuchFieldException ignored) {
            } catch (Throwable t) {
                Log.err("[IP Switch] swap '@': @", fn, t.getMessage());
            }
        }
        return false;
    }
}
```

---
