<#escape x as jsonUtils.encodeJSONString(x)> 
{
	"id": "${g.id}",
	"name": "${g.name}",
	"title" : 
		<#assign map = g.title>
		<#include "i18nMapIterator.json.ftl">
		,
	"description" : 
		<#assign map = g.description>
		<#include "i18nMapIterator.json.ftl">
		,
	"contact" : 
		<#assign map = g.contact>
		<#include "i18nMapIterator.json.ftl">
		,
	"libraryId": <#if g.libraryId??>"${g.libraryId}"<#else>""</#if>,
	"newsgroupId": <#if g.newsgroupId??>"${g.newsgroupId}"<#else>""</#if>,
	"informationId": <#if g.informationId??>"${g.informationId}"<#else>""</#if>,
	"eventId": <#if g.eventId??>"${g.eventId}"<#else>""</#if>,
	"isPublic": <#if g.isPublic??>${g.isPublic?string}<#else>false</#if>,
	"isRegistered" : <#if g.isRegistered??>${g.isRegistered?string}<#else>false</#if>,
	"allowApply" : <#if g.allowApply??>${g.allowApply?string}<#else>""</#if>,
	"permissions" : {
	<#assign perms = g.permissions?keys>
	<#list perms as p>
		"${p}": "${g.permissions[p]}"<#if (p_has_next)>,</#if>
	</#list>
	},
	"logoUrl": <#if g.logoUrl??>"${g.logoUrl}"<#else>""</#if>
	
}
</#escape>