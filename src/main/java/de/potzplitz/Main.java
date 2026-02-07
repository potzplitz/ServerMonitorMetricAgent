package de.potzplitz;

import de.potzplitz.connector.AnnouncementListener;
import de.potzplitz.connector.MasterUptimeChecker;
import de.potzplitz.core.AgentRuntime;
import de.potzplitz.core.Scheduler;
import de.potzplitz.metric.Metrics;

import java.io.IOException;
import java.time.Duration;

public class Main {
    public static void main(String[] args) throws Exception {
        new AgentRuntime().start();
    }
}

// Plan:
// spring backend pingt bei hochfahren alle agents
// durch ping wird ip von backend announced welche die agents nutzen für polling
// durch ping werden agents aktiviert und sammeln daten und pollen es an spring backend
// daten werden in datenbank auf nas gespeichert und später dann in webgui angezeigt