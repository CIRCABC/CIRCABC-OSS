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
package eu.cec.digit.circabc.web.bean.navigation;

import eu.cec.digit.circabc.aspect.AbstractAspect.ComparatorType;
import eu.cec.digit.circabc.model.DossierModel;
import eu.cec.digit.circabc.service.CircabcServiceRegistry;
import eu.cec.digit.circabc.web.Services;
import eu.cec.digit.circabc.web.wai.bean.navigation.*;
import eu.cec.digit.circabc.web.wai.bean.navigation.event.AppointmentDetailsBean;
import eu.cec.digit.circabc.web.wai.bean.navigation.inf.InfContentDetailsBean;
import eu.cec.digit.circabc.web.wai.bean.navigation.inf.InfFileLinkDetailsBean;
import eu.cec.digit.circabc.web.wai.bean.navigation.library.*;
import eu.cec.digit.circabc.web.wai.bean.navigation.news.ForumBean;
import eu.cec.digit.circabc.web.wai.bean.navigation.news.PostBean;
import eu.cec.digit.circabc.web.wai.bean.navigation.news.TopicBean;
import org.alfresco.model.ApplicationModel;
import org.alfresco.model.ContentModel;
import org.alfresco.model.ForumModel;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.bean.repository.Node;

import javax.faces.context.FacesContext;

import static eu.cec.digit.circabc.model.CircabcModel.*;
import static eu.cec.digit.circabc.model.EventModel.TYPE_EVENT;

/**
 * Enumeration of each specifc kind of node managed by Circabc.
 *
 * @author yanick pignot
 * @author Stephane Clinckart
 */
public enum NavigableNodeType {
    NOT_CIRCABC_CHILD(ComparatorType.NOT_ASPECT, ASPECT_CIRCABC_MANAGEMENT, null),
    CIRCABC_CHILD(ComparatorType.ASPECT, ASPECT_CIRCABC_MANAGEMENT, null),

    LIBRARY_CHILD(CIRCABC_CHILD, ComparatorType.ASPECT, ASPECT_LIBRARY, LibraryBean.BEAN_NAME),
    NEWSGROUP_CHILD(CIRCABC_CHILD, ComparatorType.ASPECT, ASPECT_NEWSGROUP, NewsGroupBean.BEAN_NAME),
    SURVEY_CHILD(CIRCABC_CHILD, ComparatorType.ASPECT, ASPECT_SURVEY, SurveyHomeBean.BEAN_NAME),
    EVENT_CHILD(CIRCABC_CHILD, ComparatorType.ASPECT, ASPECT_EVENT, EventBean.BEAN_NAME),
    INFORMATION_CHILD(CIRCABC_CHILD, ComparatorType.ASPECT, ASPECT_INFORMATION,
            InformationBean.BEAN_NAME),

    CIRCABC_ROOT(CIRCABC_CHILD, ComparatorType.ASPECT, ASPECT_CIRCABC_ROOT, WelcomeBean.BEAN_NAME),
    CATEGORY_HEADER(ComparatorType.TYPE, TYPE_CATEGORY_HEADER, CategoryHeadersBean.BEAN_NAME),
    CATEGORY(CIRCABC_CHILD, ComparatorType.ASPECT, ASPECT_CATEGORY, CategoryBean.BEAN_NAME),
    IG_ROOT(CIRCABC_CHILD, ComparatorType.ASPECT, ASPECT_IGROOT, InterestGroupBean.BEAN_NAME),
    LIBRARY(LIBRARY_CHILD, ComparatorType.ASPECT, ASPECT_LIBRARY_ROOT, LibraryBean.BEAN_NAME),
    NEWSGROUP(NEWSGROUP_CHILD, ComparatorType.ASPECT, ASPECT_NEWSGROUP_ROOT, NewsGroupBean.BEAN_NAME),
    DIRECTORY(CIRCABC_CHILD, ComparatorType.TYPE, TYPE_DIRECTORY_SERVICE, DirectoryBean.BEAN_NAME),
    SURVEY(SURVEY_CHILD, ComparatorType.ASPECT, ASPECT_SURVEY_ROOT, SurveyHomeBean.BEAN_NAME),
    EVENT(EVENT_CHILD, ComparatorType.ASPECT, ASPECT_EVENT_ROOT, EventBean.BEAN_NAME),
    INFORMATION(INFORMATION_CHILD, ComparatorType.ASPECT, ASPECT_INFORMATION_ROOT,
            InformationBean.BEAN_NAME),

