package de.potzplitz;

public class Main {
    public static void main(String[] args) {
        // Plan:
        // spring backend pingt bei hochfahren alle agents
        // durch ping wird ip von backend announced welche die agents nutzen für polling
        // durch ping werden agents aktiviert und sammeln daten und pollen es an spring backend
        // daten werden in datenbank auf nas gespeichert und später dann in webgui angezeigt
    }
}