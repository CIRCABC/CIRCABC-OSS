package io.swagger.model;

import java.util.Objects;

public class PingInfo {

    private String instanceId = null;
    private String hostname = null;
    private String ip = null;  
    private String uptime = null;
    private long uptimeMs = 0L;
    private long startTime = 0L;
    private String processId = null;
    private long memoryTotalMB = 0L;
    private long memoryFreeMB = 0L;
    private long memoryUsedMB = 0L;

    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getUptime() {
        return uptime;
    }

    public void setUptime(String uptime) {
        this.uptime = uptime;
    }

    public long getUptimeMs() {
        return uptimeMs;
    }

    public void setUptimeMs(long uptimeMs) {
        this.uptimeMs = uptimeMs;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public String getProcessId() {
        return processId;
    }

    public void setProcessId(String processId) {
        this.processId = processId;
    }

    public long getMemoryTotalMB() {
        return memoryTotalMB;
    }

    public void setMemoryTotalMB(long memoryTotalMB) {
        this.memoryTotalMB = memoryTotalMB;
    }

    public long getMemoryFreeMB() {
        return memoryFreeMB;
    }

    public void setMemoryFreeMB(long memoryFreeMB) {
        this.memoryFreeMB = memoryFreeMB;
    }

    public long getMemoryUsedMB() {
        return memoryUsedMB;
    }

    public void setMemoryUsedMB(long memoryUsedMB) {
        this.memoryUsedMB = memoryUsedMB;
    }


  
  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PingInfo info = (PingInfo) o;
    return (
      Objects.equals(this.instanceId, info.instanceId) &&
      Objects.equals(this.ip, info.ip) &&
      Objects.equals(this.hostname, info.hostname) &&
      Objects.equals(this.uptime, info.uptime) &&
      Objects.equals(this.uptimeMs, info.uptimeMs) &&
      Objects.equals(this.startTime, info.startTime) &&
      Objects.equals(this.processId, info.processId) &&
      Objects.equals(this.memoryTotalMB, info.memoryTotalMB) &&
      Objects.equals(this.memoryFreeMB, info.memoryFreeMB) &&
      Objects.equals(this.memoryUsedMB, info.memoryUsedMB)
    );
  }

  @Override
  public int hashCode() {
    return Objects.hash(
      instanceId,
      ip,
      hostname,
      uptime,
      uptimeMs,
      startTime,
      processId,
      memoryTotalMB,
      memoryFreeMB,
      memoryUsedMB
    );
  }

  @Override
  public String toString() {
    return (
      "class Info {\n" +
      "    instanceId: " +
      toIndentedString(instanceId) +
      "\n" +
      "    ip: " +
      toIndentedString(ip) +
      "\n" +
      "    hostname: " +
      toIndentedString(hostname) +
      "\n" +
      "    uptime: " +
      toIndentedString(uptime) +
      "\n" +
        "    uptimeMs: " +
        toIndentedString(uptimeMs) +
        "\n" +
        "    startTime: " +
        toIndentedString(startTime) +
        "\n" +
        "    processId: " +
        toIndentedString(processId) +
        "\n" +
        "    memoryTotalMB: " +
        toIndentedString(memoryTotalMB) +
        "\n" +
        "    memoryFreeMB: " +
        toIndentedString(memoryFreeMB) +
        "\n" +
        "    memoryUsedMB: " +
        toIndentedString(memoryUsedMB) +
        "\n" +
      "}"
    );
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces (except the first line).
   */
  private String toIndentedString(java.lang.Object o) {
    return Util.toIndentedString(o);
  }
}
