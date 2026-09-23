<#escape x as jsonUtils.encodeJSONString(x)>
{	
		"recoverable": <#if recoveryOption.recoverable??>${recoveryOption.recoverable?string}<#else>false</#if>,
		"profile": 
			<#if recoveryOption.profile??>
				<#assign profile = recoveryOption.profile>
				<#include "groups.profiles.post.json.ftl" />
			<#else>
				{}
			</#if>
}
</#escape>