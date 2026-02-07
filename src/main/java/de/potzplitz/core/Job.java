package de.potzplitz.core;

import java.io.IOException;

public interface Job {
    String name();
    void runOnce() throws IOException;
}
