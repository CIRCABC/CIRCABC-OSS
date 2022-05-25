/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/

package eu.cec.digit.circabc.migration.journal.etl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;

import eu.cec.digit.circabc.migration.archive.MigrationIteration;
import eu.cec.digit.circabc.migration.entities.generated.aida.Users.User;

/**
 * @author Yanick Pignot
 */
public class ETLReport implements Serializable
{

	private static final String MAKE_INVALID_BY_USER = "Make invalid by user.";

	/** */
	private static final long serialVersionUID = -5143109559090185233L;

	private NodeRef originalImportRoot;
	private NodeRef validImportRoot;
	private NodeRef residualImportRoot;
	private List<TransformationElement> transformationElements;
	private List<PathologicUser> pathologicUsers;
	private MigrationIteration iteration;

	/**
	 * @return the pathologicUsers
	 */
	public final List<PathologicUser> getPathologicUsers()
	{
		if(pathologicUsers == null)
		{
			pathologicUsers = new ArrayList<PathologicUser>();
		}
		return pathologicUsers;
	}

	/**
	 * @param pathologicUsers the pathologicUsers to set
	 */
	public final void withPathologicUsers(PathologicUser pathologicUser)
	{
		getPathologicUsers().add(pathologicUser);
	}

	/**
	 * @return the transformationElements
	 */
	public final List<TransformationElement> getTransformationElements()
	{
		if(transformationElements == null)
		{
			transformationElements = new ArrayList<TransformationElement>();
		}
		return transformationElements;
	}

	/**
	 * @param transformationElements the transformationElements to set
	 */
	public final void withTransformationElements(TransformationElement transformationElement)
	{
		getTransformationElements().add(transformationElement);
	}


	public PathologicUser makePathologic(final TransformationElement element)
	{
		return makePathologic(element, MAKE_INVALID_BY_USER);
	}

	public PathologicUser makePathologic(final TransformationElement element, final String message)
	{
		int idx = -1;
		int userHash = element.hashCode();

		for(int x = 0; x < getTransformationElements().size(); ++x)
		{
			if(getTransformationElements().get(x).hashCode() == userHash)
			{
				idx  = x;
				break;
			}
		}

		if(idx < 0)
		{
			throw new IllegalArgumentException("Impossible to make this user pathologic. It is not contained in the valid list. Username " + element.getOriginalPerson().getUserId().getValue());
		}

		getTransformationElements().remove(idx);
		final PathologicUser pathologicUser = new PathologicUser(element.originalPerson, message, Collections.<String, User>singletonMap(element.getValidUserId(), element.getValidUserData()), element.originalPerson == null);
		withPathologicUsers(pathologicUser);
		return pathologicUser;
	}

	public TransformationElement makeValide(final PathologicUser user, final String validUsername)
	{
		removePathologicalUser(user);
		final TransformationElement transformationElement = new TransformationElement(user.getPerson(), validUsername, user.getPropositions().get(validUsername));
		withTransformationElements(transformationElement);

		return transformationElement;
	}

	/**
	 * @param user
	 */
	public void removePathologicalUser(final PathologicUser user)
	{
		int idx = -1;
		int userHash = user.hashCode();

		for(int x = 0; x < getPathologicUsers().size(); ++x)
		{
			if(getPathologicUsers().get(x).hashCode() == userHash)
			{
				idx  = x;
				break;
			}
		}

		if(idx < 0)
		{
			throw new IllegalArgumentException("Impossible to make this user valid. It is not contained in the pathologic list. Username " + user.getPerson().getUserId().getValue());
		}

		getPathologicUsers().remove(idx);
	}

	/**
	 * @return the residualImportRoot
	 */
	public final NodeRef getResidualImportRoot()
	{
		return residualImportRoot;
	}

	/**
	 * @param residualImportRoot the residualImportRoot to set
	 */
	public final void setResidualImportRoot(NodeRef residualImportRoot)
	{
		this.residualImportRoot = residualImportRoot;
	}

	/**
	 * @return the validImportRoot
	 */
	public final NodeRef getValidImportRoot()
	{
		return validImportRoot;
	}

	/**
	 * @param validImportRoot the validImportRoot to set
	 */
	public final void setValidImportRoot(NodeRef validImportRoot)
	{
		this.validImportRoot = validImportRoot;
	}

	/**
	 * @return the originalImportRoot
	 */
	public final NodeRef getOriginalImportRoot()
	{
		return originalImportRoot;
	}

	/**
	 * @param originalImportRoot the originalImportRoot to set
	 */
	public final void setOriginalImportRoot(NodeRef originalImportRoot)
	{
		if(this.originalImportRoot != null)
		{
			throw new IllegalAccessError("The originalImportRoot is read only");
		}
		this.originalImportRoot = originalImportRoot;
	}

	/**
	 * @return the iteration
	 */
	public final MigrationIteration getIteration()
	{
		return iteration;
	}

	/**
	 * @param iteration the iteration to set
	 */
	public final void setIteration(MigrationIteration iteration)
	{
		if(this.iteration != null)
		{
			throw new IllegalAccessError("The iterationName is read only");
		}

		this.iteration = iteration;
	}

}