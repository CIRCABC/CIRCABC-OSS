<#escape x as jsonUtils.encodeJSONString(x)>
{
	<#if map??>
		<#assign keys = map?keys>
		<#list keys as k>
			"${k}": <#if map[k]??>"${map[k]}"<#else>""</#if>
			<#if (k_has_next)>,</#if>
		</#list>
	</#if>
}
</#escape>