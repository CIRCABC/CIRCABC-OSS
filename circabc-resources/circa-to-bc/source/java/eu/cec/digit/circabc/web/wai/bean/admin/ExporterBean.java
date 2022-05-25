package eu.cec.digit.circabc.web.wai.bean.admin;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.ValueChangeEvent;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.ISO9075;
import org.alfresco.web.ui.common.Utils;

import de.schlichtherle.io.File;
import eu.cec.digit.circabc.util.exporter.ExportActionExecuter;
import eu.cec.digit.circabc.util.exporter.ExportItemData;
import eu.cec.digit.circabc.web.wai.dialog.BaseWaiDialog;

/**
 * Bean to implement the export admin screen.
 * 
 * @author schwerr
 */
public class ExporterBean extends BaseWaiDialog {
	
	private static final long serialVersionUID = 8516088493963408026L;
	
	private final static String SELECT_IG_NONE = "--";
	
	private ActionService actionService = null;
	
	private static Map<String, Action> actionMap = new HashMap<String, Action>();
	
	private Map<String, Object> categories = null; 
	private String selectedCategory = null;
	
	private Map<String, Object> igs = null; 
	private List<String> selectedIG = null;
	
	private static String exportDirectory = "/ec/local/weblogic/app/exports";
	
	private boolean exportAuthorities = true;
	private boolean exportStructure = true;
	private boolean exportHeaders = false;
	private boolean exportOnlyRoot = false;
	
	private Date exportStartDate = null;
	
	/**
	 * Initializes the system. Fills the category/IG collections for the first 
	 * time.
	 */
	public void init(final Map<String, String> parameters) {
		
		super.init(parameters);
		
		categories = selectCategories();
		
		igs = selectIGs(selectedCategory);
		
		exportStartDate = new Date();
	}
	
	/**
	 * Retrieves all the categories and fills a map for the select combo boxes.
	 * 
	 * @return
	 */
	private Map<String, Object> selectCategories() {
		
		categories = new TreeMap<String, Object>();
		
		List<NodeRef> categoryNodeRefs = getManagementService().getCategories();
		
		for (NodeRef categoryNodeRef : categoryNodeRefs) {
			
			String title = (String) getNodeService().getProperty(categoryNodeRef, 
							ContentModel.PROP_TITLE);
			
			categories.put(title, categoryNodeRef.toString());
			
			// ECHA as default (remove when ready)
			if ("European Chemicals Agency".equals(title)) {
				selectedCategory = categoryNodeRef.toString();
			}
		}
		
		if (categories.entrySet().size() > 0 && selectedCategory == null) {
			selectedCategory = (String) categories.entrySet().iterator().next().getValue();
		}
		
		return categories;
	}
	
	/**
	 * Executed when a category is selected to update the IG list (for that 
	 * selected category)
	 * 
	 * @param event
	 */
	public void selectIgs(ValueChangeEvent event) {
		selectIGs((String) event.getNewValue());
	}
	
	/**
	 * Selects all IGs corresponding to the given category (noderef)
	 * 
	 * @param categoryNodeRef
	 * @return
	 */
	private Map<String, Object> selectIGs(String categoryNodeRef) {
		
		igs = new TreeMap<String, Object>();
		igs.put(SELECT_IG_NONE, SELECT_IG_NONE);
		if (selectedIG == null) {
			selectedIG = new ArrayList<String>();
			selectedIG.add(SELECT_IG_NONE);
		}
		
		if (categoryNodeRef != null) {
			
			List<NodeRef> igNodeRefs = getManagementService().getInterestGroups(new NodeRef(categoryNodeRef));
			
			for (NodeRef igNodeRef : igNodeRefs) {
				
				String title = (String) getNodeService().getProperty(igNodeRef, 
								ContentModel.PROP_TITLE);
				
				igs.put(title, igNodeRef.toString());
			}
		}
		
		return igs;
	}
	
