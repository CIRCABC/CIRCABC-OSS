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
package eu.cec.digit.circabc.web;

import eu.cec.digit.circabc.business.api.props.PropertiesBusinessSrv;
import eu.cec.digit.circabc.web.bean.override.CircabcUserPreferencesBean;
import eu.cec.digit.circabc.web.servlet.ExternalAccessServlet;
import eu.cec.digit.circabc.web.wai.bean.navigation.CategoryItem;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.util.ISO9075;
import org.alfresco.util.ParameterCheck;
import org.alfresco.web.app.Application;
import org.alfresco.web.app.servlet.DownloadContentServlet;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.bean.repository.User;
import org.alfresco.web.ui.common.Utils;
import org.alfresco.web.ui.common.Utils.URLMode;
import org.springframework.extensions.surf.util.I18NUtil;

import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.servlet.http.HttpSession;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Utilities to get informations about the Circabc environment.
 *
 * @author Matthieu Sprunck
 * @author Guillaume
 */
public class WebClientHelper {

    public static final String MSG_DATE_TIME_PATTERN = "date_time_pattern";
    public static final String MSG_DATE_PATTERN = "date_pattern";
    public static final String MSG_TIME_PATTERN = "time_pattern";
    private static final String FIRST_ACCESS_PAGE = "firstAccessPage";
    private static final String IS_LANGUAGE_IN_SYNC = "isLanguageInSync";
    private static final List<SelectItem> unmodifiableExportList;

    static {
        ArrayList<SelectItem> exportTypes = new ArrayList<>(3);
        exportTypes.add(new SelectItem(ExportTypeEnum.CSV.toString(), ExportTypeEnum.CSV.toString()));
        exportTypes.add(new SelectItem(ExportTypeEnum.XML.toString(), ExportTypeEnum.XML.toString()));
        exportTypes
                .add(new SelectItem(ExportTypeEnum.Excel.toString(), ExportTypeEnum.Excel.toString()));
        unmodifiableExportList = Collections.unmodifiableList(exportTypes);
    }

    /**
     * Helper to return the nodeRef from a Path
     *
     * @return NodeRef
     */
    public static NodeRef getNodeFromPath(final String path, final SearchService searchService) {
        final List<NodeRef> nodes = getNodesFromPath(path, searchService);
        if (nodes.size() == 1) {
            return nodes.get(0);
        }
        return null;
    }

    /**
     * Helper to return the nodeRef from a Path
     *
     * @return NodeRef
     */
    public static List<NodeRef> getNodesFromPath(final String path,
                                                 final SearchService searchService) {
        final StoreRef storeRef = new StoreRef(StoreRef.PROTOCOL_WORKSPACE, "SpacesStore");
        ResultSet resultSet = null;
        final List<NodeRef> tempList = new ArrayList<>();
        try {
            resultSet = searchService.query(storeRef, SearchService.LANGUAGE_LUCENE, path);
            tempList.addAll(resultSet.getNodeRefs());
        } finally {
            if (resultSet != null) {
                resultSet.close();
            }
        }

        return tempList;
    }

