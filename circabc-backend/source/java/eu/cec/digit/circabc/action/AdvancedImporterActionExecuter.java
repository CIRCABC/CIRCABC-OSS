/*******************************************************************************
 * Copyright 2006 European Community
 *
 *  Licensed under the EUPL, Version 1.1 or - as soon they
 *  will be approved by the European Commission - subsequent
 *  versions of the EUPL (the "Licence");
 *  You may not use this work except in compliance with the
 *  Licence.
 *  You may obtain a copy of the Licence at:
 *
 *  https://joinup.ec.europa.eu/software/page/eupl
 *
 *  Unless required by applicable law or agreed to in
 *  writing, software distributed under the Licence is
 *  distributed on an "AS IS" basis,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *  express or implied.
 *  See the Licence for the specific language governing
 *  permissions and limitations under the Licence.
 ******************************************************************************/
/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package eu.cec.digit.circabc.action;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ApplicationModel;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.ParameterDefinitionImpl;
import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.importer.ACPImportPackageHandler;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.model.FileExistsException;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.cmr.view.ImporterBinding;
import org.alfresco.service.cmr.view.ImporterContentCache;
import org.alfresco.service.cmr.view.ImporterService;
import org.alfresco.service.cmr.view.Location;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.TempFileProvider;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.owasp.esapi.ESAPI;

import java.io.*;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Importer action executor
 *
 * @author gavinc
 */
public class AdvancedImporterActionExecuter extends ActionExecuterAbstractBase {

    public static final String NAME = "advanced-import";
    public static final String PARAM_ENCODING = "encoding";
    public static final String PARAM_DESTINATION_FOLDER = "destination";
    public static final String PARAM_BINDING = "binding";
    private static final Log logger = LogFactory.getLog(AdvancedImporterActionExecuter.class);
    private static final int BUFFER_SIZE = 16384;
    private static final String TEMP_FILE_PREFIX = "alf";
    private static final String TEMP_FILE_SUFFIX_ACP = ".acp";
    private static final String TEMP_FILE_SUFFIX_ZIP = ".zip";
    private static ImporterBinding CREATE_NEW = new ImporterBinding() {

        public UUID_BINDING getUUIDBinding() {
            return UUID_BINDING.CREATE_NEW;
        }

        public String getValue(String key) {
            return null;
        }

        public boolean allowReferenceWithinTransaction() {
            return false;
        }

        public QName[] getExcludedClasses() {
            return null;
        }

        /* (non-Javadoc)
         * @see org.alfresco.service.cmr.view.ImporterBinding#getImportConentCache()
         */
        @Override
        public ImporterContentCache getImportConentCache() {
            // TODO Auto-generated method stub
            return null;
        }

    };
    private static ImporterBinding CREATE_NEW_WITH_UUID = new ImporterBinding() {

        public UUID_BINDING getUUIDBinding() {
            return UUID_BINDING.CREATE_NEW_WITH_UUID;
        }

        public String getValue(String key) {
            return null;
        }

        public boolean allowReferenceWithinTransaction() {
            return false;
        }

        public QName[] getExcludedClasses() {
            return null;
        }

        /* (non-Javadoc)
         * @see org.alfresco.service.cmr.view.ImporterBinding#getImportConentCache()
         */
        @Override
        public ImporterContentCache getImportConentCache() {
            // TODO Auto-generated method stub
            return null;
        }

    };
    private static ImporterBinding REMOVE_EXISTING = new ImporterBinding() {

        public UUID_BINDING getUUIDBinding() {
            return UUID_BINDING.REMOVE_EXISTING;
        }

        public String getValue(String key) {
            return null;
        }

        public boolean allowReferenceWithinTransaction() {
            return false;
        }

        public QName[] getExcludedClasses() {
            return null;
        }

        /* (non-Javadoc)
         * @see org.alfresco.service.cmr.view.ImporterBinding#getImportConentCache()
         */
        @Override
        public ImporterContentCache getImportConentCache() {
            // TODO Auto-generated method stub
            return null;
        }

    };
    private static ImporterBinding REPLACE_EXISTING = new ImporterBinding() {

        public UUID_BINDING getUUIDBinding() {
            return UUID_BINDING.REPLACE_EXISTING;
        }

        public String getValue(String key) {
            return null;
        }

        public boolean allowReferenceWithinTransaction() {
            return false;
        }

        public QName[] getExcludedClasses() {
            return null;
        }

        /* (non-Javadoc)
         * @see org.alfresco.service.cmr.view.ImporterBinding#getImportConentCache()
         */
        @Override
        public ImporterContentCache getImportConentCache() {
            // TODO Auto-generated method stub
            return null;
        }

    };
    private static ImporterBinding UPDATE_EXISTING = new ImporterBinding() {

        public UUID_BINDING getUUIDBinding() {
            return UUID_BINDING.UPDATE_EXISTING;
        }

        public String getValue(String key) {
            return null;
        }

        public boolean allowReferenceWithinTransaction() {
            return false;
        }

        public QName[] getExcludedClasses() {
            return null;
        }

        /* (non-Javadoc)
         * @see org.alfresco.service.cmr.view.ImporterBinding#getImportConentCache()
         */
        @Override
        public ImporterContentCache getImportConentCache() {
            // TODO Auto-generated method stub
            return null;
        }

    };
    private static ImporterBinding THROW_ON_COLLISION = new ImporterBinding() {

        public UUID_BINDING getUUIDBinding() {
            return UUID_BINDING.THROW_ON_COLLISION;
        }

        public String getValue(String key) {
            return null;
        }

        public boolean allowReferenceWithinTransaction() {
            return false;
        }

        public QName[] getExcludedClasses() {
            return null;
        }

        /* (non-Javadoc)
         * @see org.alfresco.service.cmr.view.ImporterBinding#getImportConentCache()
         */
        @Override
        public ImporterContentCache getImportConentCache() {
            // TODO Auto-generated method stub
            return null;
        }

    };
    /**
     * The importer service
     */
    private ImporterService importerService;
    /**
     * The node service
     */
    private NodeService nodeService;
    /**
     * The content service
     */
    private ContentService contentService;
    /**
     * The mimetype service
     */
    private MimetypeService mimetypeService;
    /**
     * The file folder service
     */
    private FileFolderService fileFolderService;