    LIBRARY_SPACE(LIBRARY_CHILD, ComparatorType.TYPE, ContentModel.TYPE_FOLDER,
            LibraryBean.BEAN_NAME),
    LIBRARY_DOSSIER(LIBRARY_CHILD, ComparatorType.TYPE, DossierModel.TYPE_DOSSIER_SPACE,
            DossierBean.BEAN_NAME),
    LIBRARY_CONTENT(LIBRARY_CHILD, ComparatorType.TYPE, ContentModel.TYPE_CONTENT,
            ContentDetailsBean.BEAN_NAME),
    LIBRARY_EMPTY_TRANSLATION(LIBRARY_CHILD, ComparatorType.ASPECT,
            ContentModel.ASPECT_MULTILINGUAL_EMPTY_TRANSLATION, ContentDetailsBean.BEAN_NAME),
    LIBRARY_FILE_LINK(LIBRARY_CHILD, ComparatorType.TYPE, ApplicationModel.TYPE_FILELINK,
            FileLinkDetailsBean.BEAN_NAME),
    LIBRARY_FORUM(LIBRARY_CHILD, ComparatorType.TYPE, ForumModel.TYPE_FORUM,
            DiscussionForumBean.BEAN_NAME),
    LIBRARY_TOPIC(LIBRARY_CHILD, ComparatorType.TYPE, ForumModel.TYPE_TOPIC,
            DiscussionTopicBean.BEAN_NAME),
    LIBRARY_POST(LIBRARY_CHILD, ComparatorType.TYPE, ForumModel.TYPE_POST,
            DiscussionPostBean.BEAN_NAME),

    LIBRARY_ML_CONTENT(ComparatorType.TYPE, ContentModel.TYPE_MULTILINGUAL_CONTAINER,
            MLContentDetailsBean.BEAN_NAME),
    LIBRARY_ML_CONTENT_TOPIC(ComparatorType.TYPE, ForumModel.TYPE_TOPIC,
            DiscussionTopicBean.BEAN_NAME),
    LIBRARY_ML_CONTENT_FORUM(ComparatorType.TYPE, ForumModel.TYPE_FORUM,
            DiscussionForumBean.BEAN_NAME),
    LIBRARY_ML_CONTENT_POST(ComparatorType.TYPE, ForumModel.TYPE_POST, DiscussionPostBean.BEAN_NAME),

    SURVEY_ELEMENT(SURVEY_CHILD, ComparatorType.ASPECT, ASPECT_SURVEY, SurveyHomeBean.BEAN_NAME),
    SURVEY_SPACE(SURVEY_CHILD, ComparatorType.TYPE, ContentModel.TYPE_FOLDER,
            SurveyHomeBean.BEAN_NAME),

    NEWSGROUP_FORUM(NEWSGROUP_CHILD, ComparatorType.TYPE, ForumModel.TYPE_FORUM, ForumBean.BEAN_NAME),
    NEWSGROUP_TOPIC(NEWSGROUP_CHILD, ComparatorType.TYPE, ForumModel.TYPE_TOPIC, TopicBean.BEAN_NAME),
    NEWSGROUP_POST(NEWSGROUP_CHILD, ComparatorType.TYPE, ForumModel.TYPE_POST, PostBean.BEAN_NAME),

