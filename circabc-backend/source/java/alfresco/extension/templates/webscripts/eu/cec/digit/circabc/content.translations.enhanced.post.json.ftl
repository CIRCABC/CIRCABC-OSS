<#escape x as jsonUtils.encodeJSONString(x)>
{
	"nodeRef": <#if nodeRef ??>"${nodeRef}"<#else>""</#if>
}
</#escape>