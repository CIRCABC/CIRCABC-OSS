<#escape x as jsonUtils.encodeJSONString(x)>
{
  "hostName": "${info.hostname}",
  "ip": "${info.ip}",
  "instanceId": "${info.instanceId}",
  "status": "${info.status}",
  "upTime": "${info.upTime}",
  "startTime": "${info.startTime}",

  "memory": {
    "used": ${info.memory.used?c},
    "max": ${info.memory.max?c},
    "free": ${info.memory.free?c}
  },

  "cpu": {
    "usage": ${info.cpu.usage?c},
    "cores": ${info.cpu.cores?c}
  },
  </#escape>
  "databaseConnected": ${info.databaseConnected?string("true", "false")}
}