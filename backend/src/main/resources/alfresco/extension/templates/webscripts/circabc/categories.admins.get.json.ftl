<#escape x as jsonUtils.encodeJSONString(x)>
[
<#list admins as user>
	<#include "user.get.json.ftl"/>
	<#if (user_has_next)>, </#if>
</#list>
]
</#escape>