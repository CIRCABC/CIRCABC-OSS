<#escape x as jsonUtils.encodeJSONString(x)>
{
	"inherited": ${definition.inherited?string},
	"permissions": {
		"profiles": [
			<#list definition.permissions.profiles as profilePerm>
				{
					"profile": 
						<#assign profile = profilePerm.profile>
						{
							 "id": "${profile.id}",
							 "name": "${profile.name}",
							 "groupName": "${profile.groupName}",
							 "title":  
							 	<#assign map = profile.title>
								<#include "i18nMapIterator.json.ftl">
						},
					"permission": "${profilePerm.permission}",
					"inherited": <#if profilePerm.inherited??>${profilePerm.inherited?string}<#else>true</#if>
				}
				<#if (profilePerm_has_next)>, </#if>
			</#list>
		],
		"users": [
			<#list definition.permissions.users as userPerm>
				{
					"user": 
						<#assign user = userPerm.user>
						<#include "user.get.json.ftl"/>
					,
					"permission": "${userPerm.permission}",
					"inherited": <#if userPerm.inherited??>${userPerm.inherited?string}<#else>true</#if>
				}
				<#if (userPerm_has_next)>, </#if>
			</#list>
		]
	}
}
</#escape>