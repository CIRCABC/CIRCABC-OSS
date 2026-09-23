<#escape x as jsonUtils.encodeJSONString(x)>

[
	<#list applicants as applicant>
		{
			"user": 
				<#assign user = applicant.user>
				<#include "user.get.json.ftl"/>,
			"justification": <#if applicant.justification??>"${applicant.justification}"<#else>""</#if>,
			"submitted": "${applicant.submitted}"
		}
		<#if (applicant_has_next)>, </#if>
	</#list>
]

</#escape>