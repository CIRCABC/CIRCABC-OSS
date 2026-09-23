<#--  Copyright European Community 2006 - Licensed under the EUPL V.1.0  -->
<#--  			http://ec.europa.eu/idabc/en/document/6523				 -->

<#compress>

<#include companyhome.childByNamePath["Data Dictionary/CircaBC/templates/mails/parts/mailheader.ftl"].nodeRef>


<#-- ------------------------------------- -->
<#-- --------------CONTENT---------------- -->
<#-- ------------------------------------- -->


<tr>
    <td bgcolor="#ffffff" align="center" style="padding: 0 10px 0 10px;">
      <!--[if (gte mso 9)|(IE)]>
      <table role="presentation" align="center" border="0" cellspacing="0" cellpadding="0" width="600">
      <tr>
      <td align="center" valign="top" width="600">
      <![endif]-->
      <table role="presentation" border="0" cellpadding="0" cellspacing="0" width="100%" style="max-width: 600px;" class="table-max">
        <tr>
          <td><table role="presentation" width="100%" border="0" cellspacing="0" cellpadding="0">
              <tr>
                <td><table role="presentation" width="100%" border="0" cellspacing="0" cellpadding="0">
                    <tr>
                      <td align="center" height="25" style="height:25px; font-size: 0;">&nbsp;</td>
                    </tr>
                    <tr>
                      <td align="left">
                      	<h1 style="font-family: Helvetica, Arial, sans-serif; font-size: 28px; font-weight:normal; color: #2C3E50; margin:0; mso-line-height-rule:exactly;">
                      		${formatMessage("email_label_upload")}</h1>
                      </td>
                    </tr>
                    <tr>
                      <td align="center" height="25" style="height:25px; font-size: 0;">&nbsp;</td>
                    </tr>
                    <tr>
                      <td align="left" style="font-family: Helvetica, Arial, sans-serif; font-size: 16px; line-height: 25px; color: #2C3E50;"> 
						${formatMessage("mails_common_dear_user", fullName(person))}
						<br>
                        <br>
                        <#if document?has_content>
							${formatMessage("notification_message_header_new",
								document.name,
								libraryAccessUrl(document),
								titleOrName(interestGroup),
								groupAccessUrl(interestGroup),
								titleOrName(category))
							}
						</#if>
                    </tr>
                  </table></td>
              </tr>
              <tr>
                <td align="center" height="25" style="height:25px; font-size: 0;">&nbsp;</td>
              </tr>
              <tr>
                <td align="center"><table role="presentation" width="100%" border="0" cellspacing="0" cellpadding="0">
                    <tr>
                      <td align="right">
                      <table role="presentation" border="0" cellspacing="0" cellpadding="0" class="mobile-btn-container">
                          <tr>
                            <td align="center" style="border-radius: 3px;" bgcolor="#8dcb6c">
                            	<a href="${libraryAccessUrl(document)}" title="Read more" target="_blank" style="font-family: Helvetica, Arial, sans-serif; font-size: 16px; color: #ffffff; text-decoration: none; border-radius: 3px; padding: 15px 25px; border: 1px solid #8dcb6c; display: inline-block;" class="mobile-btn">
                            		${formatMessage("email_label_visit")} &#187;</a>
                            	</td>
                          </tr>
                        </table></td>
                    </tr>
                 </td>
              </tr>
            </table></td>
        </tr>
      </table>
      <!--[if (gte mso 9)|(IE)]>
      </td>
      </tr>
      </table>
      <![endif]-->
    </td>
  </tr>

	  
	  
  <tr>
    <td align="center" height="25" style="height:25px; font-size: 0;">&nbsp;</td>
  </tr>
  
  
  <tr>
    <td bgcolor="#eff5f1" align="center" style="padding: 0 10px 0 10px;">
      <!--[if (gte mso 9)|(IE)]>
      <table role="presentation" align="center" border="0" cellspacing="0" cellpadding="0" width="600">
      <tr>
      <td align="center" valign="top" width="600">
      <![endif]-->
      <table role="presentation" border="0" cellpadding="0" cellspacing="0" width="100%" style="max-width: 600px;" class="table-max">
        <tr>
          <td bgcolor="#eff5f1">
			<table role="presentation" width="100%" border="0" cellspacing="0" cellpadding="0">
              <tr>
                <td align="center" height="25" style="height:25px; font-size: 0;">&nbsp;</td>
              </tr>
              <tr>
                <td align="left">
                	<h2 style="font-family: Helvetica, Arial, sans-serif; font-size: 28px; font-weight:normal; color: #2C3E50; margin:0; mso-line-height-rule:exactly;">
                		${formatMessage("edit_content_properties")}
					</h2>
				</td>
              </tr>
              <tr>
                <td align="center" height="25" style="height:25px; font-size: 0;">&nbsp;</td>
              </tr>
              <tr>
                <td align="left" style="font-family: Helvetica, Arial, sans-serif; font-size: 16px; line-height: 25px; color: #2C3E50;">
				

					
					<#if document?exists>
       		 
			             
			             <#if document.properties['cm:name'] ? exists><b>${formatMessage("name")}: </b>${fullName(document.properties['cm:name'])}<br/></#if>
			             <#-- <#if document.properties['cm:creator'] ? exists><b>${formatMessage("creator")}: </b>${fullName(document.properties['cm:creator'])}<br/></#if> -->
			             <#-- <#if document.properties['cm:created'] ? exists><b>${formatMessage("created")}: </b>${document.properties['cm:created']?datetime}<br/></#if> -->
			             <#if document.properties['cm:modifier'] ? exists><b>${formatMessage("modifier")}: </b>${fullName(document.properties['cm:modifier'])}<br/></#if>
			             <#if document.properties['cm:modified'] ? exists><b>${formatMessage("modified")}: </b>${document.properties['cm:modified']?datetime}<br/></#if>
			             
			             <#-- <#if document.properties['cm:author'] ? exists><b>${formatMessage("author")}: </b>${document.properties['cm:author']}<br/></#if> -->
			             <#-- <#if document.properties['cm:title'] ? exists><b>${formatMessage("title")}: </b>${document.properties['cm:title']}<br/></#if> -->
			             <#-- <#if document.properties['sys:locale'] ? exists><b>${formatMessage("language")}: </b>${document.properties['sys:locale']}<br/></#if> -->
			             <#-- <#if document.properties['cm:description'] ? exists><b>${formatMessage("description")}: </b>${document.properties['cm:description']}<br/></#if> -->
			             <#-- <#if document.properties['cm:modifier'] ? exists><b>${formatMessage("modifier")}: </b>${fullName(document.properties['cm:modifier'])}<br/></#if> -->
			             <#-- <#if document.properties['cm:modified'] ? exists><b>${formatMessage("modified")}: </b>${document.properties['cm:modified']?datetime}<br/></#if> -->
			             <#-- <#if document.properties['ver:versionLabel'] ? exists><b>${formatMessage("versionLabel")}: </b>${document.properties['ver:versionLabel']}<br/></#if> -->
			             <#-- <#if document.properties['cd:expiration_date'] ? exists><b>${formatMessage("expiration_date")}: </b>${document.properties['cd:expiration_date']?date}<br/></#if> -->
			             <#-- <#if document.properties['cd:status'] ? exists><b>${formatMessage("bulk_upload_file_status")}: </b>${formatMessage(document.properties['cd:status']?lower_case)}<br/></#if> -->
			             <#-- <#if document.properties['cd:issue_date'] ? exists><b>${formatMessage("issue_date")}: </b>${document.properties['cd:issue_date']?date}<br/></#if> -->
			             <#-- <#if document.properties['cd:keyword'] ? exists><b>${formatMessage("keyword")}: </b><#list document.properties['cd:keyword'] as k>${k.properties.title} </#list><br/></#if> -->
			             <#-- <#if document.properties['cd:reference'] ? exists><b>${formatMessage("reference")}: </b>${document.properties['cd:reference']}<br/></#if> -->
			             
			             
			             
			             <b>${formatMessage("igroot_home_path")}: </b>${libraryPath(document)} <br/>
			             <#assign descriptions = dynamicPropertyDescriptions(document) >
			                         
			             <#list descriptions as label>
			             	<#assign i = "cd:dynAttr${label_index + 1}" >
						 	<b>${label}: </b><#if document.properties['${i}'] ? exists>${document.properties['${i}']}<br/><#else>&nbsp;<br/></#if>
						 </#list>  
			             
			             
			             <b>${formatMessage("notification_message_properties_direct_access_url")}: </b><a title="${document.name}" href="${libraryAccessUrl(document)}">${libraryAccessUrl(document)}</a><br/>
			             
			       <#else>
			             No document found!
			       </#if>
				
				</td>
              </tr>
            </table>
		  </td>
        </tr>
		 <tr>
			<td align="center" height="25" style="height:25px; font-size: 0;">&nbsp;</td>
		  </tr>
      </table>
      <!--[if (gte mso 9)|(IE)]>
      </td>
      </tr>
      </table>
      <![endif]-->
    </td>
  </tr>


<#-- ------------------------------------- -->
<#-- ------------END CONTENT-------------- -->
<#-- ------------------------------------- -->

<#include companyhome.childByNamePath["Data Dictionary/CircaBC/templates/mails/parts/mailfooter.ftl"].nodeRef>

</#compress>

