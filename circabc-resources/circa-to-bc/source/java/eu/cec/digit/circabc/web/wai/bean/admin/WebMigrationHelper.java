/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/

package eu.cec.digit.circabc.web.wai.bean.admin;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.web.bean.repository.MapNode;

import eu.cec.digit.circabc.web.WebClientHelper;
import eu.cec.digit.circabc.web.WebClientHelper.ExtendedURLMode;

/**
 * @author Yanick Pignot
 *
 */
abstract class WebMigrationHelper
{

	private WebMigrationHelper(){}

	public static final String computeHref(final NodeRef exportFileRef, final NodeService nodeService, final PermissionService permissionService)
	{
		return "<br />&nbsp;<ul><li><a href=\"" + WebClientHelper.getGeneratedWaiRelativeUrl(new MapNode(exportFileRef), ExtendedURLMode.HTTP_DOWNLOAD) + "\">" +
		nodeService.getPath(exportFileRef).toDisplayPath(nodeService, permissionService)+ "</a> </li></ul>";
	}

	public static final String computeFullHref(final NodeRef exportFileRef, final NodeService nodeService, final PermissionService permissionService)
	{
		return "<br />&nbsp;<ul><li><a href=\"" + WebClientHelper.getGeneratedWaiFullUrl(new MapNode(exportFileRef), ExtendedURLMode.HTTP_DOWNLOAD) + "\">" +
		nodeService.getPath(exportFileRef).toDisplayPath(nodeService, permissionService)+ "</a> </li></ul>";
	}
}
