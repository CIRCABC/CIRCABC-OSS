<#escape x as jsonUtils.encodeJSONString(x)>
[
	<#if uploads??>
		<#list uploads as upload>
			{
				"actionDate": <#if upload.actionDate??>"${upload.actionDate}"<#else>""</#if>,
				"action": "upload",
				"igNode": <#if upload.igNode??>"${upload.igNode}"<#else>""</#if>,
				"node": 
				<#assign n = upload.node>
				<#include "node.get.json.ftl">
				
			}
			<#if (upload_has_next)>,</#if>
		</#list>
	</#if>
]
</#escape>