<#escape x as jsonUtils.encodeJSONString(x)>
{
	"group": 
		<#assign g = dashboard.group>
		<#assign id = dashboard.group.id>
		<#include "groups.get.json.ftl">
	,
	"entries":
		[
			<#list dashboard.entries as entry>
				{
				"date": "${entry.date}", 
				"news": 
					[
						<#list entry.news as item>
							{
								"date": "${item.date}",
								"node": 
									<#assign n = item.node>
									<#include "node.get.json.ftl">  
							}
							<#if (item_has_next)>, </#if>
						</#list>
					]
				}
				<#if (entry_has_next)>, </#if>
			</#list>
		]
}
</#escape>