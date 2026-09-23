<#escape x as jsonUtils.encodeJSONString(x)>
[
  <#list data as n>
	<#include "node.get.json.ftl">
	<#if (n_has_next)>,</#if>
  </#list>
]
</#escape>