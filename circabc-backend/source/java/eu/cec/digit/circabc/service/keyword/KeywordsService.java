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
package eu.cec.digit.circabc.service.keyword;

import org.alfresco.service.Auditable;
import org.alfresco.service.NotAuditable;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;

import java.util.List;

/**
 * Interface for keywords operations. A keyword is a set of nodes that can be setted to documents.
 * This keywords are setted for a specified interest group, and can be multilingual or not. The
 * non-multilingual keywords <u>should only</u> be added at the migration time.
 *
 * <p>The keywords are stored as nodes and the values of them are stored in the title property and
 * not in the name. Each keyword node parent must be a keyword container that sould be an unique
 * child of a Interst Group Root node.
 *
 * @author Matthieu Sprunck
 * @author Yanick Pignot
 * <p>Migration 3.1 -> 3.4.6 - 02/12/2011 Commented the key parameter of the @Auditable
 * annotation. Commented the deprecated @PublicService annotation.
 */
// @PublicService
public interface KeywordsService {

    /**
     * Add a single keyword to a document
     *
     * @param document The node's reference
     * @param keywords The collection of keywords
     */
    @Auditable(/*key = Auditable.Key.ARG_0, */ parameters = {"document", "keyword"})
    void addKeywordToNode(final NodeRef document, final Keyword keyword);

    /**
     * Build a keyword representation with a given Id
     *
     * @param id as NodeRef
     * @throws InvalidNodeRefException if the id is invalid
     */
    @NotAuditable
    Keyword buildKeywordWithId(final NodeRef id) throws InvalidNodeRefException;

    /**
     * Build a keyword representation with a given Id
     *
     * @param id as String <i>(NodeRef.toString)</i>
     * @throws InvalidNodeRefException if the id is invalid
     */
    @NotAuditable
    Keyword buildKeywordWithId(final String id) throws InvalidNodeRefException;

    /**
     * Add a keyword to an interest group
     *
     * @param ig      The ig's node reference
     * @param keyword The keyword to add
     */
    @Auditable(/*key = Auditable.Key.ARG_0, */ parameters = {"ig", "keyword"})
    Keyword createKeyword(final NodeRef ig, final Keyword keyword);

    /**
     * Test if the given keyword exists
     *
     * @return if the keyword already exists
     */
    @NotAuditable
    boolean exists(final Keyword keyword);

    /**
     * Gets the list of keywords for an interest group.
     *
     * @param ig The ig's node reference
     * @return a collection of {@link NodeRef}
     */
    @Auditable(/*key = Auditable.Key.ARG_0, */ parameters = {"ig"})
    List<Keyword> getKeywords(final NodeRef ig);

    /**
     * Gets a collection of keywords defined to a document
     *
     * @param document The node's reference
     * @return a collection of Keyword
     */
    @Auditable(/*key = Auditable.Key.ARG_0, */ parameters = {"document"})
    List<Keyword> getKeywordsForNode(final NodeRef document);

    /**
     * Gets a collection of documents defined having a given keyword
     *
     * @param the keyword to search
     * @return a collection of Document
     */
    @Auditable(/*key = Auditable.Key.ARG_0, */ parameters = {"parent", "keyword"})
    List<NodeRef> getNodesForKeywords(final NodeRef parent, final List<Keyword> keyword);

    /**
     * Return true is the keyword is set as being multiligual.
     *
     * @return if the keyword is multilingual or not.
     */
    @NotAuditable
    public boolean isKeywordMultilingual(final Keyword keyword);

    /**
     * Removes an existing keyword
     *
     * @param keyword The keyword to remove
     */
    @Auditable(/*key = Auditable.Key.ARG_0, */ parameters = {"keyword"})
    void removeKeyword(final Keyword keyword);

    /**
     * Sets a collection of keywords to a node
     *
     * @param document The node's reference
     * @param keywords The collection of keywords
     */
    @Auditable(/*key = Auditable.Key.ARG_0, */ parameters = {"document", "keywords"})
    void setKeywordsToNode(final NodeRef document, final List<Keyword> keywords);

    /**
     * Set a keyword multilingual and set its translations. <b>This method will erase each existing
     * values</b>
     *
     * @param keyword   the keyword to set multilingual
     * @param allValues the translated values of the keyword
     */
    @Auditable(/*key = Auditable.Key.ARG_0, */ parameters = {"keyword"})
    void updateKeyword(final Keyword keyword);
}
