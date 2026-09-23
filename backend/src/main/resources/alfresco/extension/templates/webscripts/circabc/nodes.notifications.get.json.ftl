<#escape x as jsonUtils.encodeJSONString(x)>
{
	"profiles": [
		<#list definition.profiles as profileNotif>
			{
				"profile": 
					<#assign profile = profileNotif.profile>
					{
						 "id": "${profile.id}",
						 "name": "${profile.name}",
						 "groupName": "${profile.groupName}",
						 "title": 
						 	<#assign map = profile.title>
							<#include "i18nMapIterator.json.ftl">
					},
				"notifications": "${profileNotif.notifications}",
				"inherited": ${profileNotif.inherited?string}
			}
			<#if (profileNotif_has_next)>, </#if>
		</#list>
	],
	"users": [
		<#list definition.users as userNotif>
			{
				"user": 
					<#assign user = userNotif.user>
					<#include "user.get.json.ftl"/>
				,
				"notifications": "${userNotif.notifications}",
				"inherited": ${userNotif.inherited?string}
			}
			<#if (userNotif_has_next)>, </#if>
		</#list>
	]
}
</#escape>