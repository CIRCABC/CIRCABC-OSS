<#escape x as jsonUtils.encodeJSONString(x)>
{
	"enable": <#if config.enable??>${config.enable?string}<#else>false</#if>
}
</#escape>