    /**
     * Clone function of org.alfresco.web.bean.SearchContext.getPathFromSpaceRef(NodeRef ref, boolean
     * children
     *
     * @param ref      The NodeRef to get the path
     * @param children If we also want the children (recursive)
     * @return The Path string of the nodeRef
     * @see org.alfresco.web.bean.SearchContext#getPathFromSpaceRef(org.alfresco.service.cmr.repository.NodeRef,
     * boolean)
     */
    public static String getPathFromSpaceRef(final NodeRef ref, boolean children) {
        final FacesContext context = FacesContext.getCurrentInstance();
        final Path path = Repository.getServiceRegistry(context).getNodeService().getPath(ref);
        final NamespaceService ns = Repository.getServiceRegistry(context).getNamespaceService();
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
                    prefixes = ns.getPrefixes(elementRef.getQName().getNamespaceURI());
                    if (prefixes.size() > 0) {
                        elementString = '/' + (String) prefixes.iterator().next() + ':' + ISO9075
                                .encode(elementRef.getQName().getLocalName());
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

    public static String toValidFileName(final String fileName) {
        final PropertiesBusinessSrv propertiesBusinessSrv = Services
                .getBusinessRegistry(FacesContext.getCurrentInstance()).getPropertiesBusinessSrv();

        return propertiesBusinessSrv.computeValidName(fileName);
    }

    public static String generateUniqueName(final NodeService nodeService, final NodeRef parent,
                                            final String candidateName) {
        //
        if (nodeService.getChildByName(parent, ContentModel.ASSOC_CONTAINS, candidateName) == null) {
            return candidateName;
        } else {
            final String extension = WebClientHelper.getFileNameExtension(candidateName);
            final String name = WebClientHelper.removeFileNameExtension(candidateName);
            String uniqueName;
            int tries = 0;

            do {
                uniqueName = name + " (" + (++tries) + ")" + ((extension == null) ? "" : "." + extension);
            }
            while (nodeService.getChildByName(parent, ContentModel.ASSOC_CONTAINS, uniqueName) != null);

            return uniqueName;
        }
    }

    public static String getFileNameExtension(final String fileName) {
        int extIndex = fileName.lastIndexOf('.');
        if (extIndex != -1) {
            return fileName.substring(extIndex + 1).toLowerCase();
        } else {
            return null;
        }
    }

    public static String removeFileNameExtension(final String fileName) {
        int extIndex = fileName.lastIndexOf('.');
        if (extIndex != -1) {
            return fileName.substring(0, extIndex).toLowerCase();
        } else {
            return fileName;
        }
    }

    public static List<SelectItem> getExportedTypes() {
        return unmodifiableExportList;
    }

    public static String getGeneratedWaiRelativeUrl(final Node node, final ExtendedURLMode mode) {
        return getGeneratedWaiUrl(node, mode, true);

    }

    public static String getGeneratedWaiFullUrl(final Node node, final ExtendedURLMode mode) {
        return getGeneratedWaiUrl(node, mode, false);
    }

    public static String getGeneratedWaiFullUrl(final NodeRef nodeRef, final ExtendedURLMode mode) {
        ParameterCheck.mandatory("NodeRef", nodeRef);

        return getGeneratedWaiUrl(new Node(nodeRef), mode, false);
    }

    public static String getGeneratedWaiUrl(final NodeRef nodeRef, final ExtendedURLMode mode,
                                            final boolean relative) {
        ParameterCheck.mandatory("NodeRef", nodeRef);

        return getGeneratedWaiUrl(new Node(nodeRef), mode, relative);
    }

    public static String getGeneratedWaiUrl(final Node node, final ExtendedURLMode mode,
                                            final boolean relative) {
        ParameterCheck.mandatory("Node", node);
        ParameterCheck.mandatory("Mode", mode);

        String url = "";
        final FacesContext context = FacesContext.getCurrentInstance();

        switch (mode) {
            case HTTP_WAI_BROWSE:
                if (relative) {
                    url = ExternalAccessServlet
                            .generateRelativeWaiExternalURL(ExternalAccessServlet.OUTCOME_BROWSE, node.getId());
                } else {
                    url = ExternalAccessServlet
                            .generateFullWaiExternalURL(ExternalAccessServlet.OUTCOME_BROWSE, node.getId());
                }
                break;
            case HTTP_USERDETAILS:
                if (relative) {
                    url = ExternalAccessServlet
                            .generateRelativeWaiExternalURL(ExternalAccessServlet.OUTCOME_USER_DETAILS,
                                    node.getId());
                } else {
                    url = ExternalAccessServlet
                            .generateFullWaiExternalURL(ExternalAccessServlet.OUTCOME_USER_DETAILS, node.getId());
                }
                break;
            case HTTP_NATIVE_BROWSE:
                if (!relative) {
                    url = ExternalAccessServlet.getServerContext();
                }
                url += Utils.generateURL(context, node, URLMode.BROWSE);
                break;
            case WEBDAV:
                if (!relative) {
                    url = ExternalAccessServlet.getServerContext();
                }
                url += Utils.generateURL(context, node, URLMode.WEBDAV);
                break;
            case HTTP_DOWNLOAD:
                if (!relative) {
                    url = ExternalAccessServlet.getServerContext();
                }
                url += Utils.generateURL(context, node, URLMode.HTTP_DOWNLOAD);
                break;
            case HTTP_INLINE:
                if (!relative) {
                    url = ExternalAccessServlet.getServerContext();
                }
                url += Utils.generateURL(context, node, URLMode.HTTP_INLINE);
                break;
        }

        return url;

    }

    public static String getGeneratedWaiUrl(final CategoryItem categoryItem,
                                            final ExtendedURLMode mode, final boolean relative) {
        ParameterCheck.mandatory("CategoryItem", categoryItem);
        ParameterCheck.mandatory("Mode", mode);

        String url = "";
        final FacesContext context = FacesContext.getCurrentInstance();

        switch (mode) {
            case HTTP_WAI_BROWSE:
                if (relative) {
                    url = ExternalAccessServlet
                            .generateRelativeWaiExternalURL(ExternalAccessServlet.OUTCOME_BROWSE,
                                    categoryItem.getId());
                } else {
                    url = ExternalAccessServlet
                            .generateFullWaiExternalURL(ExternalAccessServlet.OUTCOME_BROWSE,
                                    categoryItem.getId());
                }
                break;
            case HTTP_INLINE:
                if (!relative) {
                    url = ExternalAccessServlet.getServerContext();
                }
                url += DownloadContentServlet
                        .generateBrowserURL(categoryItem.getNodeRef(), categoryItem.getName());
                break;
        }

        return url;

    }

    private static String generateURLInline(FacesContext context,
                                            CategoryItem categoryItem, URLMode httpInline) {
        // TODO Auto-generated method stub
        return null;
    }

    public static String formatLocalizedDate(final Date dateToFormat, final FacesContext fc,
                                             boolean date, boolean time) {
        if (!date && !time) {
            return null;
        } else {
            final String pattern;

            if (date && time) {
                pattern = Application.getMessage(fc, MSG_DATE_TIME_PATTERN);
            } else if (date) {
                pattern = Application.getMessage(fc, MSG_DATE_PATTERN);
            } else // time
            {
                pattern = Application.getMessage(fc, MSG_TIME_PATTERN);
            }

            final DateFormat df = new SimpleDateFormat(pattern);
            final String formatedDate = df.format(dateToFormat);

            return formatedDate;
        }
    }

    /**
     * Synchronizes the languages of the JSF context and Alfresco. This is only valid for the current
     * session.
     *
     * @param requestLocale The Language that should be used.
     */
    public static void synchronizeLanguages(Locale requestLocale) {
        final FacesContext fc = FacesContext.getCurrentInstance();

        User currentUser = Application.getCurrentUser(fc);

        if (currentUser == null || currentUser.getUserName()
                .equals(AuthenticationUtil.getGuestUserName())) {
            final Locale guestLocal = Application.getLanguage(fc);
            if (isFirstAccess(fc)) {
                fc.getViewRoot().setLocale(requestLocale);
                Application.setLanguage(fc, requestLocale.getLanguage());

            } else if (guestLocal != null) {
                fc.getViewRoot().setLocale(guestLocal);
                Application.setLanguage(fc, guestLocal.getLanguage());
            } else {
                fc.getViewRoot().setLocale(requestLocale);
                Application.setLanguage(fc, requestLocale.getLanguage());
            }
        } else {

            CircabcUserPreferencesBean userPreferencesBean = Beans.getUserPreferencesBean();
            String userLanguage = userPreferencesBean.getLanguage();
            Locale locale = I18NUtil.parseLocale(userLanguage);
            I18NUtil.setLocale(locale);
            fc.getViewRoot().setLocale(locale);
            Application.setLanguage(fc, userLanguage);
        }
    }

    private static boolean isFirstAccess(FacesContext fc) {

        HttpSession session = (HttpSession) fc.getExternalContext().getSession(false);
        Boolean isFirstAccessPage = (Boolean) session.getAttribute(FIRST_ACCESS_PAGE);
        boolean result = (isFirstAccessPage == null);
        session.setAttribute(FIRST_ACCESS_PAGE, Boolean.FALSE);
        return result;
    }

    public static String translate(final String key, final Object... params) {
        final FacesContext fc = FacesContext.getCurrentInstance();

        //translate the string using Alfresco
        if (params == null || params.length < 1) {
            return Application.getMessage(fc, key);
        } else {
            return MessageFormat.format(Application.getMessage(fc, key), params);
        }
    }

    public static String getCurrentGmt() {
        return toGmt(TimeZone.getDefault());
    }

    public static String toGmt(final TimeZone tz) {
        final int offset = tz.getRawOffset();
        final int offsetInHour = offset / (1000 * 60 * 60);

        final int signum = Integer.signum(offsetInHour);

        final StringBuilder sb = new StringBuilder("GMT");

        if (signum == -1) {
            // don't need to add explicit '-'
            sb.append(offsetInHour);
        } else if (signum == +1) {
            sb.append('+').append(offsetInHour);
        }
        // else means 0 ... returns GMT

        return sb.toString();
    }

    /**
     * @return the Circabc Url
     */
    public static String getCircabcUrl() {
        return ExternalAccessServlet.getServerContext();
    }

    public static String emptyStringIfNull(Serializable serializable) {
        if (serializable == null) {
            return "";
        } else {
            return (String) serializable;
        }
    }

    public enum ExtendedURLMode {
        HTTP_WAI_BROWSE,
        HTTP_NATIVE_BROWSE,
        WEBDAV,
        HTTP_DOWNLOAD,
        HTTP_USERDETAILS,
        HTTP_INLINE
    }

    public enum ExportTypeEnum {
        CSV, XML, Excel
    }
}
