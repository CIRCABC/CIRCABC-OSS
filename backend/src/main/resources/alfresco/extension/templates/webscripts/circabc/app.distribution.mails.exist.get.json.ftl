<#escape x as jsonUtils.encodeJSONString(x)>
<#if email??>
{
	"id": <#if email.id??>${email.id?c}<#else>0</#if>
	,
	"emailAddress": <#if email.emailAddress??>"${email.emailAddress}"<#else>""</#if>
}
<#else>
{
	"id": "",
	"emailAddress": ""
}
</#if>
			

</#escape>