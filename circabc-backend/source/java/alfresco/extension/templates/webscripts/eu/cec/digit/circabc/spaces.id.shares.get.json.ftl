<#escape x as jsonUtils.encodeJSONString(x)>
{
	"data": [
		<#list shares as share>
			{
			"igId": "${share.igId}",
			"igName": "${share.igName}",
			"permission": "${share.permission}"
			}<#if (share_has_next)>, </#if>
		</#list>
	],
	"total": ${total?c}
}
</#escape>
