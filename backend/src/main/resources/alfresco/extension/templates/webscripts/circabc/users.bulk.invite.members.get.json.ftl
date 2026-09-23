<#escape x as jsonUtils.encodeJSONString(x)>
[
  <#list members as c>
	{
		"username": "<#if (c.user)??>${c.user.userName}</#if>",
		"igName": "${c.igName}",
		"igRef": "${c.igRef}",
		"fromFile": "<#if (c.fromFile)??>${c.fromFile}</#if>",
		"email": "<#if (c.user.email)??>${c.user.email}</#if>",
		"status": "${c.status}"
	}<#if (c_has_next)>,</#if>
  </#list>	
]
</#escape>