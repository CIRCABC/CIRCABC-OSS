<#escape x as jsonUtils.encodeJSONString(x)>
[
<#list results as result>
	{
		"id": "${result.id}",
		"canEdit": ${result.canEdit?string},
		"documentLocation": "${result.documentLocation}",
		"errorDescription": "${result.errorDescription}"
	}<#if (result_has_next)>, </#if>
</#list>
]
</#escape>