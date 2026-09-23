<#escape x as jsonUtils.encodeJSONString(x)>
{
	"id": "${n.id}",
	"name": "${n.name}",
	"type": "${n.type}",
	"service": <#if n.service??>"${n.service.serviceName}"<#else>""</#if>,
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
	"favourite": <#if n.favourite??>${n.favourite?string}<#else>false</#if>,
	"hasSubFolders": <#if n.hasSubFolders??>${n.hasSubFolders?string}<#else>false</#if>,
	"hasGuestAccess": <#if n.hasGuestAccess??>${n.hasGuestAccess?string}<#else>false</#if>,
	"originalNodeRef": <#if n.originalNodeRef??>"${n.originalNodeRef}"<#else>""</#if>
}
</#escape>
