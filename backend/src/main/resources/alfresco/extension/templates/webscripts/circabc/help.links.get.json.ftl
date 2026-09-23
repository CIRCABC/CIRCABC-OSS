<#escape x as jsonUtils.encodeJSONString(x)>

[
  <#list links as link>
	<#include "help.link.get.json.ftl" />
	<#if (link_has_next)>,</#if>
  </#list>
 ] 

</#escape>