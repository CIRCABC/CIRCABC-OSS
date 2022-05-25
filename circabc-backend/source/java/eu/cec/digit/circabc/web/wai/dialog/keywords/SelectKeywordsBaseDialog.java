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
package eu.cec.digit.circabc.web.wai.dialog.keywords;

import eu.cec.digit.circabc.service.keyword.Keyword;
import eu.cec.digit.circabc.web.comparator.SelectItemLabelComparator;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.web.bean.repository.Node;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.faces.component.UISelectMany;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import javax.faces.model.SelectItem;
import java.util.*;


/**
 * Bean that backs a generic Keyword Selector WAI Dialog.
 *
 * @author Yanick Pignot
 */
public abstract class SelectKeywordsBaseDialog extends ManageKeywordsDialog {

    private static final long serialVersionUID = 7766640743571143699L;

    /**
     * Logger
     */
    private static final Log logger = LogFactory.getLog(SelectKeywordsBaseDialog.class);

    protected Map<NodeRef, Keyword> settedKeywords;
    protected Map<NodeRef, Keyword> interestGroupKeywords;
    protected transient DataModel settedKeywordsDataModel = null;

    @Override
    public void init(Map<String, String> parameters) {
        super.init(parameters);

        if (parameters != null) {
            initKeywords();
        }
    }

    protected abstract String finishImpl(FacesContext context, String outcome) throws Exception;

    public abstract Node getInterestGroup();

    public abstract String getDialogCloseAndLaunchAction();

    /**
     * Action handler called when the Add button is pressed to process the current selection
     */
    public void addSelection(ActionEvent event) {
        UISelectMany picker = (UISelectMany) event.getComponent()
                .findComponent("set-keywords-list");

        if (picker == null) {
            throw new NullPointerException(
                    "No select many component found with the ID set-keywords-list");
        }

        final Object[] results = picker.getSelectedValues();

        // add the selected keyxord(s) to the document.
        for (final Object result : results) {
            final NodeRef keyId = new NodeRef((String) result);
            settedKeywords.put(keyId, interestGroupKeywords.get(keyId));
        }

        // apply changes to the datamodel
        setDataModel();
    }

    /**
     * Action handler called when the Remove button is pressed to remove a keyword translation
     */
    public void removeSelection(ActionEvent event) {

        final Keyword wrapper = (Keyword) this.settedKeywordsDataModel.getRowData();
        if (wrapper != null) {
            this.settedKeywords.remove(wrapper.getId());
        }

        // apply changes to the datamodel
        setDataModel();
    }

    /**
     * @return the keywords
     */
    public SelectItem[] getInterestGroupKeywords() {
        final List<SelectItem> itemAsList = new ArrayList<>();
        final Locale filteredLanguage =
                (getSelectedLanguage() == null || getSelectedLanguage().equals(NULL_VALUE)) ? null
                        : new Locale(getSelectedLanguage());

        if (filteredLanguage == null) {
            // simply add all keywords
            for (final Map.Entry<NodeRef, Keyword> entry : interestGroupKeywords.entrySet()) {
                // don't return keywords that is already setted to the document
                if (!settedKeywords.containsKey(entry.getKey())) {
                    itemAsList.add(new SelectItem(entry.getKey().toString(), entry.getValue().toString()));
                }
            }
        } else {
            for (final Map.Entry<NodeRef, Keyword> entry : interestGroupKeywords.entrySet()) {
                if (entry.getValue().isKeywordTranslated()) {
                    // don't return keywords that is already setted to the document
                    if (!settedKeywords.containsKey(entry.getKey())) {
                        final MLText values = entry.getValue().getMLValues();
                        if (values.containsKey(filteredLanguage)) {
                            itemAsList
                                    .add(new SelectItem(entry.getKey().toString(), values.get(filteredLanguage)));
                        }
                    }
                } else {
                    // not ml keyword not added
                }
            }
        }

        Collections.sort(itemAsList, new SelectItemLabelComparator());
        return itemAsList.toArray(new SelectItem[itemAsList.size()]);
    }


    /**
     * @return the keywords
     */
    public List<KeywordWrapper> getSettedKeywords() {
        final List<KeywordWrapper> keys = new ArrayList<>(settedKeywords.size());

        for (final Map.Entry<NodeRef, Keyword> entry : settedKeywords.entrySet()) {
            keys.add(new KeywordWrapper(entry.getKey(), entry.getValue().toString()));
        }

        return keys;
    }

    /**
     * Returns the properties for current document keywords JSF DataModel
     *
     * @return JSF DataModel representing the document keywords
     */
    public DataModel getSettedKeywordsDataModel() {
        if (this.settedKeywordsDataModel == null) {
            this.settedKeywordsDataModel = new ListDataModel();
            setDataModel();
        }
        return this.settedKeywordsDataModel;
    }

    @Override
    protected void initKeywords() {
        settedKeywordsDataModel = null;

        super.initKeywords();

        //fill the map of all available keywords for the interest group
        interestGroupKeywords = new HashMap<>(keywords.size());
        for (final Keyword keyword : keywords) {
            interestGroupKeywords.put(keyword.getId(), keyword);
        }

        if (getActionNode() != null) {
            if (logger.isDebugEnabled()) {
                logger.debug(
                        "Trying to retreive the keyword list for the current document : " + getActionNode()
                                .getName() + "(" + getActionNode().getNodeRef() + ")");
            }

            // get the keywords assigned to the document and fill the map
            final List<Keyword> docKeywordsList = getKeywordsService()
                    .getKeywordsForNode(getActionNode().getNodeRef());

            settedKeywords = new HashMap<>(docKeywordsList.size());
            for (final Keyword keyword : docKeywordsList) {
                settedKeywords.put(keyword.getId(), keyword);
            }

            if (logger.isDebugEnabled()) {
                logger.debug(
                        "Keyword successfully retreived for the document : " + getActionNode().getName() + "("
                                + getActionNode().getNodeRef() + ")"
                                + "\n    All Keywords" + docKeywordsList);
            }
        } else {
            settedKeywords = null;
        }

    }

    protected void setDataModel() {
        final List<Keyword> docKeywordsAsList = new ArrayList<>(settedKeywords.size());
        docKeywordsAsList.addAll(settedKeywords.values());
        getSettedKeywordsDataModel().setWrappedData(docKeywordsAsList);

    }
}
