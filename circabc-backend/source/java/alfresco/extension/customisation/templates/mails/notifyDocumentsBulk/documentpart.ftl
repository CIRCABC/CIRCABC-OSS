<tr>
    <td align="center" height="25" style="height:25px; font-size: 0;">&nbsp;</td>
  </tr>
  <tr>
    <td align="left" style="font-family: Helvetica, Arial, sans-serif; font-size: 16px; line-height: 25px; color: #2C3E50;">
    
		<#if document.name ?? ><b>${formatMessage("name")}: </b>${document.name}<br/></#if>
		<#if document.title ?? ><b>${formatMessage("title")}: </b>${document.title}<br/></#if>
		<#if document.description ??><b>${formatMessage("description")}: </b>${document.description}<br/></#if>
				
		<#-- <#if document.properties['cm:creator'] ? exists><b>${formatMessage("creator")}: </b>${fullName(document.properties['cm:creator'])}<br/></#if> -->
		<#-- <#if document.properties['cm:created'] ? exists><b>${formatMessage("created")}: </b>${document.properties['cm:created']?datetime}<br/></#if> -->
		
		<#if document.modifier ??><b>${formatMessage("modifier")}: </b>${document.modifier}<br/></#if>
		<#if document.modified ??><b>${formatMessage("modified")}: </b>${document.modified}<br/></#if>
		
		<#-- <#if document.properties['cm:author'] ? exists><b>${formatMessage("author")}: </b>${document.properties['cm:author']}<br/></#if> -->
		<#-- <#if document.properties['sys:locale'] ? exists><b>${formatMessage("language")}: </b>${document.properties['sys:locale']}<br/></#if> -->
		
		<#-- <#if document.properties['ver:versionLabel'] ? exists><b>${formatMessage("versionLabel")}: </b>${document.properties['ver:versionLabel']}<br/></#if> -->
		<#-- <#if document.properties['cd:expiration_date'] ? exists><b>${formatMessage("expiration_date")}: </b>${document.properties['cd:expiration_date']?date}<br/></#if> -->
		<#-- <#if document.properties['cd:status'] ? exists><b>${formatMessage("bulk_upload_file_status")}: </b>${formatMessage(document.properties['cd:status']?lower_case)}<br/></#if> -->
		<#-- <#if document.properties['cd:issue_date'] ? exists><b>${formatMessage("issue_date")}: </b>${document.properties['cd:issue_date']?date}<br/></#if> -->
		
		<#if document.keywords ??><b>${formatMessage("keyword")}: </b>${document.keywords}<br/></#if>
		
		<#-- <#if document.properties['cd:reference'] ? exists><b>${formatMessage("reference")}: </b>${document.properties['cd:reference']}<br/></#if> -->

		<b>${formatMessage("igroot_home_path")}: </b>${document.path} <br/>
		
		<#assign dynProps = document.dynamicProperties?keys >
				 
		<#list dynProps as label>
			<#if document.dynamicProperties[label] ??><b>${label}: </b>${document.dynamicProperties[label]}<br/></#if>
		</#list>  
		<br/>
		
		<b>${formatMessage("notification_message_properties_direct_access_url")}: </b><a title="${document.name}" href="${document.url}">${document.url}</a><br/>
		
		<br/>
		<a href="${document.url}" title="Read more" target="_blank" style="font-family: Helvetica, Arial, sans-serif; font-size: 16px; color: #335577; text-decoration: none; border-radius: 3px; padding: 15px 25px; border: 1px solid #335577; display: inline-block;" class="mobile-btn">
    		${formatMessage("email_label_visit")} &#187;</a>
	</td>
  </tr>