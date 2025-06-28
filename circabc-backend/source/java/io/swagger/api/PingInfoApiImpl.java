package io.swagger.api;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Duration;
import java.util.UUID;

import eu.cec.digit.circabc.service.user.UserService;
import io.swagger. model.PingInfo;

public class PingInfoApiImpl implements PingInfoApi {

    private final String instanceId = UUID.randomUUID().toString();
    private final long startTime = System.currentTimeMillis();

    private UserService userService;

    public void setUserService(UserService userService) {
        this.userService = userService;
    }


    public PingInfo getPingInfo() throws UnknownHostException {
        InetAddress addr = InetAddress.getLocalHost();
        String hostname = addr.getHostName();
        String ip = addr.getHostAddress();

        Runtime runtime = Runtime.getRuntime();
        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        long uptimeMs = runtimeMXBean.getUptime();

        long totalMemory = runtime.totalMemory() / (1024 * 1024); // in MB
        long freeMemory = runtime.freeMemory() / (1024 * 1024);   // in MB
        long usedMemory = totalMemory - freeMemory;

        PingInfo.MemoryInfo memory = new  PingInfo.MemoryInfo();
        memory.setUsed((double) usedMemory);
        memory.setFree((double) freeMemory);
        memory.setMax((double) totalMemory);

        PingInfo.CpuInfo cpu = new  PingInfo.CpuInfo();
        cpu.setUsage(getCpuLoad());
        cpu.setCores(Runtime.getRuntime().availableProcessors());

        PingInfo info = new PingInfo();
        info.setHostname(hostname);
        info.setIp(ip);
        info.setInstanceId(instanceId);
        info.setStartTime(String.valueOf(startTime));
        info.setUpTime(formatDuration(uptimeMs));
        info.setStatus(eu.cec.digit.circabc.web.servlet.ControlServlet.ServerState.isActivated() ? "ACTIVATED" : "DEACTIVATED");
        info.setMemory(memory);
        info.setCpu(cpu);
        info.setDatabaseConnected(isDatabaseConnected());

        return info;
    }

    private boolean isDatabaseConnected() {
        try {
            if (userService == null) {
                return false;
            }
            userService.isUserExists("admin");
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private String formatDuration(long millis) {
        Duration duration = Duration.ofMillis(millis);
        long seconds = duration.getSeconds();
        return String.format("%d days, %02d:%02d:%02d",
                seconds / 86400,
                (seconds % 86400) / 3600,
                (seconds % 3600) / 60,
                seconds % 60);
    }

    private double getCpuLoad() {
        try {
            com.sun.management.OperatingSystemMXBean osBean =
                (com.sun.management.OperatingSystemMXBean)
                ManagementFactory.getOperatingSystemMXBean();
            return osBean.getSystemCpuLoad(); // 0.0 to 1.0
        } catch (Exception e) {
            return -1.0;
        }
    }
}
