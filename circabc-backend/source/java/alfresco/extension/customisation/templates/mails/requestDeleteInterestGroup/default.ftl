<#--  Copyright European Community 2006 - Licensed under the EUPL V.1.0  -->
<#--  			http://ec.europa.eu/idabc/en/document/6523				 -->

<#compress>

<#include companyhome.childByNamePath["Data Dictionary/CircaBC/templates/mails/parts/mailheader.ftl"].nodeRef>


<#-- ------------------------------------- -->
<#-- --------------CONTENT---------------- -->
<#-- ------------------------------------- -->


<tr>
    <td bgcolor="#ffffff" align="center" style="padding: 0 10px 0 10px;">
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
                      		${formatMessage("group_request_delete_template_mail_subject")}</h1>
                      </td>
                    </tr>
                    <tr>
                      <td align="center" height="25" style="height:25px; font-size: 0;">&nbsp;</td>
                    </tr>
                    <tr>
                      <td align="left" style="font-family: Helvetica, Arial, sans-serif; font-size: 16px; line-height: 25px; color: #2C3E50;"> 
						${formatMessage("mails_dear_category_admin")}
						<br/>
						<br/>
						${formatMessage("mails_delete_group_request_category_first_part")} ${interestGroupId}
						${formatMessage("mails_delete_group_request_category_segond_part")} (${categoryName})
						<br/>
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
                            	  <a href="${categoryGroupRequestAccessUrl(categRef)}" title="Read more" target="_blank" style="font-family: Helvetica, Arial, sans-serif; font-size: 16px; color: #ffffff; text-decoration: none; border-radius: 3px; padding: 15px 25px; border: 1px solid #8dcb6c; display: inline-block;" class="mobile-btn">
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
    </td>
  </tr>

  <tr>
    <td align="center" height="25" style="height:25px; font-size: 0;">&nbsp;</td>
  </tr>
  
  <tr>
    <td bgcolor="#eff5f1" align="center" style="padding: 0 10px 0 10px;">
      <table role="presentation" border="0" cellpadding="0" cellspacing="0" width="100%" style="max-width: 600px;" class="table-max">
        <tr>
          <td bgcolor="#eff5f1">
			<table role="presentation" width="100%" border="0" cellspacing="0" cellpadding="0">
              <tr>
                <td align="center" height="25" style="height:25px; font-size: 0;">&nbsp;</td>
              </tr>
               <tr>
                <td align="left" style="font-family: Helvetica, Arial, sans-serif; font-size: 16px; line-height: 25px; color: #2C3E50;">
                  		${formatMessage("mails_new_group_from_user")} <b>${fromUser.firstname} ${fromUser.lastname} - (${fromUser.email})</b>
					            ${formatMessage("mails_new_group_justification")}
				        </td>
              </tr>
              <tr>
                <td align="left" style="font-family: Helvetica, Arial, sans-serif; font-size: 16px; line-height: 25px; color: #2C3E50;">
					      <br/>
					      ${justification}
				        </td>
              </tr>
              <tr>
                <td align="center" height="25" style="height:25px; font-size: 0;">&nbsp;</td>
              </tr>
              <tr>
                <td align="left" style="font-family: Helvetica, Arial, sans-serif; font-size: 16px; line-height: 25px; color: #2C3E50;">
					<table>
						<tbody>
							<tr>
								<td align="left" style="font-family: Helvetica, Arial, sans-serif; font-size: 16px; line-height: 25px; color: #2C3E50; vertical-align: top;">
									<b>${formatMessage("name")}<b/>
								</td>
								<td align="left" style="font-family: Helvetica, Arial, sans-serif; font-size: 16px; line-height: 25px; color: #2C3E50;">
									${name}
								</td>
							</tr>
							<tr>
								<td align="left" style="font-family: Helvetica, Arial, sans-serif; font-size: 16px; line-height: 25px; color: #2C3E50; vertical-align: top;">
									<b>${formatMessage("title")}<b/>
								</td>
								<td align="left" style="font-family: Helvetica, Arial, sans-serif; font-size: 16px; line-height: 25px; color: #2C3E50;">
									<#if title?? && title != "null">
										${title}									
									</#if>
								</td>
							</tr>
							<tr>
								<td align="left" style="font-family: Helvetica, Arial, sans-serif; font-size: 16px; line-height: 25px; color: #2C3E50; vertical-align: top;">
									<b>${formatMessage("description")}<b/>
								</td>
								<td align="left" style="font-family: Helvetica, Arial, sans-serif; font-size: 16px; line-height: 25px; color: #2C3E50;">
									<#if description?? && description != "null">
										${description}
									</#if>		
								</td>
							</tr>
						</tbody>
					</table>
				</td>
              </tr>
            </table>
		  </td>
        </tr>
		 <tr>
			<td align="center" height="25" style="height:25px; font-size: 0;">&nbsp;</td>
		  </tr>
      </table>
    </td>
  </tr>


<#-- ------------------------------------- -->
<#-- ------------END CONTENT-------------- -->
<#-- ------------------------------------- -->

<#include companyhome.childByNamePath["Data Dictionary/CircaBC/templates/mails/parts/mailfooter.ftl"].nodeRef>

</#compress>