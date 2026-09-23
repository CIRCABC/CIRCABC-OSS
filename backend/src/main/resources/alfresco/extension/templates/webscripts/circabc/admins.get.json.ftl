<#escape x as jsonUtils.encodeJSONString(x)>
<#compress>
[ 	
  <#list admins as a>
	{
		"userId": "${a.userId}",
		"firstname": "${a.firstname}",
		"lastname": "${a.lastname}",
		"email": "${a.email}"
	}<#if (a_has_next)>,</#if>
  </#list>
]
</#compress>
</#escape>