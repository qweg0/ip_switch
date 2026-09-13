```java
package horry.ipswitch;

import arc.files.Fi;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.util.Log;
import arc.util.serialization.Jval;

public class ProxyConfig {

    public static class Proxy {
        public String name;
        public String host;
        public int port;
        public String user = "";
        public String pass = "";
        public boolean enabled;

        public Proxy() {}

        public Proxy(String name, String host, int port, String user, String pass) {
            this.name = name;
            this.host = host;
            this.port = port;
            this.user = user;
            this.pass = pass;
        }

        public String key() {
            return host + ":" + port;
        }
    }

    public final ObjectMap<String, Proxy> proxies = new ObjectMap<>();
    public String activeProxy = null;
    public boolean spoofUuid = false;

    private final Fi file;

    public ProxyConfig(Fi file) {
        this.file = file;
        load();
    }

    public void load() {
        if (!file.exists()) return;
        try {
            Jval root = Jval.read(file.readString());
            if (root.has("active")) activeProxy = root.getString("active", null);
            spoofUuid = root.getBool("spoofUuid", false);
            if (root.has("proxies") && root.get("proxies").isArray()) {
                for (Jval p : root.get("proxies").asArray()) {
                    Proxy pr = new Proxy();
                    pr.name = p.getString("name", "");
                    pr.host = p.getString("host", "");
                    pr.port = p.getInt("port", 1080);
                    pr.user = p.getString("user", "");
                    pr.pass = p.getString("pass", "");
                    pr.enabled = p.getBool("enabled", false);
                    if (!pr.name.isEmpty()) proxies.put(pr.name, pr);
                }
            }
            Log.info("[IP Switch] прокси загружено: @", proxies.size);
        } catch (Exception e) {
            Log.err("[IP Switch] конфиг: @", e.getMessage());
        }
    }

    public void save() {
        Jval root = Jval.newObject();
        root.put("active", activeProxy == null ? "" : activeProxy);
        root.put("spoofUuid", spoofUuid);

        Jval arr = Jval.newArray();
        for (Proxy p : proxies.values()) {
            Jval o = Jval.newObject();
            o.put("name", p.name);
            o.put("host", p.host);
            o.put("port", p.port);
            o.put("user", p.user);
            o.put("pass", p.pass);
            o.put("enabled", p.enabled);
            arr.add(o);
        }
        root.put("proxies", arr);

        file.writeString(root.toString(Jval.Jformat.plain));
        Log.info("[IP Switch] конфиг сохранён");
    }

    public boolean add(String name, String host, int port, String user, String pass) {
        if (name == null || name.isEmpty() || host == null || host.isEmpty()) return false;
        if (proxies.containsKey(name)) return false;
        proxies.put(name, new Proxy(name, host, port, user, pass));
        save();
        return true;
    }

    public boolean remove(String name) {
        if (!proxies.containsKey(name)) return false;
        proxies.remove(name);
        if (name.equals(activeProxy)) activeProxy = null;
        save();
        return true;
    }

    public boolean activate(String name) {
        Proxy p = proxies.get(name);
        if (p == null) return false;
        for (Proxy other : proxies.values()) other.enabled = false;
        p.enabled = true;
        activeProxy = name;
        save();
        return true;
    }

    public void deactivate() {
        for (Proxy p : proxies.values()) p.enabled = false;
        activeProxy = null;
        save();
    }

    public Proxy active() {
        return activeProxy == null ? null : proxies.get(activeProxy);
    }

    public Seq<Proxy> list() {
        return proxies.values().toSeq();
    }
}
```

---
