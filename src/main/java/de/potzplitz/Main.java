package de.potzplitz;

import de.potzplitz.connector.AnnouncementListener;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {

        AnnouncementListener.startListening(9090);
        // Plan:
        // spring backend pingt bei hochfahren alle agents
        // durch ping wird ip von backend announced welche die agents nutzen für polling
        // durch ping werden agents aktiviert und sammeln daten und pollen es an spring backend
        // daten werden in datenbank auf nas gespeichert und später dann in webgui angezeigt
    }
}