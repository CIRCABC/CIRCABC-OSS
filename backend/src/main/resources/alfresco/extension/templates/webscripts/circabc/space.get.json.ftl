<#escape x as jsonUtils.encodeJSONString(x)>
{
	"data": [
	  <#list data as n>
		<#include "node.get.json.ftl">
		<#if (n_has_next)>,</#if>
	  </#list>
	],
	"total": ${total?c}
}
</#escape>