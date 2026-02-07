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


        String baseUrl = MasterInfo.getMasterBaseUrl();
        if (baseUrl == null) throw new IOException("No master base URL known");

        String url = baseUrl + "/agent/uptime";
        System.out.println(url);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(3))
                .GET()
                .build();

        try {
            HttpResponse<Void> resp = CLIENT.send(request, HttpResponse.BodyHandlers.discarding());

            int sc = resp.statusCode();
            if (sc < 200 || sc >= 300) {
                MasterInfo.setBackendAlive(false);
                throw new IOException("Uptime ping failed, HTTP " + sc + " @ " + url);

            }

        } catch (java.net.ConnectException e) {
            MasterInfo.setBackendAlive(false);
            throw new IOException("Cannot connect to master @ " + url, e);
        } catch (InterruptedException e) {
            MasterInfo.setBackendAlive(false);
            Thread.currentThread().interrupt();
            throw new IOException("Uptime ping interrupted @ " + url, e);
        }
    }
}
