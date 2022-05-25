<#escape x as jsonUtils.encodeJSONString(x)>
{	
	"data": [
		<#list revocations.data as request>
			{
				"id": <#if request.id??>${request.id?c}<#else>0</#if>,
				"requester": <#if request.requester??>"${request.requester}"<#else>""</#if>,
				"revocationDate": <#if request.revocationDate??>"${request.revocationDate}"<#else>""</#if>,
				"requestState": <#if request.requestState??>${request.requestState?c}<#else>0</#if>,
				"userIds": [
					<#list request.userIds as userId>
						"${userId}"
					<#if (userId_has_next)>,</#if>
					</#list>
				],
				"jobStarted": <#if request.jobStarted??>"${request.jobStarted}"<#else>""</#if>,
				"jobEnded": <#if request.jobEnded??>"${request.jobEnded}"<#else>""</#if>,
				"action": <#if request.action??>"${request.action}"<#else>""</#if>,
				"groupId": <#if request.groupId??>"${request.groupId}"<#else>""</#if>
			}
		<#if (request_has_next)>,</#if>
		</#list>	
	],
	"total": ${revocations.total?c}
}
</#escape>