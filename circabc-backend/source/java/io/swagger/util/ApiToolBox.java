package io.swagger.util;

import de.schlichtherle.io.FileOutputStream;
import eu.cec.digit.circabc.model.CircabcModel;
import eu.cec.digit.circabc.service.event.OccurenceRate;
import eu.cec.digit.circabc.util.PathUtils;
import io.swagger.exception.SwaggerRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.util.ISO9075;
import org.apache.commons.io.IOUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.util.*;

/**
 * @author beaurpi
 */
public class ApiToolBox {

    public static final String MSG_PREFIX_TIMES_OCCURENCE =
            "event_create_meetings_wizard_step1_occurs_";
    private static final String NOT_APPLICABLE = "event_view_meetings_details_dialog_notapplicable";
    private static final String OCCUR_ONCE = "event_view_meetings_details_dialog_occurence_once";
    private static final String OCCUR_TIME = "event_view_meetings_details_dialog_occurence_times";
    private static final String OCCUR_EVERY_TIME =
            "event_view_meetings_details_dialog_occurence_every";
    /**
     * Restructure this without the faces context and see how to make it multilingual...
     *
     * <p>event_view_meetings_details_dialog_notapplicable=Not Applicable
     * event_view_meetings_details_dialog_occurence_every=Occurs every {0} {1} for {2} times.
     * event_view_meetings_details_dialog_occurence_once=Only Once
     * event_view_meetings_details_dialog_occurence_times={0}. Occurs {1} times
     *
     * <p>event_create_meetings_wizard_step1_occurs_daily=Daily
     * event_create_meetings_wizard_step1_occurs_days=Days
     * event_create_meetings_wizard_step1_occurs_every=Every
     * event_create_meetings_wizard_step1_occurs_everytwoweeks=Every Two Weeks
     * event_create_meetings_wizard_step1_occurs_for=for
     * event_create_meetings_wizard_step1_occurs_mondaytofriday=Monday to Friday
     * event_create_meetings_wizard_step1_occurs_mondaywednseyfriday=Mon/Wed/Fri
     * event_create_meetings_wizard_step1_occurs_monthlybydate=Monthly by date
     * event_create_meetings_wizard_step1_occurs_monthlybyweekday=Monthly by weekday
     * event_create_meetings_wizard_step1_occurs_months=Months
     * event_create_meetings_wizard_step1_occurs_once=Only Once
     * event_create_meetings_wizard_step1_occurs_times=times
     * event_create_meetings_wizard_step1_occurs_tuesdaythursday=Tue/Thu
     * event_create_meetings_wizard_step1_occurs_weekly=Weekly
     * event_create_meetings_wizard_step1_occurs_weeks=Weeks
     * event_create_meetings_wizard_step1_occurs_yearly=Yearly
     * event_create_meetings_wizard_step1_occurs=Occurs
     */
    private static final Map<String, String> occurrenceMap;

    static {
        Map<String, String> occMap = new HashMap<>();
        occMap.put(NOT_APPLICABLE, "Not Applicable");
        occMap.put(OCCUR_EVERY_TIME, "Occurs every {0} {1} for {2} times.");
        occMap.put(OCCUR_ONCE, "Only Once");
        occMap.put(OCCUR_TIME, "{0}. Occurs {1} times.");

        occMap.put("event_create_meetings_wizard_step1_occurs_daily", "Daily");
        occMap.put("event_create_meetings_wizard_step1_occurs_days", "Days");
        occMap.put("event_create_meetings_wizard_step1_occurs_every", "Every");
        occMap.put("event_create_meetings_wizard_step1_occurs_everytwoweeks", "Every Two Weeks");
        occMap.put("event_create_meetings_wizard_step1_occurs_for", "for");
        occMap.put("event_create_meetings_wizard_step1_occurs_mondaytofriday", "Monday to Friday");
        occMap.put("event_create_meetings_wizard_step1_occurs_mondaywednseyfriday", "Mon/Wed/Fri");
        occMap.put("event_create_meetings_wizard_step1_occurs_monthlybydate", "Monthly by date");
        occMap.put("event_create_meetings_wizard_step1_occurs_monthlybyweekday", "Monthly by weekday");
        occMap.put("event_create_meetings_wizard_step1_occurs_months", "Months");
        occMap.put("event_create_meetings_wizard_step1_occurs_once", "Only Once");
        occMap.put("event_create_meetings_wizard_step1_occurs_times", "times");
        occMap.put("event_create_meetings_wizard_step1_occurs_tuesdaythursday", "Tue/Thu");
        occMap.put("event_create_meetings_wizard_step1_occurs_weekly", "Weekly");
        occMap.put("event_create_meetings_wizard_step1_occurs_weeks", "Weeks");
        occMap.put("event_create_meetings_wizard_step1_occurs_yearly", "Yearly");
        occMap.put("event_create_meetings_wizard_step1_occurs", "Occurs");
        occurrenceMap = Collections.unmodifiableMap(occMap);
    }

    private NodeService nodeService;
    private NamespaceService namespaceService;
    private AuthorityService authorityService;