    INFORMATION_SPACE(INFORMATION_CHILD, ComparatorType.TYPE, ContentModel.TYPE_FOLDER,
            InformationBean.BEAN_NAME),
    INFORMATION_CONTENT(INFORMATION_CHILD, ComparatorType.TYPE, ContentModel.TYPE_CONTENT,
            InfContentDetailsBean.BEAN_NAME),
    INFORMATION_FILE_LINK(INFORMATION_CHILD, ComparatorType.TYPE, ApplicationModel.TYPE_FILELINK,
            InfFileLinkDetailsBean.BEAN_NAME),

    EVENT_APPOINTMENT(EVENT_CHILD, ComparatorType.TYPE, TYPE_EVENT, AppointmentDetailsBean.BEAN_NAME);


    private static final String THE_LOGIC_SHOULDN_T_ALLOW_TO_EXECUTE_THIS_CODE = "The logic shouldn't allow to execute this code.";
    private final ComparatorType comparatorType;
    private final QName comparatorQName;
    private final NavigableNodeType requireNodeType;
    private final String beanName;
    private DictionaryService dictionaryService;
    private CircabcServiceRegistry circabcServiceRegistry;

    NavigableNodeType(final ComparatorType comprator, final QName comparatorQName,
                      final String beanName) {
        this(null, comprator, comparatorQName, beanName);
    }

    NavigableNodeType(final NavigableNodeType requireNodeType, final ComparatorType comprator,
                      final QName comparatorQName, final String beanName) {
        this.requireNodeType = requireNodeType;
        this.comparatorType = comprator;
        this.comparatorQName = comparatorQName;
        this.beanName = beanName;
    }

    public boolean isNodeFromType(final Node node) {
        return isNodeFromType(node, false);
    }

    /*package */boolean isNodeFromType(final Node node, final boolean resovleParent) {
        if (node == null) {
            return false;
        } else if (resovleParent == false || requireNodeType == null || requireNodeType
                .isNodeFromType(node, resovleParent)) {
            switch (comparatorType) {
                case ASPECT:
                    return node.hasAspect(comparatorQName);

                case TYPE:

                    final QName type = node.getType();

                    if (comparatorQName.equals(type)) {
                        return true;
                    } else {
                        final TypeDefinition typeDef = getDictionaryService().getType(type);

                        if (typeDef != null) {
                            if (comparatorQName.equals(type) || getDictionaryService()
                                    .isSubClass(type, comparatorQName)) {
                                return true;
                            }
                        }
                    }

                    return false;

                default:
                    throw new IllegalStateException(THE_LOGIC_SHOULDN_T_ALLOW_TO_EXECUTE_THIS_CODE);
            }
        } else {
            return false;
        }
    }

    public boolean isStrictlyUnder(final NavigableNodeType otherType) {
        if (otherType.equals(this)) {
            return false;
        } else {
            return isEqualsOrUnderImpl(otherType);
        }
    }

    public boolean isEqualsOrUnder(final NavigableNodeType otherType) {
        if (otherType.equals(this)) {
            return true;
        } else {
            return isEqualsOrUnderImpl(otherType);
        }
    }

    private boolean isEqualsOrUnderImpl(final NavigableNodeType otherType) {
        boolean response = false;
        //boolean under = false;
        if (otherType.equals(CIRCABC_CHILD)) {
            response = true;
        } else {
            switch (otherType) {
                case CIRCABC_ROOT:
                    response = true;
                    break;
                case CATEGORY_HEADER:
                    response = !this.equals(CIRCABC_ROOT);
                    break;
                case CATEGORY:
                    response = !this.equals(CIRCABC_ROOT) && !this.equals(CATEGORY_HEADER);
                    break;
                case IG_ROOT:
                    response =
                            !this.equals(CIRCABC_ROOT) && !this.equals(CATEGORY_HEADER) && !this.equals(CATEGORY);
                    break;
                case LIBRARY:
                    response = this.equals(LIBRARY_CHILD)
                            || (this.getRequireNodeType() != null && this.getRequireNodeType()
                            .equals(LIBRARY_CHILD))
                            || this.equals(LIBRARY_ML_CONTENT) || this.equals(LIBRARY_ML_CONTENT_FORUM)
                            || this.equals(LIBRARY_ML_CONTENT_TOPIC) || this.equals(LIBRARY_ML_CONTENT_POST);
                    break;
                case NEWSGROUP:
                    response = this.equals(NEWSGROUP_CHILD) || (this.getRequireNodeType() != null && this
                            .getRequireNodeType().equals(NEWSGROUP_CHILD));
                    break;
                case SURVEY:
                    response = this.equals(SURVEY_CHILD) || (this.getRequireNodeType() != null && this
                            .getRequireNodeType().equals(SURVEY_CHILD));
                    break;
                case INFORMATION:
                    response = this.equals(INFORMATION_CHILD) || (this.getRequireNodeType() != null && this
                            .getRequireNodeType().equals(INFORMATION_CHILD));
                    break;
                case EVENT:
                    response = this.equals(EVENT_CHILD) || (this.getRequireNodeType() != null && this
                            .getRequireNodeType().equals(EVENT_CHILD));
                    break;
                case DIRECTORY:
                    response = this.equals(DIRECTORY);
                    break;
                default:
                    break;
            }
        }

        return response;
    }

