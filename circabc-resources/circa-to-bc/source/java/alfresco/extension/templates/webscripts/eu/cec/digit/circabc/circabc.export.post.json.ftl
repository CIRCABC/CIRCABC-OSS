<#escape x as jsonUtils.encodeJSONString(x)>
{
  "message": "${message!""}"
  <#if iterationName??>, "iterationName": "${iterationName}"</#if>
  <#if implementationName??>, "implementationName": "${implementationName}"</#if>
  <#if exportFileRef??>, "exportFileRef": "${exportFileRef}"</#if>
}
</#escape>