    /**
     * Reads a stream into a file
     *
     * @param inputStream
     * @param file
     * @param limit
     * @return
     * @throws IOException
     */
    public static long inputStreamToFile(InputStream inputStream, File file, long limit)
            throws IOException {

        OutputStream outStream = new FileOutputStream(file);

        byte[] buffer = new byte[8 * 1024];
        int bytesRead;
        long totalBytesRead = 0;
        try {
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                totalBytesRead += bytesRead;
                if (totalBytesRead > limit) {
                    return totalBytesRead;
                }
                outStream.write(buffer, 0, bytesRead);
            }
        } finally {
            IOUtils.closeQuietly(outStream);
        }
        return totalBytesRead * -1;
    }

    /**
     * Checks if the provided image stream is valid and returns a temp file with its contents.
     *
     * @param fileName
     * @param inputStream
     * @param size
     * @return
     */
    public static File checkAndGetImageFile(String fileName, InputStream inputStream, long size) {

        File tempFile;

        try {

            tempFile = File.createTempFile("attachment", ".tmp");
            long bytesRead = ApiToolBox.inputStreamToFile(inputStream, tempFile, size);

            if (bytesRead * -1 > size) {
                throw new IllegalArgumentException("File exceeds 1Mb size: " + fileName);
            }

            BufferedImage image = ImageIO.read(tempFile);
            if (image == null) {
                throw new IllegalArgumentException("Not an image, or image corrupted: " + fileName);
            }

            return tempFile;
        } catch (Exception e) {
            throw new SwaggerRuntimeException("Exception reading image.", e);
        } finally {
            if (inputStream != null) {
                IOUtils.closeQuietly(inputStream);
            }
        }
    }

    /**
     * Generate a search XPATH pointing to the specified node, optionally return an XPATH that
     * includes the child nodes.
     *
     * @param ref      Of the node to generate path too
     * @param children Whether to include children of the node
     * @return the path
     */
    public String getPathFromSpaceRef(NodeRef ref, boolean children) {
        Path path = nodeService.getPath(ref);

        StringBuilder buf = new StringBuilder(64);
        for (int i = 0; i < path.size(); i++) {
            String elementString = "";
            Path.Element element = path.get(i);
            if (element instanceof Path.ChildAssocElement) {
                ChildAssociationRef elementRef = ((Path.ChildAssocElement) element).getRef();
                if (elementRef.getParentRef() != null) {
                    Collection<String> prefixes =
                            namespaceService.getPrefixes(elementRef.getQName().getNamespaceURI());
                    if (!prefixes.isEmpty()) {
                        elementString =
                                '/'
                                        + prefixes.iterator().next()
                                        + ':'
                                        + ISO9075.encode(elementRef.getQName().getLocalName());
                    }
                }
            }

            buf.append(elementString);
        }
        if (children) {
            // append syntax to get all children of the path
            buf.append("//*");
        } else {
            // append syntax to just represent the path, not the children
            buf.append("/*");
        }

        return buf.toString();
    }

    public NodeRef getNodeRef(String id) {
        NodeRef result = Converter.createNodeRefFromId(id);
        if (nodeService.exists(result)) {
            return result;
        }
        result = Converter.createArchiveNodeRefFromId(id);
        if (nodeService.exists(result)) {
            return result;
        } else {
            return null;
        }
    }

    public long getDatabaseID(final NodeRef nodeRef) {
        return (long) nodeService.getProperty(nodeRef, ContentModel.PROP_NODE_DBID);
    }

    public String getName(final NodeRef nodeRef) {
        return (String) nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);
    }

    public NodeRef getCurrentInterestGroup(final NodeRef currentNodeRef) {
        NodeRef tempNodeRef = currentNodeRef;
        // go up to the root of the IG
        for (; ; ) {
            if (tempNodeRef == null) {
                break;
            }

            if (!nodeService.hasAspect(tempNodeRef, CircabcModel.ASPECT_IGROOT)) {
                tempNodeRef = nodeService.getPrimaryParent(tempNodeRef).getParentRef();
            } else {
                break;
            }
        }

        return tempNodeRef;
    }

    /**
     * Get all users that belong to a given group. If there are no users in the group, an empty list
     * is returned.
     *
     * @param groupName Name of the group
     */
    public List<String> getUsersFromGroup(String groupName) {

        List<String> users = new ArrayList<>();

        NodeRef groupNodeRef = authorityService.getAuthorityNodeRef(groupName);

        if (groupNodeRef == null) {
            // comes here for GROUP_EVERYONE
            return users;
        }

        List<ChildAssociationRef> children = nodeService.getChildAssocs(groupNodeRef);

        for (ChildAssociationRef child : children) {

            if (ContentModel.TYPE_PERSON.equals(nodeService.getType(child.getChildRef()))) {

                String userName =
                        (String) nodeService.getProperty(child.getChildRef(), ContentModel.PROP_USERNAME);

                users.add(userName);
            }
        }

        return users;
    }

    public String getCircabcPath(NodeRef nodeRef, boolean includeFirstSlash) {
        Path path = getNodeService().getPath(nodeRef);
        return PathUtils.getCircabcPath(path, includeFirstSlash);
    }

    public String getCategoryPath(NodeRef nodeRef, boolean includeFirstSlash) {
        Path path = getNodeService().getPath(nodeRef);
        return PathUtils.getCategoryPath(path, includeFirstSlash);
    }

    public String getInterestGroupPath(NodeRef nodeRef, boolean includeFirstSlash) {
        Path path = getNodeService().getPath(nodeRef);
        return PathUtils.getInterestGroupPath(path, includeFirstSlash);
    }

    public String getLibraryPath(NodeRef nodeRef, boolean includeFirstSlash) {
        Path path = getNodeService().getPath(nodeRef);
        return PathUtils.getLibraryPath(path, includeFirstSlash);
    }

    public NodeRef getInterestGroupForArchivedNode(NodeRef nodeRef) {
        String id =
                (String) nodeService.getProperty(nodeRef, CircabcModel.PROP_IG_ROOT_NODE_ID_ARCHIVED);
        if (id != null) {
            return getNodeRef(id);
        }else {
            ChildAssociationRef childRef =
                (ChildAssociationRef)
                        nodeService.getProperty(nodeRef, ContentModel.PROP_ARCHIVED_ORIGINAL_PARENT_ASSOC);
            if (getNodeService().exists(childRef.getParentRef())) {
                return getCurrentInterestGroup(childRef.getParentRef());
            } else {
                return null;
            }
        }
    }

    public String getCircabcPathForArchivedNode(NodeRef nodeRef, boolean includeFirstSlash) {
        ChildAssociationRef childRef =
                (ChildAssociationRef)
                        nodeService.getProperty(nodeRef, ContentModel.PROP_ARCHIVED_ORIGINAL_PARENT_ASSOC);
        if (getNodeService().exists(childRef.getParentRef())) {
            Path path = nodeService.getPath(childRef.getParentRef());
            return PathUtils.getCircabcPath(path, includeFirstSlash);
        } else {
            return null;
        }
    }

    public NodeService getNodeService() {
        return nodeService;
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public NamespaceService getNamespaceService() {
        return namespaceService;
    }

    public void setNamespaceService(NamespaceService namespaceService) {
        this.namespaceService = namespaceService;
    }

    /**
     * @param authorityService the authorityService to set
     */
    public void setAuthorityService(AuthorityService authorityService) {
        this.authorityService = authorityService;
    }

    public NodeRef getCurrentLibraryRoot(NodeRef nodeRef) {
        if (nodeRef == null
                || !nodeRef
                .getStoreRef()
                .getProtocol()
                .equals(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE.getProtocol())) {
            return null;
        } else if (nodeService.hasAspect(nodeRef, CircabcModel.ASPECT_LIBRARY_ROOT)) {
            return nodeRef;
        } else {
            return getCurrentLibraryRoot(nodeService.getPrimaryParent(nodeRef).getParentRef());
        }
    }

    public NodeRef getCurrentNewsgroupRoot(NodeRef nodeRef) {
        if (nodeRef == null
                || !nodeRef
                .getStoreRef()
                .getProtocol()
                .equals(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE.getProtocol())) {
            return null;
        } else if (nodeService.hasAspect(nodeRef, CircabcModel.ASPECT_NEWSGROUP_ROOT)) {
            return nodeRef;
        } else {
            return getCurrentNewsgroupRoot(nodeService.getPrimaryParent(nodeRef).getParentRef());
        }
    }

    public String getOccurenceAsString(String occurenceRateString) {

        if (occurenceRateString == null || occurenceRateString.isEmpty()) {
            return occurrenceMap.get(NOT_APPLICABLE);
        }

        String occurStr = null;
        final OccurenceRate occurenceRate = new OccurenceRate(occurenceRateString);

        switch (occurenceRate.getMainOccurence()) {
            case OnlyOnce:
                occurStr = translate(OCCUR_ONCE);
                break;

            case Times:
                occurStr =
                        translate(
                                OCCUR_TIME, getDisplayTimesOccurence(occurenceRate), occurenceRate.getTimes());
                break;

            case EveryTimes:
                occurStr =
                        translate(
                                OCCUR_EVERY_TIME,
                                occurenceRate.getEvery(),
                                getDisplayEveryTimesOccurence(occurenceRate),
                                occurenceRate.getTimes());
                break;
        }

        return occurStr;
    }

    protected String translate(final String key, final Object... params) {
        MessageFormat form = new MessageFormat(occurrenceMap.get(key));
        return form.format(params);
    }

    public String getDisplayTimesOccurence(OccurenceRate occurenceRate) {
        return occurrenceMap.get(
                MSG_PREFIX_TIMES_OCCURENCE + occurenceRate.getTimesOccurence().name().toLowerCase());
    }

    public String getDisplayEveryTimesOccurence(OccurenceRate occurenceRate) {
        return occurrenceMap.get(
                MSG_PREFIX_TIMES_OCCURENCE + occurenceRate.getEveryTimesOccurence().name().toLowerCase());
    }
}
