<#escape x as jsonUtils.encodeJSONString(x)>

{
	"url": <#if information.url??>"${information.url}"<#else>""</#if>,
	"adapt": <#if information.adapt??>${information.adapt?string}<#else>false</#if>,
	"displayOldInformation": <#if information.displayOldInformation??>${information.displayOldInformation?string}<#else>false</#if>,
	"permissions" : {
		<#assign perms = information.permissions?keys>
		<#list perms as p>
			"${p}": "${information.permissions[p]}"
			<#if (p_has_next)>,</#if>
		</#list>
	}
}
	
</#escape>