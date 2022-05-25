<#escape x as jsonUtils.encodeJSONString(x)>
	<#if version??>
		{
			"versionLabel": "${version.versionLabel}",
			"notes": "${version.notes}",
			"node": 
				<#assign n = version.node>
				<#include "node.get.json.ftl">
		}
	</#if>
</#escape>