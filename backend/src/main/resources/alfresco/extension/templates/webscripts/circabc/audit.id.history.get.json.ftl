<#escape x as jsonUtils.encodeJSONString(x)>
{
	"data": [
		<#list logResults as logResult>
		{
		      "logDate": "${logResult.logDate?string["yyyy-MM-dd HH:mm"]}", 
		      "activityDescription": "${logResult.activityDescription}",
		      "serviceDescription": "${logResult.serviceDescription}",
		      "userId": "${logResult.userName}",
		      "information": "${logResult.info}",
		      "path": "${logResult.path}",
		      "success": ${(logResult.isOK == 1)?string}
		}<#if (logResult_has_next)>, </#if>
		</#list>
	],
	"total": ${total?c}
}
</#escape>