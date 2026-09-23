<#escape x as jsonUtils.encodeJSONString(x)>
{	
		"userId": "${user.userId}",
		"firstname": "${user.firstname}",
		"lastname": "${user.lastname}",
		"email": "${user.email}",
		"phone": "${user.phone}",
		"contentFilterLang":"${user.contentFilterLang}",
		"uiLang":"${user.uiLang}",
		"visibility":<#if user.visibility??>${user.visibility?string}<#else>true</#if>,
		"avatar": <#if user.avatar??>"${user.avatar}"<#else>""</#if>
}
</#escape>