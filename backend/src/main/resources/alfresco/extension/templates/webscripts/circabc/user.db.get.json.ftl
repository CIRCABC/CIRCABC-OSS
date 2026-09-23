<#escape x as jsonUtils.encodeJSONString(x)>
{	
		"userId": "${user.userId}",
		"firstname": "${user.firstname}",
		"lastname": "${user.lastname}",
		"email": "${user.email}",
		"phone": <#if user.phone??>"${user.phone}"<#else>""</#if>,
		"contentFilterLang":"${user.contentFilterLang}",
		"uiLang":"${user.uiLang}",
		"visibility":<#if user.visibility??>${user.visibility?string}<#else>true</#if>,
		"avatar": <#if user.avatar??>"${user.avatar}"<#else>""</#if>,
		"properties": {
				<#assign keys = user.properties?keys>
				<#list keys as k>
					"${k}": <#if user.properties[k]??>"${user.properties[k]}"<#else>""</#if>
					<#if (k_has_next)>, </#if>
				</#list>
		}
}
</#escape>