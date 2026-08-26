<#escape x as jsonUtils.encodeJSONString(x)>
{
  "message": "${message}",
  "groupId": "${groupId}",
  "durationMs": ${durationMs?c}
}
</#escape>
