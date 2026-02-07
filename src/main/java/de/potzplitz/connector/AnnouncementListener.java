package de.potzplitz.connector;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import de.potzplitz.var.MasterInfo;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

public class AnnouncementListener {
    private static HttpServer server;

    public static void startListening(int port, Runnable onAnnouncement) throws IOException {
        if (server != null) return;

        server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/announcement", new RegisterHandler(onAnnouncement));
        server.setExecutor(null);
        server.start();

        System.out.println("Agent listening on port " + port);
    }

    static class RegisterHandler implements HttpHandler {
        private final Runnable onAnnouncement;

        RegisterHandler(Runnable onAnnouncement) {
            this.onAnnouncement = onAnnouncement;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }

            String clientIp = exchange.getRemoteAddress().getAddress().getHostAddress();
            MasterInfo.setMasterBaseUrl("http://" + clientIp + ":8080");
            MasterInfo.setHasMaster(true);

            byte[] response = "OK".getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, response.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response);
            }

            if (onAnnouncement != null) onAnnouncement.run();
        }
    }
}

