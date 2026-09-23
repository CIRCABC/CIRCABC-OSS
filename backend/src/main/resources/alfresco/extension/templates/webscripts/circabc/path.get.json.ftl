<#escape x as jsonUtils.encodeJSONString(x)>
[
  <#list nodes as n>
	{
		"id": "${n.id}",
		"name": "${n.name}",
		<#if (n.title)??>"title": {
			<#assign keys = n.title?keys>
			<#list keys as k>
				"${k}": "${n.title[k]}"
				<#if (k_has_next)>,</#if>
			</#list>
		},</#if>
		<#if (n.description)??>"description": {
			<#assign keys = n.description?keys>
			<#list keys as k>
				"${k}": "${n.description[k]}"
				<#if (k_has_next)>,</#if>
			</#list>
		},</#if>
		"type": "${n.type}",
		"properties": {
			<#assign keys = n.properties?keys>
			<#list keys as k>
				"${k}": <#if n.properties[k]??>"${n.properties[k]}"<#else>""</#if>
				<#if (k_has_next)>,</#if>
			</#list>
		}
	}<#if (n_has_next)>,</#if>
  </#list>
]
</#escape>