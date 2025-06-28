<#if user?exists>
	<#escape x as jsonUtils.encodeJSONString(x)>
	{	
		"userId": <#if user.userName??>"${user.userName}"<#else>""</#if>,
		"firstname": <#if user.firstName??>"${user.firstName}"<#else>""</#if>,
		"lastname": <#if user.lastName??>"${user.lastName}"<#else>""</#if>,
		"email": <#if user.email??>"${user.email}"<#else>""</#if>,
		"phone": <#if user.phone??>"${user.phone}"<#else>""</#if>,
		"organisation": <#if user.domain??>"${user.domain}"<#else>""</#if>,
		"sourceOrganisation":  <#if user.sourceOrganisation??>"${user.sourceOrganisation}"<#else>""</#if>,
		"dg": <#if user.dg??>"${user.dg}"<#else>""</#if>,
		"departmentNumber": <#if user.orgdepnumber??>"${user.orgdepnumber}"<#else>""</#if>		
	}
	</#escape>
</#if>