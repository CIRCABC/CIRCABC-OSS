<#escape x as jsonUtils.encodeJSONString(x)>
[
<#list groups as g>
	<#include "groups.get.json.ftl">
	<#if (g_has_next)>, </#if>
</#list>
]
</#escape>