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
package eu.cec.digit.circabc.service.newsgroup;

import org.alfresco.service.Auditable;
import org.alfresco.service.cmr.repository.DuplicateChildNodeNameException;
import org.alfresco.service.cmr.repository.NodeRef;

import java.io.File;
import java.io.InputStream;
import java.util.List;

/**
 * Interface to manage post attachement.
 *
 * <pre>
 * 		Ideally containers are TYPE_FORUM or TYPE_TOPIC and contents are TYPE_POST. But it is not required.
 * </pre>
 *
 * @author Yanick Pignot
 * <p>Migration 3.1 -> 3.4.6 - 02/12/2011 Commented the key parameter of the @Auditable
 * annotation. Commented the deprecated @PublicService annotation.
 */
// @PublicService
public interface AttachementService {

    /**
     * Create the refered node (refered) in an hidden folder, and attach it to an existiong nodeRef
     * (referer)
     *
     * @return the create refered nodeRef
     */
    @Auditable(
            /*key = Auditable.Key.ARG_0, */ parameters = {
            "referer",
            "referedFile",
            "encoding",
            "mimetype"
    })
    NodeRef attach(
            final NodeRef referer, final File referedFile, final String encoding, final String mimetype);

    /**
     * Create the refered node (refered) in an hidden folder, and attach it to an existiong nodeRef
     * (referer)
     *
     * @return the create refered nodeRef
     */
    @Auditable(
            /*key = Auditable.Key.ARG_0, */ parameters = {
            "referer",
            "referedIs",
            "name",
            "encoding",
            "mimetype"
    })
    NodeRef attach(
            final NodeRef referer,
            final InputStream referedIs,
            final String name,
            final String encoding,
            final String mimetype);

    /**
     * Attach an existing nodeRef (refered) to another one (referer)
     *
     * @return always the refered nodeRef
     * @throws DuplicateChildNodeNameException If the node is already attached
     */
    @Auditable(/*key = Auditable.Key.ARG_0, */ parameters = {"referer", "refered"})
    NodeRef attach(final NodeRef referer, final NodeRef refered)
            throws DuplicateChildNodeNameException;

    /**
     * Get all attachements of a referer
     */
    List<NodeRef> getAttachements(final NodeRef referer);

    /**
     * get if a node is an hidden attachement
     */
    boolean isHiddenAttachement(final NodeRef refered);
}
