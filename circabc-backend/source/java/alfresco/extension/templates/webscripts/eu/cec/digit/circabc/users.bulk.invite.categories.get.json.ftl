<#escape x as jsonUtils.encodeJSONString(x)>
[
  <#list categories as c>
	{
		"id": "${c.id}",
		"name": "${c.name}"
	}<#if (c_has_next)>,</#if>
  </#list>	
]
</#escape>