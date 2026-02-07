package de.potzplitz.core;

import de.potzplitz.connector.AnnouncementListener;
import de.potzplitz.connector.MasterUptimeChecker;
import de.potzplitz.connector.Polling;
import de.potzplitz.metric.Metrics;
import de.potzplitz.var.MasterInfo;

import java.time.Duration;

public class AgentRuntime {

    private final Scheduler scheduler = new Scheduler(2);

    private volatile boolean jobsRunning = false;

    public void start() throws Exception {
        scheduler.start();

        AnnouncementListener.startListening(9090, () -> {
            MasterInfo.setBackendAlive(true);
            System.out.println("[agent] Announcement received, master=" + MasterInfo.getMasterBaseUrl());
        });

        while (true) {
            System.out.println("[agent] Waiting for master announcement...");
            MasterInfo.awaitMaster();

            startJobsIfNotRunning();

            while (MasterInfo.hasMaster()) {
                Thread.sleep(1000);

                if (!MasterInfo.isBackendAlive()) {

                    System.out.println("[agent] Backend DOWN -> stopping jobs and waiting for re-announce");
                    stopJobsIfRunning();
                    MasterInfo.resetMaster();
                    break;
                }
            }
        }
    }

    private void startJobsIfNotRunning() {
        if (jobsRunning) return;

        scheduler.scheduleFixedDelay(new Polling(),
                Duration.ofSeconds(1), Duration.ofSeconds(5));

        scheduler.scheduleFixedDelay(new MasterUptimeChecker(),
                Duration.ofSeconds(1), Duration.ofSeconds(30));

        jobsRunning = true;
        System.out.println("[agent] Jobs scheduled");
    }

    private void stopJobsIfRunning() {
        if (!jobsRunning) return;

        scheduler.cancel("Polling");
        scheduler.cancel("MasterUptimeChecker");

        jobsRunning = false;
        System.out.println("[agent] Jobs cancelled");
    }
}

