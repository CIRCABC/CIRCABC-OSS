/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/

package eu.cec.digit.circabc.migration.validation.impl;

import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.jxpath.JXPathContext;

import eu.cec.digit.circabc.migration.entities.ElementsHelper;
import eu.cec.digit.circabc.migration.entities.XMLElement;
import eu.cec.digit.circabc.migration.entities.XMLNode;
import eu.cec.digit.circabc.migration.entities.generated.ImportRoot;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Category;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Circabc;
import eu.cec.digit.circabc.migration.entities.generated.nodes.InterestGroup;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Link;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Meeting;
import eu.cec.digit.circabc.migration.entities.generated.nodes.SharedSpacelink;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Space;
import eu.cec.digit.circabc.migration.entities.generated.properties.Shared;
import eu.cec.digit.circabc.migration.journal.MigrationTracer;
import eu.cec.digit.circabc.migration.validation.JXPathValidator;
import eu.cec.digit.circabc.model.CircabcModel;
import eu.cec.digit.circabc.repo.struct.SimplePath;
import eu.cec.digit.circabc.service.struct.ManagementService;

/**
 * Validate the node references setted in the given object graph
 *
 * @author yanick pignot
 */
public class NodeReferencesValidator extends JXPathValidator
{
    private static final String IG_REFERENCE = ".//*[igReference]";
    private static final String LIBRARY_SECTION = ".//*[librarySection]";
    private static final String REFERENCE = ".//*[reference]";

    public static final String CIRCABC_REFERENCE = SimplePath.PATH_SEPARATOR + "Circabc";

    private enum TargetLocation
    {
        REPOSITORY,
        XML,
        NOT_FOUND
    }


    @SuppressWarnings("unchecked")
    @Override
    protected void validateCircabcImpl(final JXPathContext context, final MigrationTracer<ImportRoot> journal) throws Exception
    {
    	for(final XMLNode node: (List<XMLNode>) context.selectNodes(REFERENCE))
        {
    		// the result is polluate with the reference property of nodes
    		if(node instanceof Link)
    		{
    			final Link link = (Link) node;

    			final String reference = link.getReference();
	            final TargetLocation location = getTargetLocation(context, reference);
	            mustExists(link, reference, location, journal);

	            final String elementIGPath = getSimpleInterestGroupPath(context, ElementsHelper.getXPath(link), TargetLocation.XML);
	            final String referenceIGPath = getSimpleInterestGroupPath(context, reference, location);

	            if(link instanceof SharedSpacelink)
	            {
	            	final String elementCategoryPath = getSimpleCategoryPath(context, ElementsHelper.getXPath(link), TargetLocation.XML);
	                final String referenceCategoryPath = getSimpleCategoryPath(context, reference, location);

	            	// must be in another interest group
	                mustBeInAnIG(referenceIGPath, link, reference, journal);
	                mustBeDifferentIG(elementIGPath, referenceIGPath, link, reference, journal);
	                mustBeSameCategory(elementCategoryPath, referenceCategoryPath, link, reference, journal);
	                // must be a library space
	                mustBeLibraySpace(context, link, reference, location, journal);
	                // must be shared to the current interest group
	                mustBeShared(context, link, elementIGPath, reference, location, journal);
	            }
	            else
	            {
	            	// must be in the same interest group
	                mustBeInAnIG(referenceIGPath, link, reference, journal);
	                mustBeSameIG(elementIGPath, referenceIGPath, link, reference, journal);
	            }
    		}
        }

        for(final Meeting meeting: (List<Meeting>) context.selectNodes(LIBRARY_SECTION))
        {
            final String reference = meeting.getLibrarySection();
            final TargetLocation location = getTargetLocation(context, reference);
            mustExists(meeting, reference, location, journal);

            final String elementIGPath = getSimpleInterestGroupPath(context, ElementsHelper.getXPath(meeting), TargetLocation.XML);
            final String referenceIGPath = getSimpleInterestGroupPath(context, reference, location);

            // must be in the same interest group
			mustBeInAnIG(referenceIGPath, meeting, reference, journal);
			mustBeSameIG(elementIGPath, referenceIGPath, meeting, reference, journal);
            // must be a library space
			mustBeLibraySpace(context, meeting, reference, location, journal);
        }

        for(final Shared shared: (List<Shared>) context.selectNodes(IG_REFERENCE))
        {
            final String reference = shared.getIgReference();
            final TargetLocation location = getTargetLocation(context, reference);
            mustExists(shared, reference, location, journal);

            final String elementIGPath = getSimpleInterestGroupPath(context, ElementsHelper.getXPath(shared), TargetLocation.XML);
            final String referenceIGPath = getSimpleInterestGroupPath(context, reference, location);

            // must be in an interest group
	        mustBeInAnIG(referenceIGPath, shared, reference, journal);
            // must be an interest group different that the link's one.
            mustBeDifferentIG(elementIGPath, referenceIGPath, shared, reference, journal);
			mustBeAnIg(context, shared, reference, location, journal);
        }
    }

