<#escape x as jsonUtils.encodeJSONString(x)>
{
	<#if report??>
		"lockedNodes": [
			<#if report.lockedNodes??>
				<#list report.lockedNodes as n>
					<#include "node.get.json.ftl">
					<#if (n_has_next)>,</#if>
				  </#list>
			</#if>
		],
		"sharedNodes": [
			<#if report.sharedFolders??>
				<#list report.sharedFolders as n>
					<#include "node.get.json.ftl">
					<#if (n_has_next)>,</#if>
				  </#list>
			</#if>
		],
		"sharedProfiles": [
			<#if report.sharedProfiles??>
				<#list report.sharedProfiles as profile>
					
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
						"exported": ${profile.exported?string},
						"exportedRefs": 
							[
								<#list profile.exportedRefs as targetRef>
									"${targetRef}"
									<#if (targetRef_has_next)>, </#if>
								</#list>
							]
						}
	
					<#if (profile_has_next)>,</#if>
				  </#list>
			</#if>
		]
	</#if>
}
</#escape>