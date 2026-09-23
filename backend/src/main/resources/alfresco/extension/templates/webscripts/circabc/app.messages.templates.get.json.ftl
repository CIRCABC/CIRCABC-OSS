<#escape x as jsonUtils.encodeJSONString(x)>
{
	"data":
		[
		  <#list messages.data as message>
			<#include "app.message.template.get.json.ftl">
			<#if (message_has_next)>,</#if>
		  </#list>
		],
	"total": ${total?c}
}

</#escape>