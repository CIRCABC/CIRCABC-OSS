<#escape x as jsonUtils.encodeJSONString(x)>
[
  <#list messages as message>
	<#include "app.message.template.get.json.ftl">
	<#if (message_has_next)>,</#if>
  </#list>
]
</#escape>