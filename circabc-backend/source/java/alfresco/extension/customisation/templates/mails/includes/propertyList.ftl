<#--  Copyright European Community 2006 - Licensed under the EUPL V.1.0  -->
<#--  			http://ec.europa.eu/idabc/en/document/6523-->


<i><u>${formatMessage("notification_message_properties_listing")}:</u></i>
<br />
<ul>
       <#if document?exists>
       		 
       		 
             <#if document.properties['cm:author'] ? exists><li>${formatMessage("author")}: <i>${document.properties['cm:author']}</i></li></#if>
             <#if document.properties['cm:title'] ? exists><li>${formatMessage("title")}: <i>${document.properties['cm:title']}</i></li></#if>
             <#if document.properties['sys:locale'] ? exists><li>${formatMessage("language")}: <i>${document.properties['sys:locale']}</i></li></#if>
             <#if document.properties['cm:description'] ? exists><li>${formatMessage("description")}: <i>${document.properties['cm:description']}</i></li></#if>
             <#if document.properties['cm:creator'] ? exists><li>${formatMessage("creator")}: <i>${fullName(document.properties['cm:creator'])}</i></li></#if>
             <#if document.properties['cm:created'] ? exists><li>${formatMessage("created")}: <i>${document.properties['cm:created']?datetime}</i></li></#if>
             <#if document.properties['cm:modifier'] ? exists><li>${formatMessage("modifier")}: <i>${fullName(document.properties['cm:modifier'])}</i></li></#if>
             <#if document.properties['cm:modified'] ? exists><li>${formatMessage("modified")}: <i>${document.properties['cm:modified']?datetime}</i></li></#if>
             <#if document.properties['ver:versionLabel'] ? exists><li>${formatMessage("versionLabel")}: <i>${document.properties['ver:versionLabel']}</i></li></#if>
             <#if document.properties['cd:expiration_date'] ? exists><li>${formatMessage("expiration_date")}: <i>${document.properties['cd:expiration_date']?date}</i></li></#if>
             <#if document.properties['cd:status'] ? exists><li>${formatMessage("bulk_upload_file_status")}: <i>${formatMessage(document.properties['cd:status']?lower_case)}</i></li></#if>
             <#if document.properties['cd:issue_date'] ? exists><li>${formatMessage("issue_date")}: <i>${document.properties['cd:issue_date']?date}</i></li></#if>
             <#if document.properties['cd:keyword'] ? exists><li>${formatMessage("keyword")}: <i><#list document.properties['cd:keyword'] as k>${k.properties.title} </#list></i></li></#if>
             <#if document.properties['cd:reference'] ? exists><li>${formatMessage("reference")}: <i>${document.properties['cd:reference']}</i></li></#if>
             <li>path: <i>${libraryPath(document)}</i></li>
             <#assign descriptions = dynamicPropertyDescriptions(document) >
                         
             <#list descriptions as label>
             	<#assign i = "cd:dynAttr${label_index + 1}" >
			 	<li>${label}: <i><#if document.properties['${i}'] ? exists>${document.properties['${i}']}<#else>&nbsp;</#if></i></li>
			 </#list>  
             
             
             <li>${formatMessage("notification_message_properties_direct_access_url")}: <i><a title="${document.name}" href="${simpleAccessUrl(document)}">${simpleAccessUrl(document)}</a></i></li>
             <li>${formatMessage("notification_message_properties_direct_download_url")}: <i><a title="${document.name}" href="${simpleDownloadUrl(document)}">${simpleDownloadUrl(document)}</a></i></li>
       <#else>
             No document found!
       </#if>	
</ul>


