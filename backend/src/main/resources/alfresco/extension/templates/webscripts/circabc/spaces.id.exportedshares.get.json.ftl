<#escape x as jsonUtils.encodeJSONString(x)>
[
<#list shares as share>
	{
	"id": "${share.nodeRef.id}",
	"path": "${share.path}"
	}<#if (share_has_next)>, </#if>
</#list>
]
</#escape>
