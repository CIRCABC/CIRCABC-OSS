<#escape x as jsonUtils.encodeJSONString(x)>
{
  "message": "${message}",
  "categoryId": "${categoryId}",
  "durationMs": ${durationMs?c}
}
</#escape>