    public boolean isIGServiceOrAbove() {
        boolean response = false;

        switch (this) {
            case CIRCABC_ROOT:
                response = true;
                break;
            case CATEGORY_HEADER:
                response = true;
                break;
            case CATEGORY:
                response = true;
                break;
            case IG_ROOT:
                response = true;
                break;
            case LIBRARY:
                response = true;
                break;
            case NEWSGROUP:
                response = true;
                break;
            case SURVEY:
                response = true;
                break;
            case DIRECTORY:
                response = true;
                break;
            case EVENT:
                response = true;
                break;
            case INFORMATION:
                response = true;
                break;
            default:
                break;
        }

        return response;
    }

    public NavigableNodeType getRelatedIgService() {
        NavigableNodeType service = null;

        switch (this) {
            case CIRCABC_ROOT:
                break;
            case CATEGORY_HEADER:
                break;
            case CATEGORY:
                break;
            case IG_ROOT:
                break;
            case LIBRARY:
                service = LIBRARY;
                break;
            case NEWSGROUP:
                service = NEWSGROUP;
                break;
            case SURVEY:
                service = SURVEY;
                break;
            case DIRECTORY:
                service = DIRECTORY;
                break;
            default:
                if (isEqualsOrUnderImpl(LIBRARY)) {
                    service = LIBRARY;
                } else if (isEqualsOrUnderImpl(NEWSGROUP)) {
                    service = NEWSGROUP;
                } else if (isEqualsOrUnderImpl(SURVEY)) {
                    service = SURVEY;
                } else if (isEqualsOrUnderImpl(DIRECTORY)) {
                    service = DIRECTORY;
                } else if (isEqualsOrUnderImpl(EVENT)) {
                    service = EVENT;
                } else if (isEqualsOrUnderImpl(INFORMATION)) {
                    service = INFORMATION;
                }
        }

        return service;
    }

    /**
     * @return the comparatorQName
     */
    public QName getComparatorQName() {
        return comparatorQName;
    }

    /**
     * @return the comparatorType
     */
    public ComparatorType getComparatorType() {
        return comparatorType;
    }

    /**
     * @return the requireNodeType
     */
    public NavigableNodeType getRequireNodeType() {
        return requireNodeType;
    }

    /**
     * @return the beanName
     */
    public String getBeanName() {
        return beanName;
    }

    protected DictionaryService getDictionaryService() {
        if (this.dictionaryService == null) {
            //this.dictionaryService = Services.getAlfrescoServiceRegistry(FacesContext.getCurrentInstance()).getDictionaryService();
            this.dictionaryService = getCircabcServiceRegistry().getNonSecuredDictionaryService();
        }

        return this.dictionaryService;
    }

    protected CircabcServiceRegistry getCircabcServiceRegistry() {
        if (this.circabcServiceRegistry == null) {
            this.circabcServiceRegistry = Services
                    .getCircabcServiceRegistry(FacesContext.getCurrentInstance());
        }

        return this.circabcServiceRegistry;
    }
}
