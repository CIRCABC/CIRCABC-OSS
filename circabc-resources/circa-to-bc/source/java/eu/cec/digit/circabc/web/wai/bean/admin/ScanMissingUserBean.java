package eu.cec.digit.circabc.web.wai.bean.admin;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.repository.NodeRef;

import eu.cec.digit.circabc.migration.post.cleaning.MissingUserReferrenceAction;
import eu.cec.digit.circabc.web.wai.dialog.BaseWaiDialog;

public class ScanMissingUserBean extends BaseWaiDialog
{

	private ActionService actionService;
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 5774135604228010671L;

	@Override
	public String getPageIconAltText() {
		
		return "Scan Missing Users";
	}

	@Override
	public String getBrowserTitle() {

		return "Scan Missing Users";
	}

	@Override
	protected String finishImpl(FacesContext arg0, String arg1)
			throws Throwable {

		NodeRef circabcNodeRef = getManagementService().getCircabcNodeRef();
		        
        Action action = getActionService().createAction(MissingUserReferrenceAction.ACTION_NAME, null);
        action.setExecuteAsynchronously(true);
        getActionService().executeAction(action, circabcNodeRef);
		
		return "finish";
	}
	
	public void reset(ActionEvent event) {
		this.init(null);
	}
	
	@Override
	public String getFinishButtonLabel() {
		return "Launch";
	}
	
	@Override
	public String getCancelButtonLabel() {
		return "Cancel";
	}

	/**
	 * @return the actionService
	 */
	public ActionService getActionService() {
		return actionService;
	}

	/**
	 * @param actionService the actionService to set
	 */
	public void setActionService(ActionService actionService) {
		this.actionService = actionService;
	}

	

}
