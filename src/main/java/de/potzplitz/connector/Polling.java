package de.potzplitz.connector;

import de.potzplitz.core.Job;
import de.potzplitz.metric.Metrics;

import java.io.IOException;

public class Polling implements Job {

    private Metrics metrics = new Metrics();

    @Override
    public String name() {
        return "Polling";
    }

    @Override
    public void runOnce() throws IOException {
        System.out.println(metrics.returnData().toString(2));
    }
}
