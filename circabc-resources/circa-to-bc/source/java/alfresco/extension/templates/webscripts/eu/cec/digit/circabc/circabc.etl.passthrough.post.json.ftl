<#escape x as jsonUtils.encodeJSONString(x)>
{
  "message": "${message!""}"
  <#if iterationName??>, "iterationName": "${iterationName}"</#if>
}
</#escape>
