/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/

package eu.cec.digit.circabc.migration.processor.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.favourites.FavouritesService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import eu.cec.digit.circabc.migration.entities.ElementsHelper;
import eu.cec.digit.circabc.migration.entities.generated.ImportRoot;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Category;
import eu.cec.digit.circabc.migration.entities.generated.nodes.CategoryHeader;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Circabc;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Content;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Dossier;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Forum;
import eu.cec.digit.circabc.migration.entities.generated.nodes.InfContent;
import eu.cec.digit.circabc.migration.entities.generated.nodes.InfMLContent;
import eu.cec.digit.circabc.migration.entities.generated.nodes.InfSpace;
import eu.cec.digit.circabc.migration.entities.generated.nodes.InterestGroup;
import eu.cec.digit.circabc.migration.entities.generated.nodes.MlContent;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Node;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Space;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Topic;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Url;
import eu.cec.digit.circabc.migration.entities.generated.user.Person;
import eu.cec.digit.circabc.migration.entities.generated.user.Persons;
import eu.cec.digit.circabc.migration.journal.MigrationTracer;
import eu.cec.digit.circabc.migration.processor.PostProcessor;
import eu.cec.digit.circabc.service.CircabcServiceRegistry;
import eu.cec.digit.circabc.service.migration.ImportationException;

/**
 * Post-processor that restores user favourites after all nodes have been imported.
 *
 * <p>During export, each person's favourites (within the exported IG) are stored as
 * nodeRef strings from the source environment. During import, this processor builds
 * a mapping of old exportation paths to new nodeRefs by walking the import tree,
 * then calls FavouritesService.addFavourite to restore each user's favourites
 * on the target environment.</p>
 */
public class MigrateFavourites implements PostProcessor
{
    private static final Log logger = LogFactory.getLog(MigrateFavourites.class);

