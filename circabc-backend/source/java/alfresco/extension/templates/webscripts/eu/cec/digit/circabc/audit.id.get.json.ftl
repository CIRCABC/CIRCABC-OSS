<#escape x as jsonUtils.encodeJSONString(x)>
 [
	<#list logResults as logResult>
	{
		"logDate": "${logResult.logDate?string["yyyy-MM-dd HH:mm"]}", 
		"activityDescription": "${logResult.activityDescription}",
		"serviceDescription": "${logResult.serviceDescription}",
		"userId": "${logResult.userName}",
		"information": <#if logResult.info??>"${logResult.info}"<#else>""</#if>,
		"path": "${logResult.path}",
		"success": ${(logResult.isOK == 1)?string}
	}<#if (logResult_has_next)>, </#if>
	</#list>
]
</#escape>