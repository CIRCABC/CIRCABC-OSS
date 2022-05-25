<#escape x as jsonUtils.encodeJSONString(x)>
{
	"data": [
        <#list data as statistic>
        {
            "name": "${statistic.name}",
            "size": "${statistic.sizeAsString}",
            "modifiedDate": "${statistic.fileInfo.modifiedDate?string["yyyy-MM-dd"]}",
            "downloadURL": "${statistic.downloadUrl}"
        }<#if (statistic_has_next)>, </#if>
        </#list>
      ],
	"total": ${total?c}
}
</#escape>
