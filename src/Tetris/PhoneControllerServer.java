package Tetris;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.concurrent.Executors;

public class PhoneControllerServer {

    private static final long CONNECTION_TIMEOUT_MS = 2500;
    private static final long LINE_EVENT_VISIBLE_MS = 1400;

    private final PlayManager pm;
    private HttpServer server;

    private volatile long lastPingTime = 0;
    private volatile boolean connected = false;
    private volatile String accessUrl = "Starting...";
    private volatile String clientIp = "-";

    private int leftPressCount = 0;
    private int rightPressCount = 0;
    private int downPressCount = 0;
    private int rotatePressCount = 0;
    private int pauseToggleCount = 0;
    private int restartPressCount = 0;
    private int nextLevelPressCount = 0;
    private int replayLevelPressCount = 0;

    private volatile String latestUiEventType = "none";
    private volatile long latestUiEventId = 0;

    public PhoneControllerServer(PlayManager pm) {
        this.pm = pm;
    }

    public void start() {
        try {
            server = HttpServer.create(new InetSocketAddress(8080), 0);
            server.setExecutor(Executors.newCachedThreadPool());

            server.createContext("/", this::handleRoot);
            server.createContext("/style.css", this::handleStyle);
            server.createContext("/ControllerApp.js", this::handleScript);
            server.createContext("/ping", this::handlePing);
            server.createContext("/tap", this::handleTap);
            server.createContext("/volume", this::handleVolume);
            server.createContext("/event", this::handleEvent);

            server.start();

            String ip = findLocalIPv4();
            int port = server.getAddress().getPort();
            accessUrl = "http://" + ip + ":" + port;

            System.out.println("Phone Controller started " + accessUrl);

            startConnectionMonitor();

        } catch (IOException e) {
            e.printStackTrace();
            accessUrl = "Server failed";
        }
    }

    private String findLocalIPv4() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();

