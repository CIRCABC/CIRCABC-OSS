<#escape x as jsonUtils.encodeJSONString(x)>

{
	"id": <#if request.id??>${request.id?c}<#else>0</#if>,
	"proposedName": <#if request.proposedName??>"${request.proposedName}"<#else>""</#if>,
	"proposedTitle" : 
		<#assign map = request.proposedTitle>
		<#include "i18nMapIterator.json.ftl">
		,
	"proposedDescription" : 
		<#assign map = request.proposedDescription>
		<#include "i18nMapIterator.json.ftl">
		,
	"requestDate": <#if request.requestDate??>"${request.requestDate.toDate()?datetime?iso_utc}"<#else>""</#if>,
	"from":
		<#if request.from??>
			<#assign user = request.from>
			<#include "user.get.json.ftl">
		<#else>""</#if>,
	"leaders":
		<#if request.leaders??>
			[
			<#list request.leaders as user>
			  	<#include "user.get.json.ftl">
				<#if (user_has_next)>,</#if>
			  </#list>  
			]
		<#else>[]</#if>,
	"justification": <#if request.justification??>"${request.justification}"<#else>""</#if>,
	"reviewer": 
		<#if request.reviewer??>
			<#assign user = request.reviewer>
			<#include "user.get.json.ftl">
		<#else>""</#if>,
	"agreement": <#if request.agreement??>${request.agreement?c}<#else>""</#if>,
	"agreementDate": <#if request.agreementDate??>"${request.agreementDate.toDate()?datetime?iso_utc}"<#else>""</#if>
}

</#escape>