	/* (non-Javadoc)
	 * @see eu.cec.digit.circabc.web.wai.dialog.WaiDialog#getPageIconAltText()
	 */
	@Override
	public String getPageIconAltText() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see eu.cec.digit.circabc.web.wai.dialog.WaiDialog#getBrowserTitle()
	 */
	@Override
	public String getBrowserTitle() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Calls the export action for the export to be executed in background.
	 * 
	 * @see org.alfresco.web.bean.dialog.BaseDialogBean#finishImpl(javax.faces.context.FacesContext, java.lang.String)
	 */
	@Override
	protected String finishImpl(FacesContext context, String outcome)
			throws Throwable {
		
		if (selectedCategory != null || (selectedIG != null && !selectedIG.isEmpty())) {
			
			Action action = actionService.createAction(ExportActionExecuter.NAME);
			
			action.setParameterValue(
					ExportActionExecuter.PARAM_EXPORT_DIRECTORY, exportDirectory);
			action.setParameterValue(
					ExportActionExecuter.PARAM_EXPORT_AUTHORITIES, exportAuthorities);
			action.setParameterValue(
					ExportActionExecuter.PARAM_EXPORT_STRUCTURE, exportStructure);
			action.setParameterValue(
					ExportActionExecuter.PARAM_EXPORT_HEADERS, exportHeaders);
			action.setParameterValue(
					ExportActionExecuter.PARAM_EXPORT_ONLY_ROOT, exportOnlyRoot);
			action.setParameterValue(
					ExportActionExecuter.PARAM_EXPORT_START, exportStartDate);
			
			List<ExportItemData> exportItemsData = new ArrayList<ExportItemData>();
			
			// Choose between Category or IG
			if (selectedIG == null || selectedIG.isEmpty() || 
					(selectedIG.size() == 1 && SELECT_IG_NONE.equals(selectedIG.get(0)))) {
				ExportItemData exportItemData = buildExportItemData(selectedCategory);
				exportItemsData.add(exportItemData);
				actionMap.put(exportItemData.getName(), action);
			}
			else {
				String names = "";
				for (String selectedIg : selectedIG) {
					if (!SELECT_IG_NONE.equals(selectedIg)) {
						ExportItemData exportItemData = buildExportItemData(selectedIg);
						exportItemsData.add(exportItemData);
						names += exportItemData.getName() + " ";
					}
				}
				actionMap.put(names, action);
			}
			
			action.setParameterValue(ExportActionExecuter.PARAM_ITEMS_TO_EXPORT, 
										(Serializable) exportItemsData);
			
			actionService.executeAction(action, null, false, true);
		}
		
		return "finish";
	}
	
	/**
	 * Builds the object that keeps the name and the noderef to export.
	 * 
	 * @param action
	 * @param selectedItem
	 */
	protected ExportItemData buildExportItemData(String selectedItem) {
		
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmssS");
		String formattedExportDate = simpleDateFormat.format(new Date());
		
		NodeRef itemNodeRef = new NodeRef(selectedItem);
		
		String name = (String) getNodeService().getProperty(itemNodeRef, 
				ContentModel.PROP_TITLE);
		
		String completeName = ISO9075.encode(name + "-" + formattedExportDate);
		
		return new ExportItemData(completeName, itemNodeRef);
	}
	
	/**
	 * Does the final validation before finishing the implementation 
	 * (finishImpl)
	 * 
	 * @see org.alfresco.web.bean.dialog.BaseDialogBean#finish()
	 */
	@Override
	public String finish() {
		
		// Check if the export directory is valid
		if (!(new File(exportDirectory).exists())) {
			Utils.addErrorMessage(translate("migration_export_category_ig_dialog_page_error_directory"));
			return null;
		}
		
		return super.finish();
	}

	public void reset(ActionEvent event) {
		init(null);
	}
	
	/**
	 * Returns a string with all actions and current statuses.
	 */
	public String getWorkingExportNames() {
		
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
		
		String statuses = "<br/><br/>";
		
		Iterator<Map.Entry<String, Action>> iterator = actionMap.entrySet().iterator();
		
		while (iterator.hasNext()) {
			
			Map.Entry<String, Action> actionEntry = iterator.next();
			
			String status = (String) actionEntry.getValue().getParameterValue(
								ExportActionExecuter.PARAM_CURRENT_STATUS);
			
			Date startDate = (Date) actionEntry.getValue().getParameterValue(
					ExportActionExecuter.PARAM_START_DATE);
			
			statuses += actionEntry.getKey() + " [" + 
					((startDate != null) ? simpleDateFormat.format(startDate) : "") + 
						"] -> " + status;
			
			String executionTime = (String) actionEntry.getValue().getParameterValue(
					ExportActionExecuter.PARAM_EXE_TIME);
			
			if (executionTime != null) {
				statuses += " (Done in " + executionTime + ".)";
			}
			
			if (iterator.hasNext()) {
				statuses += "<br/>";
			}
		}
		
		if ("<br/><br/>".equals(statuses)) {
			statuses = "-";
		}
		
		return statuses;
	}
	
