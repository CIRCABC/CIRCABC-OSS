/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/

package eu.cec.digit.circabc.web.wai.bean.admin;

import java.text.MessageFormat;
import java.util.Date;
import java.util.Map;

import javax.faces.context.FacesContext;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.web.app.Application;

import eu.cec.digit.circabc.migration.archive.MigrationIteration;

/**
 * @author Yanick Pignot
 *
 */
public final class WebIteration implements MigrationIteration
{
	/** */
	private static final long serialVersionUID = -9194025433832049316L;
	private MigrationIteration iterationDelegate;

	private static final String MSG_X_TIMES = "manage_importations_dialog_page_x_times";

	public WebIteration(final MigrationIteration iterationDelegate)
	{
		this.iterationDelegate = iterationDelegate;
	}

	public String getTransformationDatesSize()
	{
		return getXTime(iterationDelegate.getTransformationDates().size());
	}


	public String getImportedDatesSize()
	{
		return getXTime(iterationDelegate.getImportedDates().size());
	}

	public String getFailedImportationSize()
	{
		return getXTime(iterationDelegate.getFailedImportation().size());
	}

	private String getXTime(final int value)
	{
		final FacesContext fc = FacesContext.getCurrentInstance();
		return MessageFormat.format(Application.getMessage(fc, MSG_X_TIMES), value);
	}

	/**
	 * @return
	 * @see eu.cec.digit.circabc.migration.archive.MigrationIteration#getCreator()
	 */
	public String getCreator()
	{
		return iterationDelegate.getCreator();
	}

	/**
	 * @return
	 * @see eu.cec.digit.circabc.migration.archive.MigrationIteration#getDescription()
	 */
	public String getDescription()
	{
		return iterationDelegate.getDescription();
	}

	/**
	 * @return
	 * @see eu.cec.digit.circabc.migration.archive.MigrationIteration#getIdentifier()
	 */
	public String getIdentifier()
	{
		return iterationDelegate.getIdentifier();
	}

	/**
	 * @return
	 * @see eu.cec.digit.circabc.migration.archive.MigrationIteration#getImportedDates()
	 */
	public Map<NodeRef, Date> getImportedDates()
	{
		return iterationDelegate.getImportedDates();
	}

	/**
	 * @return
	 * @see eu.cec.digit.circabc.migration.archive.MigrationIteration#getIterationStartDate()
	 */
	public Date getIterationStartDate()
	{
		return iterationDelegate.getIterationStartDate();
	}

	/**
	 * @return
	 * @see eu.cec.digit.circabc.migration.archive.MigrationIteration#getOriginalFileNodeRef()
	 */
	public NodeRef getOriginalFileNodeRef()
	{
		return iterationDelegate.getOriginalFileNodeRef();
	}

	/**
	 * @return
	 * @see eu.cec.digit.circabc.migration.archive.MigrationIteration#getTransformationDates()
	 */
	public Map<NodeRef, Date> getTransformationDates()
	{
		return iterationDelegate.getTransformationDates();
	}


	/**
	 * @return
	 * @see eu.cec.digit.circabc.migration.archive.MigrationIteration#getFailedImportation()
	 */
	public Map<NodeRef, Date> getFailedImportation()
	{
		return iterationDelegate.getFailedImportation();
	}

	public NodeRef getIterationRootSpace()
	{
		return iterationDelegate.getIterationRootSpace();
	}

	public NodeRef getOriginalIgLogsFileNodeRef()
	{
		return iterationDelegate.getOriginalIgLogsFileNodeRef();
	}


}
