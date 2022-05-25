<#escape x as jsonUtils.encodeJSONString(x)>
 [
  <#list logos as n>
	<#include "node.get.json.ftl">
	<#if (n_has_next)>,</#if>
  </#list>
]
</#escape>