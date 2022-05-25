/**
 * ***************************************************************************** Copyright 2006
 * European Community
 *
 * <p>Licensed under the EUPL, Version 1.1 or - as soon they will be approved by the European
 * Commission - subsequent versions of the EUPL (the "Licence"); You may not use this work except in
 * compliance with the Licence. You may obtain a copy of the Licence at:
 *
 * <p>https://joinup.ec.europa.eu/software/page/eupl
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * Licence is distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * ****************************************************************************
 */
package eu.cec.digit.circabc.repo.filefolder;

import eu.cec.digit.circabc.error.CircabcRuntimeException;
import eu.cec.digit.circabc.model.CircabcModel;
import eu.cec.digit.circabc.service.filefolder.CircabcFileFolderService;
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.model.ForumModel;
import org.alfresco.repo.cache.SimpleCache;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.model.FileFolderServiceType;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.ResultSetRow;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ISO9075;
import org.alfresco.web.bean.repository.Repository;

import java.io.Serializable;
import java.util.*;

public class EagerCircabcFileFolderServiceImpl implements CircabcFileFolderService {

    private SearchService searchService;
    private NamespaceService namespaceService;
    private DictionaryService dictionaryService;
    private NodeService nodeService;
    private SimpleCache<String, Map<NodeRef, Set<NodeRef>>> circabcFileFolderServiceCache;

    public List<FileInfo> list(NodeRef contextNodeRef) {
        if (!nodeService.hasAspect(contextNodeRef, CircabcModel.ASPECT_LIBRARY)) {
            throw new CircabcRuntimeException(
                    contextNodeRef.toString() + " does not have library aspect");
        }

        List<FileInfo> result = new ArrayList<>();
        final NodeRef libraryNodeRef = getLibraryNodeRef(contextNodeRef);
        final String key = AuthenticationUtil.getRunAsUser() + libraryNodeRef.getId();

        // reset library node
        if (nodeService.hasAspect(contextNodeRef, CircabcModel.ASPECT_LIBRARY_ROOT)) {
            circabcFileFolderServiceCache.remove(key);
        }
        Map<NodeRef, Set<NodeRef>> map;
        if (circabcFileFolderServiceCache.contains(key)) {
            map = circabcFileFolderServiceCache.get(key);
            if (!map.containsKey(contextNodeRef)) {
                map = buildTree(libraryNodeRef);
                circabcFileFolderServiceCache.put(key, map);
            }

        } else {
            map = buildTree(libraryNodeRef);
            circabcFileFolderServiceCache.put(key, map);
        }
        final Set<NodeRef> nodeRefs = map.get(contextNodeRef);
        if (nodeRefs != null) {
            result = toFileInfo(nodeRefs);
        }
        return result;
    }

    private NodeRef getLibraryNodeRef(NodeRef contextNodeRef) {
        NodeRef result = null;

        if (nodeService.hasAspect(contextNodeRef, CircabcModel.ASPECT_LIBRARY_ROOT)) {
            result = contextNodeRef;
        } else {
            NodeRef currentNodeRef = contextNodeRef;
            while (!getNodeService().hasAspect(currentNodeRef, CircabcModel.ASPECT_LIBRARY_ROOT)) {
                currentNodeRef = getNodeService().getPrimaryParent(currentNodeRef).getParentRef();
            }
            result = currentNodeRef;
        }
        return result;
    }

    /**
     * Helper method to convert node reference instances to file info
     *
     * @param nodeRefs the node references
     * @return Return a list of file info
     * @throws InvalidTypeException if the node is not a valid type
     */
    private List<FileInfo> toFileInfo(Set<NodeRef> nodeRefs) throws InvalidTypeException {
        if (nodeRefs == null) {
            return new ArrayList<>();
        }

        List<FileInfo> results = new ArrayList<>(nodeRefs.size());
        for (NodeRef nodeRef : nodeRefs) {
            if (nodeService.exists(nodeRef)) {
                FileInfo fileInfo;
                try {
                    fileInfo = toFileInfo(nodeRef);
                } catch (InvalidTypeException ex) {
                    continue;
                }

                results.add(fileInfo);
            }
        }
        return results;
    }

