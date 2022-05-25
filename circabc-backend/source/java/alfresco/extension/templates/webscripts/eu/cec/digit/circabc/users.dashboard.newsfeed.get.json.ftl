<#escape x as jsonUtils.encodeJSONString(x)>
{
	"when": <#if feed.when??>"${feed.when}"<#else>""</#if>,
	"uploads": <#if feed.uploads??>${feed.uploads?c}<#else>0</#if>,
	"updates": <#if feed.updates??>${feed.updates?c}<#else>0</#if>,
	"comments": <#if feed.comments??>${feed.comments?c}<#else>0</#if>,
	"groupFeeds": [
		<#list feed.groupFeeds as groupFeed>
			{
				"id": <#if groupFeed.id??>"${groupFeed.id}"<#else>""</#if>,
				"name": <#if groupFeed.name??>"${groupFeed.name}"<#else>""</#if>,
				"title":<#assign map = groupFeed.title>
					<#include "i18nMapIterator.json.ftl">
					,
				"feed": [
					<#list groupFeed.feed as activitiy>
						{
							"actionDate": <#if activitiy.actionDate??>"${activitiy.actionDate}"<#else>""</#if>,
							"action": <#if activitiy.action??>"${activitiy.action}"<#else>""</#if>,
							"igNode": <#if activitiy.igNode??>"${activitiy.igNode}"<#else>""</#if>,
							"username": <#if activitiy.username??>"${activitiy.username}"<#else>""</#if>,
							"node": 
							<#assign n = activitiy.node>
							<#include "node.get.json.ftl">
							
						}
						<#if (activitiy_has_next)>,</#if>
				</#list>
				]				
			}
			<#if (groupFeed_has_next)>,</#if>
		</#list>
	]
}
</#escape>