    private TargetLocation getTargetLocation(final JXPathContext context, final String reference)
    {
        if(getTargetElment(context, reference) != null)
        {
           return TargetLocation.XML;
        }
        else if(isReferenceExistsInRepository(reference))
        {
           return TargetLocation.REPOSITORY;
        }
        else
        {
           return TargetLocation.NOT_FOUND;
        }
    }

	private void mustExists(final XMLElement element, final String reference, final TargetLocation location, final MigrationTracer<ImportRoot> journal)
    {
    	switch (location)
		{
			case XML:
				debug(journal, "Reference successfully found in the XML.", reference, element);
				break;
			case REPOSITORY:
				debug(journal, "Reference successfully found in the repository.", reference, element);
				break;
			default:
				debug(journal, "Reference can't be found either in the repository either in the xml file.", reference, element);
		}
	}



    private void mustBeInAnIG(final String simpleIGPath, final XMLElement element, final String reference, final MigrationTracer<ImportRoot> journal)
    {
        if(simpleIGPath == null)
        {
            debug(journal, "The reference must be included in an interest group", reference, element);
        }
        else
        {
        	debug(journal, "Reference successfully located under an InterestGroup", reference);
        }
    }

    private void mustBeDifferentIG(final String elementIGPath, final String referenceIGPath, final XMLElement element, final String reference, final MigrationTracer<ImportRoot> journal)
    {
        if(elementIGPath != null && referenceIGPath != null && referenceIGPath.equals(elementIGPath))
        {
            debug(journal, "This kind of link can only refer a node another InterestGroup",
                            "Interest Group" + elementIGPath ,
                            element);
        }
        else
        {
        	debug(journal, "Reference successfully located under a different InterestGroup",
        		"Link Interest Group: " + elementIGPath,
        		"Reference Interest Group: " + referenceIGPath,
        		reference);
        }
    }

    private void mustBeSameIG(final String elementIGPath, final String referenceIGPath, final XMLElement element, final String reference, final MigrationTracer<ImportRoot> journal)
    {
        if(elementIGPath == null || referenceIGPath == null || !referenceIGPath.equals(elementIGPath))
        {
            debug(journal, "This kind of link can only refer a node in the same InterestGroup",
                            "Link Interest Group:  " + elementIGPath ,
                            "Target Interest Group:  " + referenceIGPath,
                            element);
        }
        else
        {
        	debug(journal, "Reference successfully located under the same InterestGroup",
        		"Interest Group: " + elementIGPath,
        		reference);
        }
    }

    private void mustBeSameCategory(final String elementCatgoryPath, final String referenceCatgoryPath, final XMLElement element, final String reference, final MigrationTracer<ImportRoot> journal)
    {
        if(elementCatgoryPath == null || referenceCatgoryPath == null || !referenceCatgoryPath.equals(elementCatgoryPath))
        {
            debug(journal, "This kind of link can only refer a node in the same Category",
                            "Link Category:  " + elementCatgoryPath ,
                            "Target Category:  " + referenceCatgoryPath,
                            element);
        }
        else
        {
        	debug(journal, "Reference successfully located under the same Category",
        		"Category: " + elementCatgoryPath,
        		reference);
        }
    }

    private void mustBeLibraySpace(final JXPathContext context, final XMLElement element, final String reference, final TargetLocation location, final MigrationTracer<ImportRoot> journal)
    {
        boolean valid = false;

        if(TargetLocation.XML.equals(location))
        {
            final XMLElement target = getTargetElment(context, reference);

            valid = target != null && target instanceof Space;

        }
        else if(TargetLocation.REPOSITORY.equals(location))
        {
            final NodeRef referenceRef = ElementsHelper.getRepositorySimplePath(getManagementService(), reference).getNodeRef();

            if(reference != null)
            {
            	valid = getNodeService().hasAspect(referenceRef, CircabcModel.ASPECT_LIBRARY);
            	valid = valid && getNodeService().getType(referenceRef).isMatch(ContentModel.TYPE_FOLDER);
            }
        }
        else
        {
            return;
        }

        if(!valid)
        {
            debug(journal, "This kind of link can only refer a space in the library", reference, element);
        }
        else
        {
        	debug(journal, "Reference type is successfully found being a library space", reference);
        }
    }

	private void mustBeAnIg(final JXPathContext context, final XMLElement element, final String reference, final TargetLocation location, final MigrationTracer<ImportRoot> journal)
    {
        boolean valid = false;

        if(TargetLocation.XML.equals(location))
        {
            final XMLElement target = getTargetElment(context, reference);

            valid = target != null && target instanceof InterestGroup;

        }
        else if(TargetLocation.REPOSITORY.equals(location))
        {
            final NodeRef referenceRef = ElementsHelper.getRepositorySimplePath(getManagementService(), reference).getNodeRef();

            if(reference != null)
            {
                valid = getNodeService().hasAspect(referenceRef, CircabcModel.ASPECT_IGROOT);
            }
        }
        else
        {
            return;
        }

        if(!valid)
        {
			debug(journal, "This kind of link can only refer an Interest group", reference, element);
        }
        else
        {
        	debug(journal, "Reference type is successfully found being a Interest group", reference);
        }
    }

