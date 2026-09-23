<#escape x as jsonUtils.encodeJSONString(x)>
[
  <#list userData as c>
	{
		"username": "<#if (c.user)??>${c.user.userName}</#if>",
		"igName": "<#if (c.igName)??>${c.igName}</#if>",
		"igRef": "<#if (c.igRef)??>${c.igRef}</#if>",
		"fromFile": "<#if (c.fromFile)??>${c.fromFile}</#if>",
		"email": "<#if (c.user.email)??>${c.user.email}</#if>",
		"profileId": "<#if (c.profile)??>${c.profile}</#if>",
		"status": "<#if (c.status)??>${c.status}</#if>"
	}<#if (c_has_next)>,</#if>
  </#list>	
]
</#escape>