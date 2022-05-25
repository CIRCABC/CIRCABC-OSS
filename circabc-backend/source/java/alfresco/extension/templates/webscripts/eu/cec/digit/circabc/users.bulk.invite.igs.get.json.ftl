<#escape x as jsonUtils.encodeJSONString(x)>
[
  <#list igs as c>
	{
		"igName": "${c.igName}",
		"igId": "${c.igId}",
		"categoryId": "${c.categoryId}"
	}<#if (c_has_next)>,</#if>
  </#list>	
]
</#escape>