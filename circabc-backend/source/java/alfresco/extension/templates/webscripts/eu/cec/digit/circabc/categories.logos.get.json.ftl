<#escape x as jsonUtils.encodeJSONString(x)>
[
<#if logos??>
  <#list logos as n>
  	<#include "node.get.json.ftl">
	<#if (n_has_next)>,</#if>
  </#list>  	
</#if>
]
</#escape>