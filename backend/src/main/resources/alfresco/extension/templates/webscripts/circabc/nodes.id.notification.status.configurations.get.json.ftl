<#escape x as jsonUtils.encodeJSONString(x)>
{
	"data": [
        <#list data as notification>
        {
            "userName": "${notification.username}",
            "status": "${notification.statusName}",
            "type": "${notification.typeName}",
            "authority": "${notification.authority}",
            "nodeId": "${notification.nodeId}",
            "inherited": ${notification.getInherited()?string},
            "title":
                <#if notification.title??>
                    <#assign map = notification.title>
				    <#include "i18nMapIterator.json.ftl">
                <#else>
                    {}
                </#if>
        }<#if (notification_has_next)>, </#if>
        </#list>
      ],
	"total": ${total?c}
}
</#escape>