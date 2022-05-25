/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/
package eu.cec.digit.circabc.web.wai.bean.admin;

import java.io.FileInputStream;
import java.util.Collections;
import java.util.Map;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import org.alfresco.web.app.Application;
import org.alfresco.web.bean.repository.User;
import org.alfresco.web.ui.common.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import eu.cec.digit.circabc.business.api.BusinessStackError;
import eu.cec.digit.circabc.service.migration.ImportService;
import eu.cec.digit.circabc.service.migration.ImportationException;
import eu.cec.digit.circabc.web.Services;
import eu.cec.digit.circabc.web.bean.override.CircabcNavigationBean;
import eu.cec.digit.circabc.web.wai.bean.content.AddContentBean;
import eu.cec.digit.circabc.web.wai.bean.content.CircabcUploadedFile;

/**
 * Bean that back the importation file.
 *
 * @author Pignot Yanick
 */
public class UploadImportationFileBean extends AddContentBean
{

	private static final String FINISH_OUTCOME = "finish";
	private static final String RETRY = "retry";

	private static final String MSG_API_FILE = "upload_exportation_file_dialog_select_mainpage";
	private static final String MSG_LOGS_FILE = "upload_exportation_file_dialog_select_iglogs";

	private static final String MSG_TARGET_API = "upload_exportation_file_dialog_target_api";
	private static final String MSG_TARGET_LOGS = "upload_exportation_file_dialog_target_logs";
	
	/** */
	private static final long serialVersionUID = 1688168394578086333L;
	private static final Log logger = LogFactory.getLog(UploadImportationFileBean.class);

	private static final String ERROR_MSG = "upload_exportation_file_dialog_error";

	private transient ImportService importService;
	private String label;
	private String description;

	public void reset(ActionEvent event)
    {
    	this.init(null);
    }

	@Override
	public void init(final Map<String, String> parameters)
	{
		super.init(Collections.singletonMap(NODE_ID_PARAMETER, getManagementService().getCircabcNodeRef().getId()));
		
		label = null;
		description = null;
		
		if(!hasRight())
		{
			logger.warn("Security violation!!! User (" + ((CircabcNavigationBean)getNavigator()).getCurrentUserName() + ") tries to access to the importation page !");

			throw new IllegalStateException("You have not enough rights to perform this operation. ");
		}
	}

	public boolean hasRight()
	{
		final User user = getNavigator().getCurrentUser();
		return user.isAdmin();
	}

    @Override
    protected String finishImpl(final FacesContext context, String outcome) throws Exception
    {
    	try
    	{
	    	getImportService().storeNewImportFile(
	    			new FileInputStream(getUploadedFiles().get(0).getFile()),
	    			new FileInputStream(getUploadedFiles().get(1).getFile()),
	    			this.label,
	    			this.description);

	    	Utils.addStatusMessage(FacesMessage.SEVERITY_INFO , "Iteration '" + label + "' successfully created.");

	    	return FINISH_OUTCOME;
    	}
		catch(final BusinessStackError validationErrors)
		{
			for(final String msg: validationErrors.getI18NMessages())
			{
				Utils.addErrorMessage(msg);
			}
			this.isFinished = false;
			return null;
		}
		catch(final ImportationException ex)
    	{
    		Utils.addErrorMessage(Application.getMessage(context, ERROR_MSG) + ": " + ex.getMessage(), ex);
    		isFinished = false;

    		return RETRY;
    	}

    }

    public String getUploadPanelMessage()
    {
    	final FacesContext fc = FacesContext.getCurrentInstance();
    	if(getUploadedFileCount() == 0)
    	{
    		return Application.getMessage(fc, MSG_API_FILE);
    	}
    	else
    	{
    		return Application.getMessage(fc, MSG_LOGS_FILE);
    	}
    }

    @Override
	public void removeSelection(ActionEvent event)
	{
		// clear all uploaded file
    	clearUpload();
	}

	@Override
	public void addFile(CircabcUploadedFile fileBean)
	{
    	super.addFile(fileBean);
    	
    	if(fileBean != null)
    	{
    		// ugly but so easy solution to display wich kind of file is uploaded
    		
    		if(getUploadedFileCount() == 1)
    		{
    			fileBean.addSubmitedProperty("content-language", translate(MSG_TARGET_API));
    		}
    		else if(getUploadedFileCount() == 2)
    		{
    			fileBean.addSubmitedProperty("content-language", translate(MSG_TARGET_LOGS));
    		}     	
    	}
    	
	}
	
    @Override
    public String cancel()
    {
        super.cancel();

        return FINISH_OUTCOME;
    }
    
    @Override
    public String doPostCommitProcessing(final FacesContext context, final String outcome)
    {
        super.doPostCommitProcessing(context, outcome);

        return FINISH_OUTCOME;
    }

	/**
	 * @return the importService
	 */
	protected final ImportService getImportService()
	{
		if(importService == null)
		{
			importService = (ImportService) Services.getCircabcServiceRegistry(FacesContext.getCurrentInstance()).getService(ManageMigrationBaseBean.IMPORT_SERVICE);
		}
		return importService;
	}


	/**
	 * @param importService the importService to set
	 */
	public final void setImportService(ImportService importService)
	{
		this.importService = importService;
	}

	/**
	 * @return the description
	 */
	public final String getDescription()
	{
		return description;
	}

	/**
	 * @param description the description to set
	 */
	public final void setDescription(String description)
	{
		this.description = description;
	}

	/**
	 * @return the label
	 */
	public final String getLabel()
	{
		return label;
	}

	/**
	 * @param label the label to set
	 */
	public final void setLabel(String label)
	{
		this.label = label;
	}
}