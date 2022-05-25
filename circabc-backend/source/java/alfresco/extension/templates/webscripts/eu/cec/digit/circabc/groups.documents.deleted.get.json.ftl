<#escape x as jsonUtils.encodeJSONString(x)>
{
	"data": [
	  <#list data as n>
		{
			"id": "${n.id}",
			"name": "${n.name}",
			"type": "${n.type}",
			"parentId": "${n.parentId}",	
			"properties":{
						<#assign keys = n.properties?keys>
						<#list keys as k>
							"${k}": <#if n.properties[k]??>"${n.properties[k]}"<#else>""</#if>
							<#if (k_has_next)>,</#if>
						</#list>
				},
			"title":
				<#assign map = n.title>
				<#include "i18nMapIterator.json.ftl">
				,
			"description":
				<#assign map = n.description>
				<#include "i18nMapIterator.json.ftl">
				,
			"permissions" : {
						<#assign perms = n.permissions?keys>
						<#list perms as p>
							"${p}": "${n.permissions[p]}"
							<#if (p_has_next)>,</#if>
						</#list>
				},
			"notifications": <#if n.notifications??>"${n.notifications}"<#else>""</#if>,
			"deletedBy": <#if n.deletedBy??>"${n.deletedBy}"<#else>""</#if>,
			"deletedDate": <#if n.deletedDate??>"${n.deletedDate}"<#else>""</#if>
		}
		<#if (n_has_next)>,</#if>
	  </#list>
	],
	"total": ${total?c}
}
</#escape>