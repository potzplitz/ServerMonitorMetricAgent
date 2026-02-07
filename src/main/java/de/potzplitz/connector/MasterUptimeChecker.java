package de.potzplitz.connector;

import de.potzplitz.core.Job;
import de.potzplitz.var.MasterInfo;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class MasterUptimeChecker implements Job {

    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(3))
            .build();

    @Override
    public String name() {
        return "MasterUptimeChecker";
    }

    @Override
    public void runOnce() throws IOException {
        System.out.println("CheckUptime");

        String baseUrl = MasterInfo.getMasterBaseUrl();
        if (baseUrl == null) throw new IOException("No master base URL known");

        String url = baseUrl + "/agent/uptime";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(3))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString("{\"status\":\"alive\"}"))
                .build();

        try {
            HttpResponse<Void> resp = CLIENT.send(request, HttpResponse.BodyHandlers.discarding());
            if (resp.statusCode() != 200) {
                throw new IOException("Uptime ping failed, HTTP " + resp.statusCode() + " @ " + url);
            }
        } catch (java.net.ConnectException e) {
            throw new IOException("Cannot connect to master @ " + url, e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Uptime ping interrupted @ " + url, e);
        }
    }
}
