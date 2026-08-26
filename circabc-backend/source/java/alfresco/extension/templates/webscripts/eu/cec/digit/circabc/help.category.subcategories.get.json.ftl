<#escape x as jsonUtils.encodeJSONString(x)>

[
	<#list subcategories as subcategory>
		<#include "help.subcategory.get.json.ftl" />
		<#if (subcategory_has_next)>,</#if>
	</#list>
]

</#escape>
