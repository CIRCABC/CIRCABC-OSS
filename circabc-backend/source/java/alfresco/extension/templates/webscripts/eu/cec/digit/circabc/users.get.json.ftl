<#escape x as jsonUtils.encodeJSONString(x)>
[
	<#list users as user>
	{	
			"userId": "${user.userId}",
			"ecMoniker": <#if user.ecMoniker??>"${user.ecMoniker}"<#else>""</#if>
			,
			"firstname": <#if user.firstname??>"${user.firstname}"<#else>""</#if>,
			"lastname": <#if user.lastname??>"${user.lastname}"<#else>""</#if>,
			"email": <#if user.email??>"${user.email}"<#else>""</#if>,
			"avatar": <#if user.avatar??>"${user.avatar}"<#else>""</#if>
	}
	<#if (user_has_next)>,</#if>
  </#list>
]
</#escape>