package com.byd.tools.web;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.Desktop;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 本地 Web 界面启动器，会在 localhost:8910 启动一个轻量级的 HTTP 服务，并在默认浏览器中打开页面。
 */
public class WebUiLauncher {
    private static final Logger LOGGER = LogManager.getLogger(WebUiLauncher.class);
    private static final int PORT = 8910;
    private static final String HOSTNAME = "localhost";
    private static final String RESOURCE_ROOT = "/web";

    private WebUiLauncher() {
    }

    public static void main(String[] args) {
        try {
            new WebUiLauncher().start();
        } catch (IOException e) {
            LOGGER.error("Failed to start web ui server", e);
        }
    }

    private void start() throws IOException {
        InetSocketAddress address = new InetSocketAddress(HOSTNAME, PORT);
        HttpServer server = HttpServer.create(address, 0);
        ExecutorService executor = Executors.newCachedThreadPool();
        server.setExecutor(executor);

        server.createContext("/", new StaticFileHandler());
        server.start();
        LOGGER.info("Fanuc comment web ui started at http://{}:{}", HOSTNAME, PORT);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOGGER.info("Shutting down web ui server...");
            server.stop(0);
            executor.shutdown();
        }));

        openBrowser();
    }

    private void openBrowser() {
        if (!Desktop.isDesktopSupported()) {
            LOGGER.warn("Desktop is not supported, please open http://{}:{} manually.", HOSTNAME, PORT);
            return;
        }
        Desktop desktop = Desktop.getDesktop();
        if (!desktop.isSupported(Desktop.Action.BROWSE)) {
            LOGGER.warn("Browse action is not supported, please open http://{}:{} manually.", HOSTNAME, PORT);
            return;
        }
        try {
            URI uri = new URI(String.format("http://%s:%d", HOSTNAME, PORT));
            desktop.browse(uri);
        } catch (IOException | URISyntaxException e) {
            LOGGER.warn("Failed to open browser automatically, please open http://{}:{} manually.", HOSTNAME, PORT, e);
        }
    }

    private static class StaticFileHandler implements HttpHandler {
        private static final Map<String, String> MIME_TYPES = createMimeTypes();

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String requestPath = exchange.getRequestURI().getPath();
            if (Objects.equals(exchange.getRequestMethod(), "OPTIONS")) {
                handleOptions(exchange);
                return;
            }

            String sanitized = sanitizePath(requestPath);
            String resourcePath = RESOURCE_ROOT + "/" + sanitized;

            try (InputStream resourceStream = getClass().getResourceAsStream(resourcePath)) {
                if (resourceStream == null) {
                    sendNotFound(exchange, requestPath);
                    return;
                }
                byte[] responseBody = resourceStream.readAllBytes();
                String contentType = resolveContentType(resourcePath);
                Headers headers = exchange.getResponseHeaders();
                headers.add("Content-Type", contentType);
                headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
                exchange.sendResponseHeaders(200, responseBody.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(responseBody);
                }
            }
        }

        private void handleOptions(HttpExchange exchange) throws IOException {
            Headers headers = exchange.getResponseHeaders();
            headers.add("Allow", "GET,HEAD,OPTIONS");
            headers.add("Access-Control-Max-Age", String.valueOf(Duration.ofHours(1).toSeconds()));
            exchange.sendResponseHeaders(204, -1);
            exchange.close();
        }

        private String sanitizePath(String requestPath) {
            if (requestPath == null || requestPath.isBlank() || "/".equals(requestPath)) {
                return "index.html";
            }
            String trimmed = requestPath.startsWith("/") ? requestPath.substring(1) : requestPath;
            Path normalized = Path.of(trimmed).normalize();
            if (normalized.startsWith("..")) {
                return "index.html";
            }
            return normalized.toString().replace('\\', '/');
        }

        private void sendNotFound(HttpExchange exchange, String path) throws IOException {
            String message = String.format("资源未找到: %s", path);
            byte[] data = message.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "text/plain; charset=utf-8");
            exchange.sendResponseHeaders(404, data.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(data);
            }
        }

        private String resolveContentType(String resourcePath) {
            int idx = resourcePath.lastIndexOf('.');
            if (idx == -1 || idx == resourcePath.length() - 1) {
                return "text/plain; charset=utf-8";
            }
            String extension = resourcePath.substring(idx + 1).toLowerCase();
            return MIME_TYPES.getOrDefault(extension, "text/plain; charset=utf-8");
        }

        private static Map<String, String> createMimeTypes() {
            Map<String, String> map = new HashMap<>();
            map.put("html", "text/html; charset=utf-8");
            map.put("css", "text/css; charset=utf-8");
            map.put("js", "application/javascript; charset=utf-8");
            map.put("json", "application/json; charset=utf-8");
            map.put("png", "image/png");
            map.put("jpg", "image/jpeg");
            map.put("jpeg", "image/jpeg");
            map.put("svg", "image/svg+xml");
            map.put("ico", "image/x-icon");
            return map;
        }
    }
}
