<#escape x as jsonUtils.encodeJSONString(x)>
{
	"display": <#if config.display??>${config.display?string}<#else>true</#if>
}
</#escape>