<#escape x as jsonUtils.encodeJSONString(x)>
[
  <#list membership as m>
	{
		<#assign p = m.profile>
		"profile": {
			"id": <#if p.id??>"${p.id}"<#else>""</#if>,
			"name": "${p.name}",
			"groupName": <#if p.groupName??>"${p.groupName}"<#else>""</#if>,
			"title": <#assign map = p.title>
					<#include "i18nMapIterator.json.ftl">
			},
		"permissions": {
		},
		<#assign g = m.interestGroup>
		"interestGroup": <#include "groups.get.json.ftl">
	}
	<#if (m_has_next)>,</#if>
  </#list>
]
</#escape>