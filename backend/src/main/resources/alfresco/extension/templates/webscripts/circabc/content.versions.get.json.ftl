<#escape x as jsonUtils.encodeJSONString(x)>
[
	<#if versions??>
		<#list versions as version>
			<#include "content.version.get.json.ftl"/>
			<#if (version_has_next)>,</#if>
		</#list>
	</#if>
]
</#escape>