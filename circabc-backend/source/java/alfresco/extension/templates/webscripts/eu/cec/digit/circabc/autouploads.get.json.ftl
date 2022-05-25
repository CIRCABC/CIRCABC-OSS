<#escape x as jsonUtils.encodeJSONString(x)>
{
	"data": [
		<#list autouploads as autoupload>
			{
			"idConfiguration": ${autoupload.idConfiguration?c},
			"status": ${autoupload.status?c},
			"igName": "${autoupload.igName}",
			"fileId": <#if autoupload.fileNodeRef??>"${autoupload.fileNodeRef}"<#else>""</#if>,
			"parentId": <#if autoupload.parentNodeRef??>"${autoupload.parentNodeRef}"<#else>""</#if>,
			"ftpHost": "${autoupload.ftpHost}",
			"ftpPort": ${autoupload.ftpPort?c},
			"ftpUsername": <#if autoupload.ftpUsername??>"${autoupload.ftpUsername}"<#else>""</#if>,
			"ftpPath": <#if autoupload.ftpPath??>"${autoupload.ftpPath}"<#else>""</#if>,
			"dateRestriction": "${autoupload.dateRestriction}",
			"emails": <#if autoupload.emails??>"${autoupload.emails}"<#else>""</#if>,
			"autoExtract": ${autoupload.autoExtract?string},
			"jobNotifications": ${autoupload.jobNotifications?string}
			}<#if (autoupload_has_next)>, </#if>
		</#list>
	],
	"total": ${total?c}
}
</#escape>
