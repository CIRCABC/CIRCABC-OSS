<#escape x as jsonUtils.encodeJSONString(x)>
{
	"data": [
    	<#list data as user>
        {
            "userName": "${user.userName}",
            "lastName": "${user.lastName}",
            "firstName": "${user.firstName}",
            "emailAddress": "${user.emailAddress}",
            "userProperties": {
				<#assign keys = user.userProperties?keys>
				<#list keys as k>
				"${k}": <#if user.userProperties[k]??>"${user.userProperties[k]?string}"<#else>""</#if><#if (k_has_next)>,</#if>
				</#list>
			},
            "notificationLanguage": <#if (user.notificationLanguage)??>"${user.notificationLanguage.toString()}"<#else>""</#if>,
            "personId": "${user.person.id}"
        }<#if (user_has_next)>, </#if>
        </#list>
      ],
	"total": ${total?c}
}
</#escape>