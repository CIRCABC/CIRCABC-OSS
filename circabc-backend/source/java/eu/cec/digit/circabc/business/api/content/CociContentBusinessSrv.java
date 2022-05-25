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
package eu.cec.digit.circabc.business.api.content;

import org.alfresco.service.cmr.repository.NodeRef;

import java.io.File;
import java.io.InputStream;

/**
 * Business service to check in, check out, update, lock a document.
 *
 * @author Yanick Pignot
 */
public interface CociContentBusinessSrv {


    /**
     * Check out a file in the same folder
     *
     * @param nodeRef The content to lock
     * @return The working copy reference
     */
    NodeRef checkOut(final NodeRef nodeRef);

    /**
     * Check out a file in the same folder in a workflow action
     *
     * @param nodeRef The content to lock
     * @return The working copy reference
     */
    NodeRef checkOutForWorkflow(final NodeRef nodeRef, final String workflowTaskId);

    /**
     * Undo / cancel a node being checked out.
     *
     * @param workingCopyRef The working copy
     * @return The original document
     */
    NodeRef cancelCheckOut(final NodeRef workingCopyRef);

    /**
     * Check in a document with the content of the working copy node
     *
     * @param workingCopy  The working copy document noderef
     * @param minor        If the new version is minor (true, 1.0 -> 1.1) or major (false, 1.0 -> 2.0)
     * @param versionNote  The version note
     * @param keepCheckOut Keep the document checked out
     * @return The original document (locked if keepCheckedOut)
     */
    NodeRef checkIn(final NodeRef workingCopy, final boolean minor, final String versionNote,
                    boolean keepCheckOut);

    /**
     * Check in a document with the content of a given file
     *
     * @param workingCopy      The working copy document noderef
     * @param minor            If the new version is minor (true, 1.0 -> 1.1) or major (false, 1.0 -> 2.0)
     * @param versionNote      The version note
     * @param keepCheckOut     Keep the document checked out
     * @param file             The file that update the content
     * @param uploadedfilename Filename of original file that update content
     * @return The original document (locked if keepCheckedOut)
     */

    NodeRef checkIn(final NodeRef workingCopy, final boolean minor, final String versionNote,
                    boolean keepCheckOut, final File file, String uploadedfilename);

    /**
     * Check in a document with the content of a given file
     *
     * @param workingCopy         The working copy document noderef
     * @param minor               If the new version is minor (true, 1.0 -> 1.1) or major (false, 1.0 -> 2.0)
     * @param versionNote         The version note
     * @param keepCheckOut        Keep the document checked out
     * @param file                The file that update the content
     * @param disableNotification If the notifications are disabled or not
     * @param uploadedfilename    Filename of original file that update content
     * @return The original document (locked if keepCheckedOut)
     */
    NodeRef checkIn(final NodeRef workingCopy, final boolean minor, final String versionNote,
                    boolean keepCheckOut, final File file, boolean disableNotification, String uploadedfilename);

    /**
     * Lock a given node
     */
    void lock(final NodeRef nodeRef);

    /**
     * Unlock a given node
     */
    void unlock(final NodeRef nodeRef);

    /**
     * Update a document with a given file
     *
     * @param document            The document to update
     * @param file                The file where is located the new content
     * @param disableNotification Disable notification or not
     * @param uploadedfilename    Filename of original file that update content
     */
    void update(final NodeRef document, final File file, final boolean disableNotification,
                String uploadedfilename);

    /**
     * Update a document with a given file
     *
     * @param document         The document to update
     * @param file             The file where is located the new content
     * @param uploadedfilename Filename of original file that update content
     */
    void update(final NodeRef document, final File file, String uploadedfilename);

    /**
     * Update a document with a given input stream
     *
     * @param document    The document to update
     * @param inputStream The input stream with the new content
     */
    void update(final NodeRef document, final InputStream inputStream, String mimeType);

    /**
     * Return the working copy of the given locked document
     */
    NodeRef getWorkingCopy(final NodeRef lockedRef);

    /**
     * Return the original document of a working copy
     */
    NodeRef getWorkingCopyOf(final NodeRef workingCopyRef);

}
