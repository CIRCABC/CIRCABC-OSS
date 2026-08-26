<#-- Escape JSON strings to ensure proper JSON formatting -->
<#escape x as jsonUtils.encodeJSONString(x)>
{
  "data": [
    <#-- Loop through each request in the data list -->
    <#list requests.data as request>
    {
      "id": <#if request.id??>${request.id?c}<#else>0</#if>,
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
	    "requestDate": <#if request.requestDate??>"${request.requestDate.toDate()?datetime?iso_utc}"<#else>""</#if>,
      "categoryRef": <#if request.categoryRef??>"${request.categoryRef}"<#else>""</#if>,
      "groupId": <#if request.groupId??>"${request.groupId}"<#else>""</#if>,
      "justification": <#if request.justification??>"${request.justification}"<#else>""</#if>,
      "reviewer": 
        <#if request.reviewer??>
          <#assign user = request.reviewer>
          <#include "user.get.json.ftl">
        <#else>""</#if>,
      "agreement": <#if request.agreement??>${request.agreement?c}<#else>""</#if>,
      "agreementDate": <#if request.agreementDate??>"${request.agreementDate.toDate()?datetime?iso_utc}"<#else>""</#if>,
      "name": <#if request.name??>"${request.name}"<#else>""</#if>,
      "title": <#if request.title??>"${request.title}"<#else>""</#if>,
      "description": <#if request.description??>"${request.description}"<#else>""</#if>
        }<#if request_has_next>,</#if>
        </#list>
  ],
  "total": ${requests.total?c}
}
</#escape>