    @Override
    public void afterProcess(final CircabcServiceRegistry registry, final ImportRoot importRoot, final MigrationTracer importationJournal) throws ImportationException
    {
        if (importRoot == null || importRoot.getPersons() == null)
        {
            return;
        }

        final FavouritesService favouritesService;
        try {
            favouritesService = registry.getFavouritesService();
        } catch (Exception e) {
            logger.error("FavouritesService not available - skipping favourites import: " + e.getMessage());
            return;
        }
        if (favouritesService == null)
        {
            logger.error("FavouritesService not available - skipping favourites import");
            return;
        }

        final Persons persons = importRoot.getPersons();
        final List<Person> personList = persons.getPersons();

        // Check if any person has favourites to import
        boolean hasFavourites = false;
        for (final Person person : personList)
        {
            if (person.getFavourites() != null && !person.getFavourites().isEmpty())
            {
                hasFavourites = true;
                break;
            }
        }

        if (!hasFavourites)
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("No favourites to import - skipping");
            }
            return;
        }

        // Build a map of old exportation path (nodeRef string) -> new NodeRef
        // by walking the entire import tree
        final Map<String, NodeRef> exportPathToNewRef = buildExportPathMapping(importRoot);

        if (logger.isDebugEnabled())
        {
            logger.debug("Built export path mapping with " + exportPathToNewRef.size() + " entries");
        }

        int totalFavourites = 0;
        int successCount = 0;
        int failCount = 0;

        for (final Person person : personList)
        {
            final List<String> favourites = person.getFavourites();
            if (favourites == null || favourites.isEmpty())
            {
                continue;
            }

            final String userId = (String) person.getUserId().getValue();

            for (final String oldNodeRefStr : favourites)
            {
                totalFavourites++;
                try
                {
                    final NodeRef newNodeRef = exportPathToNewRef.get(oldNodeRefStr);

                    if (newNodeRef != null)
                    {
                        favouritesService.addFavourite(userId, newNodeRef);
                        successCount++;

                        if (logger.isDebugEnabled())
                        {
                            logger.debug("Added favourite for user " + userId + ": " + oldNodeRefStr + " -> " + newNodeRef);
                        }
                    }
                    else
                    {
                        failCount++;
                        if (logger.isWarnEnabled())
                        {
                            logger.warn("Could not find new NodeRef for favourite " + oldNodeRefStr + " of user " + userId);
                        }
                    }
                }
                catch (Exception e)
                {
                    failCount++;
                    if (logger.isWarnEnabled())
                    {
                        logger.warn("Failed to add favourite " + oldNodeRefStr + " for user " + userId + ": " + e.getMessage());
                    }
                }
            }
        }

        if (logger.isInfoEnabled())
        {
            logger.info("**********************************************************************************");
            logger.info("Favourites import complete: " + successCount + " succeeded, " + failCount + " failed out of " + totalFavourites + " total");
            logger.info("**********************************************************************************");
        }
    }

    /**
     * Walks the import tree and builds a mapping from exportation path (old nodeRef string)
     * to the new NodeRef that was assigned during import.
     */
    private Map<String, NodeRef> buildExportPathMapping(final ImportRoot importRoot)
    {
        final Map<String, NodeRef> mapping = new HashMap<String, NodeRef>();

        if (importRoot.getCircabc() == null)
        {
            return mapping;
        }

        final Circabc circabc = importRoot.getCircabc();

        for (final CategoryHeader header : circabc.getCategoryHeaders())
        {
            for (final Category category : header.getCategories())
            {
                for (final InterestGroup ig : category.getInterestGroups())
                {
                    // Walk library
                    if (ig.getLibrary() != null)
                    {
                        collectNodeMapping(ig.getLibrary(), mapping);
                        collectLibraryChildren(ig.getLibrary().getSpaces(), ig.getLibrary().getDossiers(),
                                ig.getLibrary().getMlContents(), ig.getLibrary().getContents(),
                                ig.getLibrary().getUrls(), mapping);
                    }

                    // Walk information
                    if (ig.getInformation() != null)
                    {
                        collectNodeMapping(ig.getInformation(), mapping);
                        collectInformationChildren(ig.getInformation(), mapping);
                    }

                    // Walk newsgroups
                    if (ig.getNewsgroups() != null)
                    {
                        collectNodeMapping(ig.getNewsgroups(), mapping);
                        collectNewsgroupChildren(ig.getNewsgroups(), mapping);
                    }

                    // Walk events
                    if (ig.getEvents() != null)
                    {
                        collectNodeMapping(ig.getEvents(), mapping);
                    }
                }
            }
        }

        return mapping;
    }

    private void collectLibraryChildren(final List<Space> spaces, final List<Dossier> dossiers,
            final List<MlContent> mlContents, final List<Content> contents,
            final List<Url> urls, final Map<String, NodeRef> mapping)
    {
        if (spaces != null)
        {
            for (final Space space : spaces)
            {
                collectNodeMapping(space, mapping);
                // Recurse into sub-spaces
                collectLibraryChildren(space.getSpaces(), space.getDossiers(),
                        space.getMlContents(), space.getContents(), space.getUrls(), mapping);
            }
        }

        if (dossiers != null)
        {
            for (final Dossier dossier : dossiers)
            {
                collectNodeMapping(dossier, mapping);
            }
        }

        if (mlContents != null)
        {
            for (final MlContent mlContent : mlContents)
            {
                collectNodeMapping(mlContent, mapping);
            }
        }

        if (contents != null)
        {
            for (final Content content : contents)
            {
                collectNodeMapping(content, mapping);
            }
        }

        if (urls != null)
        {
            for (final Url url : urls)
            {
                collectNodeMapping(url, mapping);
            }
        }
    }

    private void collectInformationChildren(final Object infoNode, final Map<String, NodeRef> mapping)
    {
        if (infoNode instanceof eu.cec.digit.circabc.migration.entities.generated.nodes.Information)
        {
            final eu.cec.digit.circabc.migration.entities.generated.nodes.Information info =
                    (eu.cec.digit.circabc.migration.entities.generated.nodes.Information) infoNode;

            if (info.getInfSpaces() != null)
            {
                for (final InfSpace infSpace : info.getInfSpaces())
                {
                    collectNodeMapping(infSpace, mapping);
                }
            }
            if (info.getInfMLContents() != null)
            {
                for (final InfMLContent infMl : info.getInfMLContents())
                {
                    collectNodeMapping(infMl, mapping);
                }
            }
            if (info.getInfContents() != null)
            {
                for (final InfContent infContent : info.getInfContents())
                {
                    collectNodeMapping(infContent, mapping);
                }
            }
        }
    }

    private void collectNewsgroupChildren(final Object ngNode, final Map<String, NodeRef> mapping)
    {
        if (ngNode instanceof eu.cec.digit.circabc.migration.entities.generated.nodes.Newsgroups)
        {
            final eu.cec.digit.circabc.migration.entities.generated.nodes.Newsgroups newsgroups =
                    (eu.cec.digit.circabc.migration.entities.generated.nodes.Newsgroups) ngNode;

            if (newsgroups.getFora() != null)
            {
                for (final Forum forum : newsgroups.getFora())
                {
                    collectNodeMapping(forum, mapping);
                    collectForumChildren(forum, mapping);
                }
            }
            if (newsgroups.getTopics() != null)
            {
                for (final Topic topic : newsgroups.getTopics())
                {
                    collectNodeMapping(topic, mapping);
                }
            }
        }
    }

    private void collectForumChildren(final Forum forum, final Map<String, NodeRef> mapping)
    {
        if (forum.getFora() != null)
        {
            for (final Forum subForum : forum.getFora())
            {
                collectNodeMapping(subForum, mapping);
                collectForumChildren(subForum, mapping);
            }
        }
        if (forum.getTopics() != null)
        {
            for (final Topic topic : forum.getTopics())
            {
                collectNodeMapping(topic, mapping);
            }
        }
    }

    /**
     * If the node has both an exportation path and a new nodeRef, add the mapping.
     */
    private void collectNodeMapping(final Object nodeObj, final Map<String, NodeRef> mapping)
    {
        if (nodeObj instanceof Node)
        {
            final Node node = (Node) nodeObj;
            final NodeRef newRef = node.getNodeReference();
            final String exportPath = ElementsHelper.getExportationPath(node);
            if (exportPath != null && newRef != null)
            {
                mapping.put(exportPath, newRef);
            }
        }
    }
}
