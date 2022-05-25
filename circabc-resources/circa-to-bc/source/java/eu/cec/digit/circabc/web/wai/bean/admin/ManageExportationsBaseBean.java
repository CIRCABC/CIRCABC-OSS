/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/
package eu.cec.digit.circabc.web.wai.bean.admin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.faces.event.ActionEvent;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import javax.faces.model.SelectItem;

import org.alfresco.web.ui.common.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import eu.cec.digit.circabc.service.migration.CategoryInterestGroupPair;
import eu.cec.digit.circabc.service.migration.ExportationException;

/**
 * Bean that manages the exportation jobs.
 *
 * @author Pignot Yanick
 */
public abstract class ManageExportationsBaseBean extends ManageMigrationBaseBean
{
	private static final long serialVersionUID = 8511115643203147770L;
	private static final Log logger = LogFactory.getLog(ManageExportationsBaseBean.class);

	private static final String MSG_ERROR_LIST_IG = "manage_exportations_dialog_error_ig_list";
	private static final String MSG_ERROR_LIST_CAT = "manage_exportations_dialog_error_cat_list";
	private static final String MSG_WARN_LIST_IG_DEPEDENCIES = "manage_exportations_dialog_warn_ig_depedencies";
	protected static final String MSG_ERROR_NO_IT_NAME = "manage_exportations_dialog_error_iterationname";
	protected static final String MSG_ERROR_BAD_IT_NAME = "manage_exportations_dialog_error_valid_iterationname";
	protected static final String MSG_ERROR_NO_IG = "manage_exportations_dialog_error_no_ig";

	private String iterationName;
	private String iterationDescription;
	private String selectedCategory;

	private transient DataModel igDataModel = null;
	private Set<String> dependenciesToWarn = null;
	private List<String> selectedInterestGroups = new ArrayList<String>();

	protected void resetState()
	{
		iterationName = null;
		iterationDescription = null;
		selectedCategory = null;
		igDataModel = null;
		selectedInterestGroups = new ArrayList<String>();
		dependenciesToWarn = new HashSet<String>();
		super.resetState();
	}

	public List<SelectItem> getCategories()
	{
		try
		{
			final List<String> categories = getExportService().getCategoryNames();
			final List<SelectItem> items = new ArrayList<SelectItem>(categories.size());

			for(final String cat: categories)
			{
				items.add(new SelectItem(cat, cat));
			}

			return items;
		}
		catch (final ExportationException e)
		{
			final String message = translate(MSG_ERROR_LIST_CAT);
			Utils.addErrorMessage(message, e);
			logger.error(message, e);
			return Collections.<SelectItem> emptyList();
		}
	}

	public List<SelectItem> getInterestGroups()
	{
		try
		{
			final List<SelectItem> items = new ArrayList<SelectItem>();

			if(selectedCategory != null)
			{
				final List<CategoryInterestGroupPair> allIgs = getExportService().getInterestGroups(selectedCategory);
				final List<String> selectedIgs = getSelectedInterestGroups();

				for(final CategoryInterestGroupPair pair: allIgs)
				{
					if(selectedIgs.contains(pair.toString()) == false)
					{
						items.add(new SelectItem(pair.toString(), pair.getInterestGroup()));
					}
				}
			}

			return items;
		}
		catch (final ExportationException e)
		{
			final String message = translate(MSG_ERROR_LIST_IG, selectedCategory);
			Utils.addErrorMessage(message, e);
			logger.error(message, e);
			return Collections.<SelectItem> emptyList();
		}
	}

	/**
	 * Change listener for the method select box
	 */
	public void updateInterestGroupList(ValueChangeEvent event)
    {
		selectedCategory = (String) event.getNewValue();
    }

	@SuppressWarnings("unchecked")
	public void updateSelectedIgList(ValueChangeEvent event)
    {
		Object value = event.getNewValue();
		final List<String> newIg;

		if(value instanceof List)
		{
			newIg = (List<String>) value;
		}
		else if(value instanceof String[])
		{
			newIg = Arrays.asList((String[]) value);
		}
		else
		{
			newIg = Collections.singletonList(value.toString());
		}

		if(newIg != null)
		{
			getSelectedInterestGroups().addAll(newIg);
		}

		validateDependencies();
    }

	public boolean isNeedDependecies()
	{
		return dependenciesToWarn.size() > 0;
	}

	public String getDependeciesMessage()
	{
		if(dependenciesToWarn.size() > 0)
		{

			final StringBuffer buff = new StringBuffer(translate(MSG_WARN_LIST_IG_DEPEDENCIES)).append("<br /><ul>");

			for(final String dep: dependenciesToWarn)
			{
				buff
					.append("<li>")
					.append(dep)
					.append("</li>");
			}

			buff.append("</ul>");

			return buff.toString();
		}
		else
		{
			return "";
		}

	}

	public void removeSelection(final ActionEvent event)
	{
		final String wrapper = (String) this.igDataModel.getRowData();
		if (wrapper != null)
		{
			getSelectedInterestGroups().remove(wrapper);
		}

		validateDependencies();
	}

	public final DataModel getInterestGroupDataModel()
	{
		if (this.igDataModel == null)
	    {
	        this.igDataModel = new ListDataModel();
	    }

	    this.igDataModel.setWrappedData(this.selectedInterestGroups);

	    return this.igDataModel;
	}

	private void validateDependencies()
	{
		final Set<CategoryInterestGroupPair> depedencies = new HashSet<CategoryInterestGroupPair>();
		dependenciesToWarn = new HashSet<String>();
		try
		{
			for(final String ig: getSelectedInterestGroups())
			{
				depedencies.addAll(
						getExportService().getInterestGroupDepedencies(CategoryInterestGroupPair.fromString(ig))
							);
			}

			for(final CategoryInterestGroupPair pair: depedencies)
			{
				if(getSelectedInterestGroups().contains(pair.toString()) == false)
				{
					dependenciesToWarn.add(pair.toString());
				}
			}

		}
		catch (ExportationException e)
		{
			Utils.addErrorMessage("Problem accessing to circa: " + e.getMessage(), e);
		}
	}

	protected final List<String> getSelectedInterestGroups()
	{
		if(selectedInterestGroups == null)
		{
			selectedInterestGroups = new ArrayList<String>();
		}
		return selectedInterestGroups;
	}

	/**
	 * @return the iterationDescription
	 */
	public final String getIterationDescription()
	{
		return iterationDescription;
	}

	/**
	 * @param iterationDescription the iterationDescription to set
	 */
	public final void setIterationDescription(String iterationDescription)
	{
		this.iterationDescription = iterationDescription;
	}

	/**
	 * @return the iterationName
	 */
	public final String getIterationName()
	{
		return iterationName;
	}

	/**
	 * @param iterationName the iterationName to set
	 */
	public final void setIterationName(String iterationName)
	{
		this.iterationName = iterationName;
	}

	public final String getSelectedCategory()
	{
		return selectedCategory;
	}

	public final void setSelectedCategory(String selectedCategory)
	{
		this.selectedCategory = selectedCategory;
	}


}