    /**
     * Extract the file and folder structure of a ZIP file into the specified directory
     *
     * @param archive    The ZIP archive to extract
     * @param extractDir The directory to extract into
     */
    public static void extractFile(ZipFile archive, String extractDir) {
        String fileName;
        String destFileName;
        byte[] buffer = new byte[BUFFER_SIZE];
        extractDir = extractDir + File.separator;
        try {
            for (Enumeration e = archive.getEntries(); e.hasMoreElements(); ) {
                ZipArchiveEntry entry = (ZipArchiveEntry) e.nextElement();
                if (!entry.isDirectory()) {
                    fileName = entry.getName();
                    fileName = fileName.replace('/', File.separatorChar);
                    destFileName = extractDir + fileName;
                    String esapiFileName = ESAPI.encoder().canonicalize(destFileName);
                    boolean esapiValidFileName = ESAPI.validator().isValidFileName("import", esapiFileName, false);
                    if (!esapiValidFileName) {
                        throw new IOException("Filename '" + destFileName + "' is invalid. Please check its name.");
                    }
                    File destFile = new File(esapiFileName);
                    String parent = destFile.getParent();
                    if (parent != null) {
                        File parentFile = new File(parent);
                        if (!parentFile.exists()) {
                            parentFile.mkdirs();
                        }
                    }
                    InputStream in = null;
                    OutputStream out = null;
                    try {
                        in = new BufferedInputStream(archive.getInputStream(entry), BUFFER_SIZE);
                        out = new BufferedOutputStream(new FileOutputStream(destFileName),
                                BUFFER_SIZE);
                        int count;
                        while ((count = in.read(buffer)) != -1) {
                            out.write(buffer, 0, count);
                        }
                    } finally {
                        if (in != null) {
                            try {
                                in.close();
                            } finally {
                                if (out != null) {
                                    out.close();
                                }
                            }
                        }
                    }
                } else {
                    File newdir = new File(extractDir + entry.getName());
                    newdir.mkdirs();
                }
            }
        } catch (IOException e) {
            throw new AlfrescoRuntimeException("Failed to process ZIP file.", e);
        }
    }
    // CREATE_NEW, CREATE_NEW_WITH_UUID, REMOVE_EXISTING, REPLACE_EXISTING, UPDATE_EXISTING, THROW_ON_COLLISION

