<#escape x as jsonUtils.encodeJSONString(x)>
[
	<#list discussions as discussion>
		{
		"post":
			<#assign n = discussion.post>
			<#include "node.get.json.ftl">  
		, 
		"topic": 
			<#assign n = discussion.topic>
			<#include "node.get.json.ftl">  
		}
		<#if (discussion_has_next)>, </#if>
	</#list>
]
</#escape>