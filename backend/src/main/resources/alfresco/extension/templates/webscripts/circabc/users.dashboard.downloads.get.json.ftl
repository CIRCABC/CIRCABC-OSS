<#escape x as jsonUtils.encodeJSONString(x)>
[
	<#if downloads??>
		<#list downloads as download>
			{
				"actionDate": <#if download.actionDate??>"${download.actionDate}"<#else>""</#if>,
				"action": "download",
				"igNode": <#if download.igNode??>"${download.igNode}"<#else>""</#if>,
				"node": 
				<#assign n = download.node>
				<#include "node.get.json.ftl">
				
			}
			<#if (download_has_next)>,</#if>
		</#list>
	</#if>
]
</#escape>