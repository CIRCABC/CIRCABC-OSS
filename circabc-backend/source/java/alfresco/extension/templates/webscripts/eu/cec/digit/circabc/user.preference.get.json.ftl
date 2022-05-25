<#escape x as jsonUtils.encodeJSONString(x)>

{
	"library":
		{
			"column":
				{
					"name": <#if preference.library.column.name??>${preference.library.column.name?string}<#else>true</#if>,
					"title": <#if preference.library.column.title??>${preference.library.column.title?string}<#else>true</#if>,
					"modification": <#if preference.library.column.modification??>${preference.library.column.modification?string}<#else>true</#if>,
					"creation": <#if preference.library.column.creation??>${preference.library.column.creation?string}<#else>false</#if>,
					"size": <#if preference.library.column.size??>${preference.library.column.size?string}<#else>true</#if>,
					"version": <#if preference.library.column.version??>${preference.library.column.version?string}<#else>true</#if>,
					"author": <#if preference.library.column.author??>${preference.library.column.author?string}<#else>false</#if>,
					"description": <#if preference.library.column.description??>${preference.library.column.description?string}<#else>false</#if>,
					"expiration": <#if preference.library.column.expiration??>${preference.library.column.expiration?string}<#else>true</#if>,
					"status": <#if preference.library.column.status??>${preference.library.column.status?string}<#else>false</#if>,
					"securityRanking": <#if preference.library.column.securityRanking??>${preference.library.column.securityRanking?string}<#else>false</#if>
				},
			"listing":
				{
					"page": <#if preference.library.listing.page??>${preference.library.listing.page}<#else>0</#if>,
					"limit": <#if preference.library.listing.limit??>${preference.library.listing.limit}<#else>10</#if>,
					"sort": <#if preference.library.listing.sort??>"${preference.library.listing.sort}"<#else>"modified_DESC"</#if>
				}
		}

}
</#escape>