<#escape x as jsonUtils.encodeJSONString(x)>

	{
	 "id": "${profile.id}",
	 "name": "${profile.name}",
	 "groupName": "${profile.groupName}",
	 "title":  
	 	<#assign map = profile.title>
		<#include "i18nMapIterator.json.ftl">
		,
	 "permissions" : {
		<#assign perms = profile.permissions?keys>
		<#list perms as p>
			"${p}": "${profile.permissions[p]}"
			<#if (p_has_next)>,</#if>
		</#list>
		},
	"imported": ${profile.imported?string},
	"importedRef": <#if profile.importedRef??>"${profile.importedRef}"<#else>""</#if>,
	"exported": ${profile.exported?string}
	}

</#escape>