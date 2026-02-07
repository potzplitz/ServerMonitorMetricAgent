package de.potzplitz.metric;

import com.sun.management.OperatingSystemMXBean;
import org.json.JSONObject;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.hardware.Sensors;
import oshi.software.os.OSFileStore;

import oshi.software.os.OperatingSystem;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;

public class Metrics implements MetricFormat {
    private SystemInfo system = new SystemInfo();
    private JSONObject metrics = new JSONObject();
    @Override
    public JSONObject returnData() {
        getSystemMetrics();
        return this.metrics;
    }

    private volatile double lastCpuLoad = Double.NaN;

    private double[][] getCpuLoad() {
        OperatingSystemMXBean os =
                (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();

        double sys = os.getSystemCpuLoad(); // 0..1 oder -1

        if (sys >= 0 && !Double.isNaN(sys) && !Double.isInfinite(sys)) {
            lastCpuLoad = sys;
        }

        double out = Double.isNaN(lastCpuLoad) ? 0.0 : lastCpuLoad;
        return new double[][] { new double[] { out } };
    }


    private long[] getRamLoad() {
        GlobalMemory mem = system.getHardware().getMemory();
        long total = mem.getTotal();
        long available = mem.getAvailable();
        long used = total - available;

        long[] ramdata = {total, available, used};

        return ramdata;
    }

    private long[] getDiskData() {
        long usable = 0;
        long total = 0;

        OperatingSystem os = system.getOperatingSystem();
        for (OSFileStore fs : os.getFileSystem().getFileStores()) {
            usable += fs.getUsableSpace();
            total  += fs.getTotalSpace();
        }

        long used = total - usable;

        return new long[] { usable, total, used };
    }


    private JSONObject getTempData() {
        Sensors sensors = system.getHardware().getSensors();

        double cpuTempC = sensors.getCpuTemperature();
        int[] fanRpm = sensors.getFanSpeeds();
        double cpuVolt = sensors.getCpuVoltage();

        return new JSONObject()
                .put("cpuTempC", cpuTempC)
                .put("fanRpm", fanRpm)
                .put("cpuVoltage", cpuVolt);
    }


    private void getSystemMetrics() {

        // CPU
        double[][] cpu = getCpuLoad();
        JSONObject cpuJson = new JSONObject()
                .put("total", cpu[0][0])
                .put("totalPercent", cpu[0][0] * 100.0);

        // RAM
        long[] ram = getRamLoad(); // total, available, used
        JSONObject ramJson = new JSONObject()
                .put("totalBytes", ram[0])
                .put("availableBytes", ram[1])
                .put("usedBytes", ram[2])
                .put("usedPercent", ram[0] == 0 ? 0 : (ram[2] * 100.0 / ram[0]));

        // Disk
        long[] disk = getDiskData(); // usable, total, used
        JSONObject diskJson = new JSONObject()
                .put("usableBytes", disk[0])
                .put("totalBytes", disk[1])
                .put("usedBytes", disk[2])
                .put("usedPercent", disk[1] == 0 ? 0 : (disk[2] * 100.0 / disk[1]));

        // Meta
        OperatingSystem os = system.getOperatingSystem();
        JSONObject metaJson = new JSONObject()
                .put("timestamp", System.currentTimeMillis())
                .put("hostname", os.getNetworkParams().getHostName())
                .put("os", os.toString());

        //Temps
        JSONObject tempJson = getTempData();

        this.metrics = new JSONObject()
                .put("meta", metaJson)
                .put("cpu", cpuJson)
                .put("ram", ramJson)
                .put("disk", diskJson)
                .put("temp", tempJson);

    }
}
