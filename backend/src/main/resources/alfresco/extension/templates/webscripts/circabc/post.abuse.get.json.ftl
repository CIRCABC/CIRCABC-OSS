<#escape x as jsonUtils.encodeJSONString(x)>
[
	<#list abuses as abuse>
	{
		"reportDate": "${abuse.reportDate?string["yyyy MMM dd, HH:mm"]}", 
		"reporter": "${abuse.reporter}",
		"message": "${abuse.message}" 
	}<#if (abuse_has_next)>,</#if>
	</#list>
] 
</#escape>