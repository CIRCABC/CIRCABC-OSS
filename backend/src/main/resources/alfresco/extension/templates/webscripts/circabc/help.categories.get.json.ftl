<#escape x as jsonUtils.encodeJSONString(x)>

[
  <#list categories as category>
	<#include "help.category.get.json.ftl" />
	<#if (category_has_next)>,</#if>
  </#list>
 ] 

</#escape>