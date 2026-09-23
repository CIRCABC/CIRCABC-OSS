<#escape x as jsonUtils.encodeJSONString(x)>
{
	"igs": [
		<#list igsAndPermissions.igs as ig>
		{
		"name": "${ig.first}",
		"value": "${ig.second}"
		}<#if (ig_has_next)>, </#if>
		</#list>
	],
	"permissions": [<#list igsAndPermissions.permissions as permission>"${permission}"<#if (permission_has_next)>, </#if></#list>]
}
</#escape>
