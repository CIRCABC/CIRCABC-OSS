<#escape x as jsonUtils.encodeJSONString(x)> 
{
	"information": {},
	"library": {},
	"events": {}, 
	"newsgroups": {
		"enableFlagNewTopic": <#if configuration.newsgroups.enableFlagNewTopic??>${configuration.newsgroups.enableFlagNewTopic?string}<#else>false</#if>,
  		"enableFlagNewForum": <#if configuration.newsgroups.enableFlagNewForum??>${configuration.newsgroups.enableFlagNewForum?string}<#else>false</#if>,
  		"ageFlagNewTopic": <#if configuration.newsgroups.ageFlagNewTopic??>${configuration.newsgroups.ageFlagNewTopic?c}<#else>7</#if>,
  		"ageFlagNewForum": <#if configuration.newsgroups.ageFlagNewForum??>${configuration.newsgroups.ageFlagNewForum?c}<#else>7</#if>
	},
	"dashboard": {}
}
</#escape>