	/**
	 * Gets the value of the categories
	 * 
	 * @return the categories
	 */
	public Map<String, Object> getCategories() {
		return categories;
	}

	/**
	 * Sets the value of the categories
	 * 
	 * @param categories the categories to set.
	 */
	public void setCategories(Map<String, Object> categories) {
		this.categories = categories;
	}

	/**
	 * Gets the value of the igs
	 * 
	 * @return the igs
	 */
	public Map<String, Object> getIgs() {
		return igs;
	}

	/**
	 * Sets the value of the igs
	 * 
	 * @param igs the igs to set.
	 */
	public void setIgs(Map<String, Object> igs) {
		this.igs = igs;
	}

	/**
	 * Gets the value of the selectedCategory
	 * 
	 * @return the selectedCategory
	 */
	public String getSelectedCategory() {
		return selectedCategory;
	}

	/**
	 * Sets the value of the selectedCategory
	 * 
	 * @param selectedCategory the selectedCategory to set.
	 */
	public void setSelectedCategory(String selectedCategory) {
		this.selectedCategory = selectedCategory;
	}

	/**
	 * Gets the value of the selectedIG
	 * 
	 * @return the selectedIG
	 */
	public List<String> getSelectedIG() {
		return selectedIG;
	}

	/**
	 * Sets the value of the selectedIG
	 * 
	 * @param selectedIG the selectedIG to set.
	 */
	public void setSelectedIG(List<String> selectedIG) {
		this.selectedIG = selectedIG;
	}

	/**
	 * Gets the value of the exportDirectory
	 * 
	 * @return the exportDirectory
	 */
	public String getExportDirectory() {
		return exportDirectory;
	}
	
	/**
	 * Sets the value of the exportDirectory
	 * 
	 * @param exportDirectory the exportDirectory to set.
	 */
	public void setExportDirectory(String anExportDirectory) {
		exportDirectory = anExportDirectory;
	}
	
	/**
	 * Sets the value of the actionService
	 * 
	 * @param actionService the actionService to set.
	 */
	public void setActionService(ActionService actionService) {
		this.actionService = actionService;
	}
	
	/**
	 * Gets the value of the exportAuthorities
	 * 
	 * @return the exportAuthorities
	 */
	public boolean isExportAuthorities() {
		return exportAuthorities;
	}
	
	/**
	 * Sets the value of the exportAuthorities
	 * 
	 * @param exportAuthorities the exportAuthorities to set.
	 */
	public void setExportAuthorities(boolean exportAuthorities) {
		this.exportAuthorities = exportAuthorities;
	}
	
	/**
	 * Gets the value of the exportStructure
	 * 
	 * @return the exportStructure
	 */
	public boolean isExportStructure() {
		return exportStructure;
	}
	
	/**
	 * Sets the value of the exportStructure
	 * 
	 * @param exportStructure the exportStructure to set.
	 */
	public void setExportStructure(boolean exportStructure) {
		this.exportStructure = exportStructure;
	}
	
	/**
	 * Gets the value of the exportHeaders
	 * 
	 * @return the exportHeaders
	 */
	public boolean isExportHeaders() {
		return exportHeaders;
	}
	
	/**
	 * Sets the value of the exportHeaders
	 * 
	 * @param exportHeaders the exportHeaders to set.
	 */
	public void setExportHeaders(boolean exportHeaders) {
		this.exportHeaders = exportHeaders;
	}
	
	/**
	 * Gets the value of the exportOnlyRoot
	 * 
	 * @return the exportOnlyRoot
	 */
	public boolean isExportOnlyRoot() {
		return exportOnlyRoot;
	}
	
	/**
	 * Sets the value of the exportOnlyRoot
	 * 
	 * @param exportOnlyRoot the exportOnlyRoot to set.
	 */
	public void setExportOnlyRoot(boolean exportOnlyRoot) {
		this.exportOnlyRoot = exportOnlyRoot;
	}
	
	/**
	 * Gets the value of the exportStartDate
	 * 
	 * @return the exportStartDate
	 */
	public Date getExportStartDate() {
		return exportStartDate;
	}
	
	/**
	 * Sets the value of the exportStartDate
	 * 
	 * @param exportStartDate the exportStartDate to set.
	 */
	public void setExportStartDate(Date exportStartDate) {
		this.exportStartDate = exportStartDate;
	}
}
