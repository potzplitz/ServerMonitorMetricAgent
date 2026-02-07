package de.potzplitz.var;

public class MasterInfo {
    private static volatile String masterBaseUrl;
    private static volatile boolean hasMaster = false;
    private static volatile boolean backendAlive = false;

    private static final Object lock = new Object();

    public static void setMasterBaseUrl(String url) { masterBaseUrl = url; }
    public static String getMasterBaseUrl() { return masterBaseUrl; }

    public static void setHasMaster(boolean v) {
        hasMaster = v;
        synchronized (lock) { lock.notifyAll(); }
    }
    public static boolean hasMaster() { return hasMaster; }

    public static void setBackendAlive(boolean v) { backendAlive = v; }
    public static boolean isBackendAlive() { return backendAlive; }

    public static void awaitMaster() throws InterruptedException {
        synchronized (lock) {
            while (!hasMaster) lock.wait();
        }
    }

    public static void resetMaster() {
        hasMaster = false;
        backendAlive = false;
        masterBaseUrl = null;
    }
}