    /**
     * Helper method to convert a node reference instance to a file info
     */
    private FileInfo toFileInfo(NodeRef nodeRef) throws InvalidTypeException {
        // Get the file attributes
        Map<QName, Serializable> properties = nodeService.getProperties(nodeRef);
        // Is it a folder
        QName typeQName = nodeService.getType(nodeRef);
        boolean isFolder = isFolder(typeQName);

        // Construct the file info and add to the results
        FileInfo fileInfo = new FileInfoImpl(nodeRef, isFolder, properties);
        // Done
        return fileInfo;
    }

    /**
     * Checks the type for whether it is a file or folder. All invalid types lead to runtime
     * exceptions.
     *
     * @param typeQName the type to check
     * @return Returns true if the type is a valid folder type, false if it is a file.
     * @throws AlfrescoRuntimeException if the type is not handled by this service
     */
    private boolean isFolder(QName typeQName) throws InvalidTypeException {
        FileFolderServiceType type = getType(typeQName);

        switch (type) {
            case FILE:
                return false;
            case FOLDER:
                return true;
            case SYSTEM_FOLDER:
                throw new InvalidTypeException(
                        "This service should ignore type " + ContentModel.TYPE_SYSTEM_FOLDER);
            case INVALID:
            default:
                throw new InvalidTypeException("Type is not handled by this service: " + typeQName);
        }
    }

    public boolean exists(NodeRef nodeRef) {
        return nodeService.exists(nodeRef);
    }

    public FileFolderServiceType getType(QName typeQName) {
        if (dictionaryService.isSubClass(typeQName, ContentModel.TYPE_FOLDER)) {
            if (dictionaryService.isSubClass(typeQName, ContentModel.TYPE_SYSTEM_FOLDER)) {
                return FileFolderServiceType.SYSTEM_FOLDER;
            }
            return FileFolderServiceType.FOLDER;
        } else if (dictionaryService.isSubClass(typeQName, ContentModel.TYPE_CONTENT)
                || dictionaryService.isSubClass(typeQName, ContentModel.TYPE_LINK)) {
            // it is a regular file
            return FileFolderServiceType.FILE;
        } else {
            // unhandled type
            return FileFolderServiceType.INVALID;
        }
    }

    private Map<NodeRef, Set<NodeRef>> buildTree(NodeRef nodeRef) {
        Map<NodeRef, Set<NodeRef>> tree;
        tree = new HashMap<>();
        final SearchParameters sp = new SearchParameters();
        sp.setLanguage(SearchService.LANGUAGE_LUCENE);
        sp.setQuery(buildvisibleDocumentQuery(nodeRef));
        sp.addStore(Repository.getStoreRef());
        ResultSet result = null;
        try {
            result = getSearchService().query(sp);
            for (ResultSetRow resultSetRow : result) {
                NodeRef currentNodeRef = resultSetRow.getNodeRef();
                if (!tree.containsKey(currentNodeRef)) {
                    final HashSet<NodeRef> value = new HashSet<>();
                    tree.put(currentNodeRef, value);
                }
                NodeRef parentNodeRef = nodeService.getPrimaryParent(currentNodeRef).getParentRef();
                if (tree.containsKey(parentNodeRef)) {
                    final Set<NodeRef> value = tree.get(parentNodeRef);
                    value.add(currentNodeRef);
                } else {
                    final HashSet<NodeRef> value = new HashSet<>();
                    value.add(currentNodeRef);
                    tree.put(parentNodeRef, value);
                }

                while (parentNodeRef != null
                        && !getNodeService().hasAspect(parentNodeRef, CircabcModel.ASPECT_IGROOT)) {
                    currentNodeRef = parentNodeRef;
                    parentNodeRef = getNodeService().getPrimaryParent(parentNodeRef).getParentRef();
                    if (tree.containsKey(parentNodeRef)) {
                        final Set<NodeRef> value = tree.get(parentNodeRef);
                        value.add(currentNodeRef);
                    } else {
                        final HashSet<NodeRef> value = new HashSet<>();
                        value.add(currentNodeRef);
                        tree.put(parentNodeRef, value);
                    }
                }
            }
        } finally {
            if (result != null) {
                result.close();
            }
        }

        return tree;
    }