            while (interfaces.hasMoreElements()) {
                NetworkInterface ni = interfaces.nextElement();

                if (!ni.isUp() || ni.isLoopback() || ni.isVirtual()) {
                    continue;
                }

                String displayName = ni.getDisplayName() == null ? "" : ni.getDisplayName().toLowerCase();
                String name = ni.getName() == null ? "" : ni.getName().toLowerCase();

                boolean looksLikeWifi = displayName.contains("wi-fi") ||
                        displayName.contains("wifi") ||
                        displayName.contains("wireless") ||
                        name.contains("wlan");

                if (!looksLikeWifi) {
                    continue;
                }

                Enumeration<InetAddress> addresses = ni.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();

                    if (addr instanceof Inet4Address && !addr.isLoopbackAddress()) {
                        return addr.getHostAddress();
                    }
                }
            }

            interfaces = NetworkInterface.getNetworkInterfaces();

            while (interfaces.hasMoreElements()) {
                NetworkInterface ni = interfaces.nextElement();

                if (!ni.isUp() || ni.isLoopback() || ni.isVirtual()) {
                    continue;
                }

                Enumeration<InetAddress> addresses = ni.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();

                    if (addr instanceof Inet4Address && !addr.isLoopbackAddress()) {
                        return addr.getHostAddress();
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return "127.0.0.1";
    }

    private void writeResponse(HttpExchange exchange, String contentType, String body) throws IOException {
        byte[] response = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.sendResponseHeaders(200, response.length);

        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response);
        }
    }

    private String getParam(String query, String key) {
        if (query == null || query.isEmpty()) {
            return null;
        }

        String[] pairs = query.split("&");

        for (String pair : pairs) {
            int idx = pair.indexOf('=');
            if (idx <= 0) {
                continue;
            }

            String k = pair.substring(0, idx);
            String v = pair.substring(idx + 1);

            if (k.equals(key)) {
                return v;
            }
        }

        return null;
    }

    private void markClientSeen(HttpExchange exchange) {
        lastPingTime = System.currentTimeMillis();
        connected = true;

        InetAddress remoteAddress = exchange.getRemoteAddress().getAddress();
        if (remoteAddress != null) {
            clientIp = remoteAddress.getHostAddress();
        }
    }

    private synchronized void queueLeftPress() {
        leftPressCount++;
    }

    private synchronized void queueRightPress() {
        rightPressCount++;
    }

    private synchronized void queueDownPress() {
        downPressCount++;
    }

    private synchronized void queueRotatePress() {
        rotatePressCount++;
    }

    private synchronized void queuePauseToggle() {
        pauseToggleCount++;
    }

    private synchronized void queueRestartPress() {
        restartPressCount++;
    }

    private synchronized void queueNextLevelPress() {
        nextLevelPressCount++;
    }

    private synchronized void queueReplayLevelPress() {
        replayLevelPressCount++;
    }

    public synchronized boolean consumeLeftPress() {
        if (leftPressCount > 0) {
            leftPressCount--;
            return true;
        }
        return false;
    }

    public synchronized boolean consumeRightPress() {
        if (rightPressCount > 0) {
            rightPressCount--;
            return true;
        }
        return false;
    }

    public synchronized boolean consumeDownPress() {
        if (downPressCount > 0) {
            downPressCount--;
            return true;
        }
        return false;
    }

    public synchronized boolean consumeRotatePress() {
        if (rotatePressCount > 0) {
            rotatePressCount--;
            return true;
        }
        return false;
    }

    public synchronized boolean consumePauseToggle() {
        if (pauseToggleCount > 0) {
            pauseToggleCount--;
            return true;
        }
        return false;
    }

    public synchronized boolean consumeRestartPress() {
        if (restartPressCount > 0) {
            restartPressCount--;
            return true;
        }
        return false;
    }

    public synchronized boolean consumeNextLevelPress() {
        if (nextLevelPressCount > 0) {
            nextLevelPressCount--;
            return true;
        }
        return false;
    }

    public synchronized boolean consumeReplayLevelPress() {
        if (replayLevelPressCount > 0) {
            replayLevelPressCount--;
            return true;
        }
        return false;
    }

    private synchronized void clearUiEventInternal() {
        latestUiEventType = "none";
        latestUiEventId = System.currentTimeMillis();
    }

    public synchronized void pushUiEvent(String type) {
        latestUiEventType = type;
        latestUiEventId = System.currentTimeMillis();
        long pushedId = latestUiEventId;

        if ("line".equals(type)) {
            Thread clearThread = new Thread(() -> {
                try {
                    Thread.sleep(LINE_EVENT_VISIBLE_MS);
                } catch (InterruptedException ignored) {
                }

                synchronized (PhoneControllerServer.this) {
                    if (latestUiEventId == pushedId && "line".equals(latestUiEventType)) {
                        clearUiEventInternal();
                    }
                }
            });
            clearThread.setDaemon(true);
            clearThread.start();
        }
    }

    public synchronized void clearUiEvent() {
        clearUiEventInternal();
    }

    public boolean isPhoneConnected() {
        return connected;
    }

    public String getAccessUrl() {
        return accessUrl;
    }

    public String getClientIp() {
        return clientIp;
    }

    private void handleRoot(HttpExchange exchange) throws IOException {
        markClientSeen(exchange);
        writeResponse(exchange, "text/html; charset=UTF-8", getControllerHTML());
    }

    private void handleStyle(HttpExchange exchange) throws IOException {
        String css = "";
        try {
            css = java.nio.file.Files.readString(java.nio.file.Paths.get("Client/style.css"));
        } catch (Exception e) {
        }
        writeResponse(exchange, "text/css; charset=UTF-8", css);
    }

    private void handleScript(HttpExchange exchange) throws IOException {
        String js = "";
        try {
            js = java.nio.file.Files.readString(java.nio.file.Paths.get("Client/ControllerApp.js"));
        } catch (Exception e) {
        }
        writeResponse(exchange, "application/javascript; charset=UTF-8", js);
    }

    private void handlePing(HttpExchange exchange) throws IOException {
        markClientSeen(exchange);
        writeResponse(exchange, "text/plain; charset=UTF-8", "OK");
    }

    private void handleTap(HttpExchange exchange) throws IOException {
        markClientSeen(exchange);
        String cmd = getParam(exchange.getRequestURI().getQuery(), "c");

        if ("LEFT".equals(cmd)) {
            queueLeftPress();
        } else if ("RIGHT".equals(cmd)) {
            queueRightPress();
        } else if ("DOWN".equals(cmd)) {
            queueDownPress();
        } else if ("ROTATE".equals(cmd)) {
            queueRotatePress();
        } else if ("PAUSE_TOGGLE".equals(cmd)) {
            queuePauseToggle();
        } else if ("RESTART".equals(cmd)) {
            queueRestartPress();
            clearUiEvent();
        } else if ("NEXT_LEVEL".equals(cmd)) {
            queueNextLevelPress();
            clearUiEvent();
        } else if ("REPLAY_LEVEL".equals(cmd)) {
            queueReplayLevelPress();
            clearUiEvent();
        }

        writeResponse(exchange, "text/plain; charset=UTF-8", "OK");
    }

    private void handleVolume(HttpExchange exchange) throws IOException {
        markClientSeen(exchange);
        String value = getParam(exchange.getRequestURI().getQuery(), "v");

        if (value != null) {
            try {
                int intValue = Integer.parseInt(value);
                float normalized = Math.max(0, Math.min(100, intValue)) / 100.0f;
                GamePanel.music.setVolume(normalized);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        writeResponse(exchange, "text/plain; charset=UTF-8", "OK");
    }

    private void handleEvent(HttpExchange exchange) throws IOException {
        markClientSeen(exchange);
        String json = "{\"type\":\"" + latestUiEventType + "\",\"id\":" + latestUiEventId +
                ",\"score\":" + pm.getScore() + "}";
        writeResponse(exchange, "application/json; charset=UTF-8", json);
    }

    private void startConnectionMonitor() {
        Thread monitorThread = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(500);

                    long now = System.currentTimeMillis();

                    if (connected && now - lastPingTime > CONNECTION_TIMEOUT_MS) {
                        connected = false;
                        clientIp = "-";

                        synchronized (PhoneControllerServer.this) {
                            if (!"level".equals(latestUiEventType) && !"gameover".equals(latestUiEventType)) {
                                clearUiEventInternal();
                            }
                        }
                    }

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        monitorThread.setDaemon(true);
        monitorThread.start();
    }

    private String getControllerHTML() {
        try {
            return java.nio.file.Files.readString(java.nio.file.Paths.get("Client/index.html"));
        } catch (Exception e) {
            return "<h1>Error. Cant find index.html in Client!</h1>"
                    + e.getMessage();
        }
    }
}