    private void mustBeShared(final JXPathContext context, final XMLElement element, final String elementIGPath, final String reference, final TargetLocation location, final MigrationTracer<ImportRoot> journal) throws Exception
    {
        if(TargetLocation.XML.equals(location))
        {
            final XMLElement target = getTargetElment(context, reference);

            if(target instanceof Space)
            {
                final Space space = (Space) target;
                final List<Shared> shareds = space.getShareds();

				if(shareds == null || shareds.size() < 1)
				{
					debug(journal, "The target library space must be shared.", reference, element);
				}
				else if(elementIGPath != null)
				{
					Shared matched = null;
					String sharedIGPath = null;

					for(final Shared shared : shareds)
					{
						sharedIGPath = getSimpleInterestGroupPath(context, shared.getIgReference());

						if(elementIGPath.equals(sharedIGPath))
						{
							matched = shared;
							break;
						}
					}

					if(matched != null)
					{
						debug(journal, "The target space is successfully shared to the link's Interest Group with the permission " + matched.getPermission(),
								"Interest group of the link: " + elementIGPath,
								element);
					}
					else
					{
						debug(journal, "The target space is not shared to the interest group of the link ",
							"Interest group of the link: " + elementIGPath,
							element);
					}
				}
            }
            else
            {
            	// don't log, another method is in charge of this mustBeLibraySpace()
            }
        }
        else if(TargetLocation.REPOSITORY.equals(location))
        {
           	debug(journal, "Since the target interest group must be shared first, it is mpossible to create a link to an existing Interest Group", element);
        }
        else
        {
            return;
        }
    }

	private String getSimpleInterestGroupPath(final JXPathContext context, final String reference) throws Exception
	{
		return getSimpleInterestGroupPath(context, reference, getTargetLocation(context, reference));
	}

	private String getSimpleInterestGroupPath(final JXPathContext context, final String reference, TargetLocation location) throws Exception
    {
        if(TargetLocation.XML.equals(location))
        {
            final InterestGroup ig = ElementsHelper.getElementInterestGroup(getTargetElment(context, reference));
            final Category category = ElementsHelper.getElementCategory(ig);

            if(ig == null || category == null)
            {
                return null;
            }
            else
            {
                return CIRCABC_REFERENCE + SimplePath.PATH_SEPARATOR + category.getName().getValue() + SimplePath.PATH_SEPARATOR + ig.getName().getValue();
            }
        }
        else if(TargetLocation.REPOSITORY.equals(location))
        {
            final NodeRef referenceRef = ElementsHelper.getRepositorySimplePath(getManagementService(), reference).getNodeRef();
            final NodeRef ig = getManagementService().getCurrentInterestGroup(referenceRef);

            if(ig == null)
            {
                return null;
            }
            else
            {
                final SimplePath simplePath = getManagementService().getNodePath(ig);
                return CIRCABC_REFERENCE + SimplePath.PATH_SEPARATOR + simplePath.getParent().getName() + SimplePath.PATH_SEPARATOR + simplePath.getName();
            }
        }
        else
        {
            return null;
        }
    }

	private String getSimpleCategoryPath(final JXPathContext context, final String reference, TargetLocation location) throws Exception
    {
        if(TargetLocation.XML.equals(location))
        {
            final Category category = ElementsHelper.getElementCategory(getTargetElment(context, reference));

            if(category == null)
            {
                return null;
            }
            else
            {
                return CIRCABC_REFERENCE + SimplePath.PATH_SEPARATOR + category.getName().getValue();
            }
        }
        else if(TargetLocation.REPOSITORY.equals(location))
        {
            final NodeRef referenceRef = ElementsHelper.getRepositorySimplePath(getManagementService(), reference).getNodeRef();
            final NodeRef category = getManagementService().getCurrentCategory(referenceRef);

            if(category == null)
            {
                return null;
            }
            else
            {
                final SimplePath simplePath = getManagementService().getNodePath(category);
                return CIRCABC_REFERENCE + SimplePath.PATH_SEPARATOR + simplePath.getName();
            }
        }
        else
        {
            return null;
        }
    }

    private XMLElement getTargetElment(final JXPathContext context, final String reference)
    {
    	return ElementsHelper.getReferencedElement((Circabc) context.getContextBean(), reference);
    }

    private boolean isReferenceExistsInRepository(String nodeReference)
    {
        return ElementsHelper.isPathExistingInRepository(getManagementService(), nodeReference);
    }



    private final ManagementService getManagementService()
    {
        return getRegistry().getManagementService();
    }

	private final NodeService getNodeService()
	{
		return getRegistry().getAlfrescoServiceRegistry().getNodeService();
	}
}
