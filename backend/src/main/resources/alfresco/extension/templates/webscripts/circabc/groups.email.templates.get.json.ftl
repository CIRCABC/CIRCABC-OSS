<#escape x as jsonUtils.encodeJSONString(x)>
[
	<#list templates as template>
		{
			"id": "${template.id}", 
			"name": "${template.name}",
			"subject": "${template.subject}",
			"text": "${template.text}"
		}
		<#if (template_has_next)>, </#if>
	</#list>
]
</#escape>