    private String buildvisibleDocumentQuery(NodeRef nodeRef) {
        String query = "(PATH:\"" + getPathFromSpaceRef(nodeRef, true) + "\"" + ")";
        query =
                query
                        + " AND (( TYPE:\""
                        + ForumModel.TYPE_POST
                        + "\" OR TYPE:\""
                        + ForumModel.TYPE_FORUMS
                        + "\") OR (TYPE:\""
                        + ContentModel.TYPE_CONTENT
                        + "\" OR TYPE:\""
                        + ContentModel.TYPE_FOLDER
                        + "\"))";
        return query;
    }

    private String getPathFromSpaceRef(final NodeRef ref, boolean children) {
        final Path path = getNodeService().getPath(ref);
        final StringBuilder buf = new StringBuilder(64);
        String elementString;
        Path.Element element;
        ChildAssociationRef elementRef;
        Collection<?> prefixes;
        for (int i = 0; i < path.size(); i++) {
            elementString = "";
            element = path.get(i);
            if (element instanceof Path.ChildAssocElement) {
                elementRef = ((Path.ChildAssocElement) element).getRef();
                if (elementRef.getParentRef() != null) {
                    prefixes = getNamespaceService().getPrefixes(elementRef.getQName().getNamespaceURI());
                    if (prefixes.size() > 0) {
                        elementString =
                                '/'
                                        + (String) prefixes.iterator().next()
                                        + ':'
                                        + ISO9075.encode(elementRef.getQName().getLocalName());
                    }
                }
            }

            buf.append(elementString);
        }
        if (children == true) {
            // append syntax to get all children of the path
            buf.append("//*");
        } else {
            // append syntax to just represent the path, not the children
            buf.append("/*");
        }

        return buf.toString();
    }

    public SearchService getSearchService() {
        return searchService;
    }

    public void setSearchService(SearchService searchService) {
        this.searchService = searchService;
    }

    public NamespaceService getNamespaceService() {
        return namespaceService;
    }

    public void setNamespaceService(NamespaceService namespaceService) {
        this.namespaceService = namespaceService;
    }

    public NodeService getNodeService() {
        return nodeService;
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public SimpleCache<String, Map<NodeRef, Set<NodeRef>>> getTreeCache() {
        return circabcFileFolderServiceCache;
    }

    public void setTreeCache(SimpleCache<String, Map<NodeRef, Set<NodeRef>>> treeCache) {
        this.circabcFileFolderServiceCache = treeCache;
    }

    public SimpleCache<String, Map<NodeRef, Set<NodeRef>>> getCircabcFileFolderServiceCache() {
        return circabcFileFolderServiceCache;
    }

    public void setCircabcFileFolderServiceCache(
            SimpleCache<String, Map<NodeRef, Set<NodeRef>>> circabcFileFolderServiceCache) {
        this.circabcFileFolderServiceCache = circabcFileFolderServiceCache;
    }

    public DictionaryService getDictionaryService() {
        return dictionaryService;
    }

    public void setDictionaryService(DictionaryService dictionaryService) {
        this.dictionaryService = dictionaryService;
    }

    /**
     * Exception when the type is not a valid File or Folder type
     *
     * @author Derek Hulley
     * @see ContentModel#TYPE_CONTENT
     * @see ContentModel#TYPE_FOLDER
     */
    private static class InvalidTypeException extends RuntimeException {

        /**
         *
         */
        private static final long serialVersionUID = -1662902469981847136L;

        public InvalidTypeException(String msg) {
            super(msg);
        }
    }
}
