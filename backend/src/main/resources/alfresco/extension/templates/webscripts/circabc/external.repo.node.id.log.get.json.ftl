<#escape x as jsonUtils.encodeJSONString(x)>
 [
    <#list logs as log>
            {
                "nodeId": "${log.nodeId}",
                "nodeName": "${log.nodeName}",
                "versionLabel": "${log.versionLabel}",
                "transactionId": "${log.transactionId}",
                "requestType": "${log.requestType}",
                "documentId": <#if log.documentId??>"${log.documentId}"<#else>""</#if>,
                "saveNumber": <#if log.saveNumber??>"${log.saveNumber}"<#else>""</#if>,
                "registrationNumber": <#if log.registrationNumber??>"${log.registrationNumber}"<#else>""</#if>
            }
            <#if (log_has_next)>,</#if>
    </#list>    
]
</#escape>
