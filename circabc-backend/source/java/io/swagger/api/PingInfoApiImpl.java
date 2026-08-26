package io.swagger.api;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Duration;
import java.util.UUID;

import io.swagger.model.PingInfo;

/**
 * @author Alain Morlet
 */
public class PingInfoApiImpl implements PingInfoApi {

    private final String instanceId = UUID.randomUUID().toString();
    private final long startTime = System.currentTimeMillis();

    /**
     * Get the ping information of the server.
     *
     * @return PingInfo object containing server details.
     */
    public PingInfo getPingInfo() throws UnknownHostException {
        InetAddress addr = InetAddress.getLocalHost();

        String hostname = addr.getHostName();
        String ip = addr.getHostAddress();

        Runtime runtime = Runtime.getRuntime();
        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        long uptimeMs = runtimeMXBean.getUptime();

        long totalMemory = runtime.totalMemory() / (1024 * 1024); // Convert to MB
        long freeMemory = runtime.freeMemory() / (1024 * 1024); // Convert to MB
        long usedMemory = totalMemory - freeMemory;

        PingInfo info = new PingInfo();
        info.setHostname(hostname);
        info.setIp(ip);
        info.setInstanceId(instanceId);
        info.setStartTime(startTime);
        info.setUptime(formatDuration(uptimeMs));
        info.setUptimeMs(uptimeMs);
        info.setProcessId(getProcessId());
        info.setMemoryTotalMB(totalMemory);
        info.setMemoryFreeMB(freeMemory);
        info.setMemoryUsedMB(usedMemory);

        return info;
    }

    private String formatDuration(long millis) {
        Duration duration = Duration.ofMillis(millis);
        return String.format("%d days, %02d:%02d:%02d",
                duration.toDays(),
                duration.toHours(),
                duration.toMinutes(),
                duration.toMillis() * 1000 % 60); // Convert to seconds
    }

    private String getProcessId() {
        String name = ManagementFactory.getRuntimeMXBean().getName();
        return name.split("@")[0]; // PID@hostname
    }
}
