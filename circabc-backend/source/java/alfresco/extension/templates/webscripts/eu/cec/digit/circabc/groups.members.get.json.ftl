<#escape x as jsonUtils.encodeJSONString(x)>
{
	"data": 
		[
		<#list data as member>
			{
			"expirationDate": <#if member.expirationDate??>"${member.expirationDate?datetime?iso_utc}"<#else>""</#if>,
			"user": 
				<#assign user = member.user>
				<#include "user.get.json.ftl"/>,
			"profile": 
				<#assign profile = member.profile>
				{
				 "id": "${profile.id}",
				 "groupName": "${profile.groupName}",
				 "name": "${profile.name}",
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
					}
				}
			}
			<#if (member_has_next)>, </#if>
		</#list>
		],
	"total": ${total?c}
}

</#escape>