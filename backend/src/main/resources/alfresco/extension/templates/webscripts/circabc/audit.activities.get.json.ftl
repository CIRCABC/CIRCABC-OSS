<#escape x as jsonUtils.encodeJSONString(x)>
<#compress>
[
	<#list activities as activity>
	{
		"id": "${activity.id}",
		"name": "${activity.activityDescription}",
		"service": "${activity.serviceDescription}"
	}<#if (activity_has_next)>, </#if>
	</#list>
]
</#compress>
</#escape>