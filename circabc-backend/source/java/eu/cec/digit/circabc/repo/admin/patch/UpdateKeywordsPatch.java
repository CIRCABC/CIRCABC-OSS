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
package eu.cec.digit.circabc.repo.admin.patch;

import eu.cec.digit.circabc.model.DocumentModel;
import eu.cec.digit.circabc.service.struct.ManagementService;
import org.alfresco.jcr.item.NodeImpl;
import org.alfresco.repo.domain.node.NodePropertyValue;
import org.alfresco.repo.domain.qname.QNameEntity;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.web.bean.repository.Node;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Applicatif patch that convert old keywords properties to new one
 *
 * @author yanick pignot
 */
public class UpdateKeywordsPatch // extends BaseReindexingPatch
{

    /**
     * Logger
     */
    private static final Log logger = LogFactory.getLog(UpdateKeywordsPatch.class);

    private static final String LUCENE_QUERY = "@cd\\:keyword:*";

    private int updatedCount = 0;

    private ManagementService managementService;
    private DictionaryService dictionaryService;
    private SessionFactory sessionFactory;

    private HibernateHelper helper;

    public UpdateKeywordsPatch() {
        helper = new HibernateHelper();
    }

    //	@Override
    protected String applyInternal() throws Exception {
        updatedCount = 0;

        final NodeRef circabc = managementService.getCircabcNodeRef();

        if (circabc != null) {

            helper.setSessionFactory(getSessionFactory());

            updatedCount = helper.fixKeywordvalues();

            //			super.reindex(LUCENE_QUERY, circabc.getStoreRef());

        }

        return "Keyword properties successfully updated" + "\n\tDocument updated: " + updatedCount;
    }

    /**
     * @return the dictionaryService
     */
    protected final DictionaryService getDictionaryService() {
        return dictionaryService;
    }

    /**
     * @param dictionaryService the dictionaryService to set
     */
    public final void setDictionaryService(DictionaryService dictionaryService) {
        this.dictionaryService = dictionaryService;
    }

    /**
     * @return the managementService
     */
    protected final ManagementService getManagementService() {
        return managementService;
    }

    /**
     * @param managementService the managementService to set
     */
    public final void setManagementService(ManagementService managementService) {
        this.managementService = managementService;
    }

    /**
     * @return the sessionFactory
     */
    public final SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    /**
     * @param sessionFactory the sessionFactory to set
     */
    public final void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    private static class HibernateHelper extends HibernateDaoSupport {

        private static final String HBM_SELECT_QNAME =
                " select qname from "
                        + QNameEntity.class.getName()
                        + " as qname "
                        + " join qname.namespace as ns "
                        + " where qname.localName = :localName"
                        + " and ns.safeUri = :uri";

        private static final String HBM_SELECT_NODES =
                "select node from "
                        + NodeImpl.class.getName()
                        + " as node "
                        + " join node.properties prop "
                        + " where index(prop) = :qnameId";

        private boolean isNodeRefOrListOf(NodePropertyValue value) {
            Serializable persistedValue = value.getStringValue();

            if (persistedValue == null) {
                persistedValue = value.getSerializableValue();
            }

            if (persistedValue == null || persistedValue instanceof Map) {
                return false;
            } else if (persistedValue instanceof Collection) {
                Collection col = (Collection) persistedValue;

                if (col.size() == 0) {
                    return false;
                } else {
                    return NodeRef.isNodeRef(col.toArray()[0].toString());
                }
            } else {
                return NodeRef.isNodeRef(persistedValue.toString());
            }
        }

        public int fixKeywordvalues() {

            HibernateCallback callback =
                    new HibernateCallback() {
                        @SuppressWarnings("unchecked")
                        public Object doInHibernate(Session session) {
                            final Query qnameQuery = session.createQuery(HBM_SELECT_QNAME);
                            qnameQuery.setString("localName", DocumentModel.PROP_KEYWORD.getLocalName());
                            qnameQuery.setString("uri", DocumentModel.PROP_KEYWORD.getNamespaceURI());

                            final QNameEntity qnameEntity = (QNameEntity) qnameQuery.uniqueResult();

                            int modifiedElement = 0;

                            if (qnameEntity != null) {
                                final long qnameId = qnameEntity.getId();
                                final Query nodesQuery = session.createQuery(HBM_SELECT_NODES);
                                nodesQuery.setLong("qnameId", qnameId);

                                final List<Node> nodes = nodesQuery.list();

                                if (UpdateKeywordsPatch.logger.isDebugEnabled()) {
                                    UpdateKeywordsPatch.logger.debug("");
                                }

                                String qnameIDString = Long.toString(qnameId);
                                for (Node node : nodes) {
                                    final NodePropertyValue value =
                                            (NodePropertyValue) node.getProperties().get(qnameIDString);
                                    if (value != null && !isNodeRefOrListOf(value)) {
                                        NodePropertyValue newPropertyValue =
                                                new NodePropertyValue(
                                                        DataTypeDefinition.NODE_REF, (Serializable) new ArrayList<NodeRef>());
                                        //	                    		 PropertyMapKey pmk = new PropertyMapKey();
                                        //	                    		 pmk.setQnameId(qnameId);
                                        node.getProperties().put(qnameIDString, newPropertyValue);
                                        modifiedElement++;

                                        if (UpdateKeywordsPatch.logger.isDebugEnabled()) {
                                            UpdateKeywordsPatch.logger.debug(
                                                    "  --  The keyword of the node "
                                                            + node.getNodeRef()
                                                            + " has been updated "
                                                            + "\n\tOld value "
                                                            + value
                                                            + "\n\tNew value "
                                                            + newPropertyValue);
                                        }
                                    }
                                }
                            }

                            return modifiedElement;
                        }
                    };

            if (UpdateKeywordsPatch.logger.isDebugEnabled()) {
                UpdateKeywordsPatch.logger.debug("Trying to check for the keyword properties .... ");
            }

            Integer updateCount = (Integer) getHibernateTemplate().execute(callback);

            if (UpdateKeywordsPatch.logger.isDebugEnabled()) {
                UpdateKeywordsPatch.logger.debug(
                        updateCount + " keyword was badly setted... The model is now respected.");
            }

            // done
            return updateCount;
        }
    }
}
