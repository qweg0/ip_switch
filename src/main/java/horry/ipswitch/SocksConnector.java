```java
package horry.ipswitch;

import arc.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class SocksConnector {

    public static Socket connect(ProxyConfig.Proxy proxy, String targetHost, int targetPort, int timeoutMs) throws IOException {
        Socket sock = new Socket();
        sock.connect(new InetSocketAddress(proxy.host, proxy.port), timeoutMs);
        sock.setSoTimeout(timeoutMs);

        InputStream in = sock.getInputStream();
        OutputStream out = sock.getOutputStream();

        boolean auth = proxy.user != null && !proxy.user.isEmpty();

        out.write(auth ? new byte[]{0x05, 0x02, 0x00, 0x02} : new byte[]{0x05, 0x01, 0x00});
        out.flush();

        byte[] resp = readN(in, 2);
        if (resp[0] != 0x05) throw new IOException("SOCKS5: bad version " + resp[0]);
        int method = resp[1] & 0xFF;

        if (method == 0x02) {
            byte[] u = proxy.user.getBytes(StandardCharsets.UTF_8);
            byte[] p = (proxy.pass == null ? "" : proxy.pass).getBytes(StandardCharsets.UTF_8);
            byte[] authReq = new byte[3 + u.length + p.length];
            authReq[0] = 0x01;
            authReq[1] = (byte) u.length;
            System.arraycopy(u, 0, authReq, 2, u.length);
            authReq[2 + u.length] = (byte) p.length;
            System.arraycopy(p, 0, authReq, 3 + u.length, p.length);
            out.write(authReq);
            out.flush();

            byte[] authResp = readN(in, 2);
            if (authResp[1] != 0x00) throw new IOException("SOCKS5: auth failed");
        } else if (method != 0x00) {
            throw new IOException("SOCKS5: no acceptable method (" + method + ")");
        }

        byte[] hostBytes = targetHost.getBytes(StandardCharsets.UTF_8);
        byte[] req = new byte[7 + hostBytes.length];
        req[0] = 0x05;
        req[1] = 0x01;
        req[2] = 0x00;
        req[3] = 0x03;
        req[4] = (byte) hostBytes.length;
        System.arraycopy(hostBytes, 0, req, 5, hostBytes.length);
        req[5 + hostBytes.length] = (byte) ((targetPort >> 8) & 0xFF);
        req[6 + hostBytes.length] = (byte) (targetPort & 0xFF);
        out.write(req);
        out.flush();

        byte[] head = readN(in, 4);
        if (head[1] != 0x00) {
            int code = head[1] & 0xFF;
            throw new IOException("SOCKS5: connect refused, code=" + code);
        }

        int atyp = head[3] & 0xFF;
        switch (atyp) {
            case 0x01: readN(in, 4 + 2); break;
            case 0x03: {
                int len = readN(in, 1)[0] & 0xFF;
                readN(in, len + 2);
                break;
            }
            case 0x04: readN(in, 16 + 2); break;
            default: throw new IOException("SOCKS5: bad ATYP " + atyp);
        }

        Log.info("[IP Switch] SOCKS5 ok: @ -> @:@", proxy.name, targetHost, targetPort);
        sock.setSoTimeout(0);
        return sock;
    }

    private static byte[] readN(InputStream in, int n) throws IOException {
        byte[] buf = new byte[n];
        int off = 0;
        while (off < n) {
            int r = in.read(buf, off, n - off);
            if (r < 0) throw new IOException("SOCKS5: stream closed");
            off += r;
        }
        return buf;
    }
}
```

---
