package de.potzplitz.connector;

import de.potzplitz.core.Job;
import de.potzplitz.metric.Metrics;
import de.potzplitz.var.MasterInfo;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class Polling implements Job {

    private final Metrics metrics = new Metrics();

    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(3))
            .build();

    @Override
    public String name() {
        return "Polling";
    }

    @Override
    public void runOnce() throws IOException {
        // Ziel-URL (MasterInfo soll sowas wie http://192.168.178.44:8080 liefern)
        String baseUrl = MasterInfo.getMasterBaseUrl();
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new IOException("No master base url set (MasterInfo.getMasterBaseUrl() is null/blank)");
        }

        String url = baseUrl + "/agent/report";

        // Metrics als JSON-String
        String json = metrics.returnData().toString(); // <- "raw" JSON string

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(5))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        try {
            HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());

            if (res.statusCode() / 100 != 2) {
                throw new IOException("Polling failed: HTTP " + res.statusCode() + " -> " + res.body());
            }

            System.out.println("[polling] sent metrics -> HTTP " + res.statusCode());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Polling interrupted", e);
        } catch (IllegalArgumentException e) {
            // z.B. wenn URI kaputt ist
            throw new IOException("Invalid URL: " + url, e);
        }
    }
}
