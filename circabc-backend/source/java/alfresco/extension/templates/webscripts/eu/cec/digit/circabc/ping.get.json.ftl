<#escape x as jsonUtils.encodeJSONString(x)> 
{
	"instanceId": "${info.instanceId}",
	"hostname": "${info.hostname}",
    "ip": "${info.ip}",
    "uptime": "${info.uptime}",
    "uptimeMs": "${info.uptimeMs}",
    "startTime": "${info.startTime}",
    "processId": "${info.processId}",
    "memoryTotalMB": "${info.memoryTotalMB}",
    "memoryFreeMB": "${info.memoryFreeMB}",
	"memoryUsedMB": "${info.memoryUsedMB}"
}
</#escape>