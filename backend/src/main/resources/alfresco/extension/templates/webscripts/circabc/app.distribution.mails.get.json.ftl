<#escape x as jsonUtils.encodeJSONString(x)>
{
	"data":
		[
		  <#list emails.data as email>
			{
				"id": <#if email.id??>${email.id?c}<#else>0</#if>
				,
				"emailAddress": <#if email.emailAddress??>"${email.emailAddress}"<#else>""</#if>
			}
			<#if (email_has_next)>,</#if>
		  </#list>
		],
	"total": ${total?c}
}
</#escape>