    /**
     * Recursively delete a dir of files and directories
     *
     * @param dir directory to delete
     */
    public static void deleteDir(File dir) {
        if (dir != null) {
            File elenco = new File(dir.getPath());

            // listFiles can return null if the path is invalid i.e. already been deleted,
            // therefore check for null before using in loop
            File[] files = elenco.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        file.delete();
                    } else {
                        deleteDir(file);
                    }
                }
            }

            // delete provided directory
            dir.delete();
        }
    }

    /**
     * Sets the ImporterService to use
     *
     * @param importerService The ImporterService
     */
    public void setImporterService(ImporterService importerService) {
        this.importerService = importerService;
    }

    /**
     * Sets the NodeService to use
     *
     * @param nodeService The NodeService
     */
    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    /**
     * Sets the ContentService to use
     *
     * @param contentService The ContentService
     */
    public void setContentService(ContentService contentService) {
        this.contentService = contentService;
    }

    /**
     * Sets the MimetypeService to use
     *
     * @param mimetypeService The MimetypeService
     */
    public void setMimetypeService(MimetypeService mimetypeService) {
        this.mimetypeService = mimetypeService;
    }

    /**
     * Sets the FileFolderService to use
     *
     * @param fileFolderService The FileFolderService
     */
    public void setFileFolderService(FileFolderService fileFolderService) {
        this.fileFolderService = fileFolderService;
    }

    /**
     * @see org.alfresco.repo.action.executer.ActionExecuter#execute(Action, NodeRef)
     */
    public void executeImpl(Action ruleAction, NodeRef actionedUponNodeRef) {
        if (this.nodeService.exists(actionedUponNodeRef)) {
            // The node being passed in should be an Alfresco content package
            ContentReader reader = this.contentService
                    .getReader(actionedUponNodeRef, ContentModel.PROP_CONTENT);
            if (reader != null) {
                NodeRef importDest = (NodeRef) ruleAction.getParameterValue(PARAM_DESTINATION_FOLDER);
                if (MimetypeMap.MIMETYPE_ACP.equals(reader.getMimetype())) {
                    // perform an import of an Alfresco ACP file (special format ZIP structure)
                    File zipFile = null;
                    try {
                        // unfortunately a ZIP file can not be read directly from an input stream so we have to create
                        // a temporary file first
                        zipFile = TempFileProvider.createTempFile(TEMP_FILE_PREFIX, TEMP_FILE_SUFFIX_ACP);
                        reader.getContent(zipFile);

                        ACPImportPackageHandler importHandler = new ACPImportPackageHandler(zipFile,
                                (String) ruleAction.getParameterValue(PARAM_ENCODING));

                        String bindingString = (String) ruleAction.getParameterValue(PARAM_BINDING);
                        ImporterBinding binding = getImportBinding(bindingString);
                        this.importerService.importView(importHandler, new Location(importDest), binding, null);
                    } finally {
                        // now the import is done, delete the temporary file
                        if (zipFile != null) {
                            zipFile.delete();
                        }
                    }
                } else if (MimetypeMap.MIMETYPE_ZIP.equals(reader.getMimetype())) {
                    // perform an import of a standard ZIP file
                    ZipFile zipFile = null;
                    File tempFile = null;
                    try {
                        tempFile = TempFileProvider.createTempFile(TEMP_FILE_PREFIX, TEMP_FILE_SUFFIX_ACP);
                        reader.getContent(tempFile);
                        // NOTE: This encoding allows us to workaround bug:
                        //       http://bugs.sun.com/bugdatabase/view_bug.do;:WuuT?bug_id=4820807
                        // We also try to use the extra encoding information if present
                        // ALF-2016
                        zipFile = new ZipFile(tempFile, "UTF-8", true);

                        // build a temp dir name based on the ID of the noderef we are importing
                        // also use the long life temp folder as large ZIP files can take a while
                        File alfTempDir = TempFileProvider.getLongLifeTempDir("import");
                        File tempDir = new File(
                                alfTempDir.getPath() + File.separatorChar + actionedUponNodeRef.getId());
                        try {
                            // TODO: improve this code to directly pipe the zip stream output into the repo objects -
                            //       to remove the need to expand to the filesystem first?
                            extractFile(zipFile, tempDir.getPath());
                            importDirectory(tempDir.getPath(), importDest);
                        } finally {
                            deleteDir(tempDir);
                        }
                    } catch (IOException ioErr) {
                        throw new AlfrescoRuntimeException("Failed to import ZIP file.", ioErr);
                    } finally {
                        // now the import is done, delete the temporary file
                        if (tempFile != null) {
                            tempFile.delete();
                        }
                    }
                }
            }
        }
    }

    private ImporterBinding getImportBinding(String bindingString) {
        if (bindingString.equalsIgnoreCase("CREATE_NEW")) {
            return CREATE_NEW;
        } else if (bindingString.equalsIgnoreCase("CREATE_NEW_WITH_UUID")) {
            return CREATE_NEW_WITH_UUID;
        } else if (bindingString.equalsIgnoreCase("REMOVE_EXISTING")) {
            return REMOVE_EXISTING;
        } else if (bindingString.equalsIgnoreCase("REPLACE_EXISTING")) {
            return REPLACE_EXISTING;
        } else if (bindingString.equalsIgnoreCase("UPDATE_EXISTING")) {
            return UPDATE_EXISTING;
        } else if (bindingString.equalsIgnoreCase("THROW_ON_COLLISION")) {
            return THROW_ON_COLLISION;
        }

        return null;
    }

    /**
     * Recursively import a directory structure into the specified root node
     *
     * @param dir  The directory of files and folders to import
     * @param root The root node to import into
     */
    private void importDirectory(String dir, NodeRef root) {
        File topdir = new File(dir);
        for (File file : topdir.listFiles()) {
            try {
                if (file.isFile()) {
                    String fileName = file.getName();

                    // create content node based on the file name
                    FileInfo fileInfo = this.fileFolderService
                            .create(root, fileName, ContentModel.TYPE_CONTENT);
                    NodeRef fileRef = fileInfo.getNodeRef();

                    // add titled aspect for the read/edit properties screens
                    Map<QName, Serializable> titledProps = new HashMap<>(1, 1.0f);
                    titledProps.put(ContentModel.PROP_TITLE, fileName);
                    this.nodeService.addAspect(fileRef, ContentModel.ASPECT_TITLED, titledProps);

                    InputStream contentStream = null;
                    // push the content of the file into the node
                    try {
                        contentStream = new BufferedInputStream(new FileInputStream(file),
                                BUFFER_SIZE);
                        ContentWriter writer = this.contentService
                                .getWriter(fileRef, ContentModel.PROP_CONTENT, true);
                        writer.setMimetype(this.mimetypeService.guessMimetype(fileName));
                        writer.putContent(contentStream);
                    } finally {
                        if (contentStream != null) {
                            try {
                                contentStream.close();
                            } catch (IOException e) {
                                if (logger.isErrorEnabled()) {
                                    logger.fatal("Error closing stream", e);
                                }
                            }
                        }
                    }

                } else {
                    // create a folder based on the folder name
                    FileInfo folderInfo = this.fileFolderService
                            .create(root, file.getName(), ContentModel.TYPE_FOLDER);
                    NodeRef folderRef = folderInfo.getNodeRef();

                    // add the uifacets aspect for the read/edit properties screens
                    this.nodeService.addAspect(folderRef, ApplicationModel.ASPECT_UIFACETS, null);

                    importDirectory(file.getPath(), folderRef);
                }
            } catch (FileNotFoundException | FileExistsException e) {
                // TODO: add failed file info to status message?
                throw new AlfrescoRuntimeException("Failed to process ZIP file.", e);
            }
        }
    }

    /**
     * @see org.alfresco.repo.action.ParameterizedItemAbstractBase#addParameterDefinitions(java.util.List)
     */
    protected void addParameterDefinitions(List<ParameterDefinition> paramList) {
        paramList.add(new ParameterDefinitionImpl(PARAM_DESTINATION_FOLDER, DataTypeDefinition.NODE_REF,
                true, getParamDisplayLabel(PARAM_DESTINATION_FOLDER)));
        paramList.add(new ParameterDefinitionImpl(PARAM_ENCODING, DataTypeDefinition.TEXT,
                false, getParamDisplayLabel(PARAM_ENCODING)));
        paramList.add(new ParameterDefinitionImpl(PARAM_BINDING, DataTypeDefinition.TEXT,
                false, getParamDisplayLabel(PARAM_BINDING)));
    }
}
