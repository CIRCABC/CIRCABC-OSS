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
package eu.cec.digit.circabc.config;

import eu.cec.digit.circabc.util.CommonUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

/**
 * Get the preferences and server configuration.
 *
 * @author Yanick Pignot
 */
public class CircabcConfiguration {

    /**
     * The server specific configuration file
     */
//  Migration 3.1 -> 3.4.6 - 09/01/2012 - Replaced configuration location by alfresco-global.properties
//  public static final String DEFAULT_PROPERTY_FILE = "alfresco/extension/config/circabc-settings.properties";
    public static final String DEFAULT_PROPERTY_FILE = "alfresco-global.properties";
    /**
     * Global properties file
     **/
    public static final String GLOBAL_ALFRESCO_PROPERTY_FILE = "alfresco/alfresco-global.properties";
    /**
     * The non-server specific configuration file
     */
    public static final String GLOBAL_PROPERTY_FILE = "alfresco/extension/config/circabc-global-settings.properties";

    /**
     * The name of the CIRCABC root node
     */
    public static final String CIRCABC_ROOT_NODE_NAME_PROPERTIES = "root.name";
    /**
     * The title of the CIRCABC root node
     */
    public static final String CIRCABC_ROOT_NODE_TITLE_PROPERTIES = "root.title";
    /**
     * The description of the CIRCABC root node
     */
    public static final String CIRCABC_ROOT_NODE_DESCRIPTION_PROPERTIES = "root.title";
    /**
     * The name of a common library node
     */
    public static final String LIBRARY_NODE_NAME_PROPERTIES = "library.name";
    /**
     * The title of a common library node
     */
    public static final String LIBRARY_NODE_TITLE_PROPERTIES = "library.title";
    /**
     * The description of a common library node
     */
    public static final String LIBRARY_NODE_DESCRIPTION_PROPERTIES = "library.description";
    /**
     * The name of a common newsgroup node
     */
    public static final String NEWSGROUP_NODE_NAME_PROPERTIES = "newsgroup.name";
    /**
     * The title of a common newsgroup node
     */
    public static final String NEWSGROUP_NODE_TITLE_PROPERTIES = "newsgroup.title";
    /**
     * The description of a common newsgroup node
     */
    public static final String NEWSGROUP_NODE_DESCRIPTION_PROPERTIES = "newsgroup.description";
    /**
     * The name of a common survey node
     */
    public static final String SURVEY_NODE_NAME_PROPERTIES = "survey.name";
    /**
     * The title of a common survey node
     */
    public static final String SURVEY_NODE_TITLE_PROPERTIES = "survey.title";
    /**
     * The description of a common survey node
     */
    public static final String SURVEY_NODE_DESCRIPTION_PROPERTIES = "survey.description";
    /**
     * The name of a common information node
     */
    public static final String INFORMATION_NODE_NAME_PROPERTIES = "information.name";
    /**
     * The title of a common information node
     */
    public static final String INFORMATION_NODE_TITLE_PROPERTIES = "information.title";
    /**
     * The description of a information survey node
     */
    public static final String INFORMATION_NODE_DESCRIPTION_PROPERTIES = "information.description";
    /**
     * The name of a common event node
     */
    public static final String EVENT_NODE_NAME_PROPERTIES = "event.name";
    /**
     * The title of a common event node
     */
    public static final String EVENT_NODE_TITLE_PROPERTIES = "event.title";
    /**
     * The description of a common event node
     */
    public static final String EVENT_NODE_DESCRIPTION_PROPERTIES = "event.description";
    /**
     * The name of the mandatory root category header
     */
    public static final String ROOT_HEADER_NAME_PROPERTIES = "category.header.root.node.name";
    /**
     * The description of the mandatory root category header
     */
    public static final String ROOT_HEADER_DESCRIPTION_PROPERTIES = "category.header.root.node.description";
    /**
     * The name of the circabc admin created at the startup. <b>Should only used at the bootstrat !!
     * </b>
     */
    public static final String ROOT_ADMIN_NAME_PROPERTIES = "root.admin.name";
    /**
     * The password of the circabc admin created at the startup. <b>Should only used at the bootstrat
     * !! </b>
     */
    public static final String ROOT_ADMIN_MAIL_PROPERTIES = "root.admin.mail";
    /**
     * The password of the circabc admin created at the startup. <b>Should only used at the bootstrat
     * !! </b>
     */
    public static final String ROOT_ADMIN_PWD_PROPERTIES = "root.admin.password";
    /**
     * The number of days in which a self registration will expire
     */
    public static final String EXPIRATION_TIME_PROPERTIES = "selfregistration.expiration.day";
    /**
     * The number of element that must be dispaleyed in a common rich list (list of spaces, content,
     * what's new elements, ...)
     */
    public static final String DEFAULT_LIST_ELEMENT_NUMBER_PROPERTIES = "wai.browse.defaultpagesize";
    /**
     * Attachment size allowed for posts
     */
    public static final String POST_ALLOWED_ATTACHMENT_SIZE_BYTES = "posts.allowed.attachment.size.bytes";
    /**
     * Attachment size allowed for logos (category, IG, avatar, etc.)
     */
    public static final String LOGO_ALLOWED_SIZE_BYTES = "logo.allowed.size.bytes";

    /**
     * The name of the machine translation root node
     */
    public static final String MT_ROOT_NODE_NAME_PROPERTIES = "mt.root.space";

    public static final String CONTEXT_INITIAL_CONTEXT_FACTORY = "context.initial_context_factory";
    public static final String CONTEXT_PROVIDER_URL = "context.provider_url";
    public static final String CONTEXT_SECURITY_AUTHENTICATION = "context.security_authentication";
    public static final String CONTEXT_SECURITY_PRINCIPAL = "context.security_principal";
    public static final String CONTEXT_SECURITY_CREDENTIALS = "context.security_credentials";
    public static final String CONTEXT_PROVIDER_URL_ECAS = "context.provider_url_ecas";

    public static final String EMAIL_PROTOCOL = "email_protocol";
    public static final String EMAIL_SERVER = "email_server";
    public static final String EMAIL_SERVER_PORT = "email_server_port";
    public static final String EMAIL_BOX = "email_box";
    public static final String EMAIL_USERNAME = "email_username";
    public static final String EMAIL_PASSWORD = "email_password";
    public static final String EMAIL_ADDRESS = "email_address";
    public static final String WEB_ROOT_URL = "web.root.url";
    public static final String IS_EMAIL_LISTENER_ACTIVE = "is_email_listener_active";
    public static final String IS_EMAIL_USE_TLS = "is_email_use_tls";

    public static final String IS_DISPLAY_SERVER_INFORMATION_ACTIVATE = "is_display_server_information_active";
    public static final String URL_OF_THE_SERVER = "url_of_the_server";

    public static final String MIGRATION_HOST = "migration_host";

    public static final String BULK_USER_IMPORT_DEPARTMENT_NUMBER_STATUS = "bulk.user.import.department.source";
    public static final String MT_USER = "mt.user";
    public static final String MT_PASSWORD = "mt.password";
    public static final String MT_APPLICATION_NAME = "mt.application.name";
    public static final String MT_SERVICE_URL = "mt.service.url";
    public static final String MT_CALLBACK_URL = "mt.callback.url";
    public static final String MT_REST_URL = "mt.rest.url";
    public static final String MT_REST_USERNAME = "mt.rest.username";
    public static final String MT_REST_PASSWORD = "mt.rest.password";

    public static final String BUILD_RELEASE = "build.circabc.release";
    public static final String BANNER_CONTACT_LINK = "banner.contact.link";
    public static final String BUILD_APPLICATION_PATH = "build.circabc.app.path";
    public static final String COMPONENT_INVITATION_NOTIFY_DIRECTORY_ADMINS = "component.invitation.notify.directoryAdmins";
    public static final String AUTO_UPLOAD_ENABLED = "app.autoupload.enabled";
    public static final String DOC_PREVIEW_RENDER_HOST = "document.preview.render.host";
    public static final String MT_ENABLED = "mt.enabled";
    public static final String ENCRYPTED_PROPERTIES_ENABLED = "encrypted.properties.enabled";
    public static final String POSTS_SORTING_ENABLED = "posts.sorting.enabled";
    public static final String MAIL_FROM_DEFAULT = "mail.from.default";
    /*
     * Piwik configuration
     */
    public static final String PIWIK_ENABLED = "piwik.enabled";
    public static final String PIWIK_SITE_ID = "piwik.conf.siteID";
    public static final String PIWIK_SITE_PATH = "piwik.conf.sitePath";
    public static final String PIWIK_INSTANCE = "piwik.conf.instance";
    public static final String SWITCH_NEW_UI_LINK = "switch.new.ui.link";
    public static final String NEW_UI_CONTEXT = "new.ui.context";
    public static final String NEW_UI_URL = "new.ui.url";
    private static final String BUILD_APPLICATION_NAME = "build.circabc.app.name";
    private static final String APP_NAME = "appName";
    private static final String APP_URL = "appUrl";
    /**
     * The cached list of properties.
     */
    private static Properties properties = null;

    static {
        if (properties == null) {
            try {
                properties = getProperties(GLOBAL_PROPERTY_FILE);
                properties.putAll(getProperties(GLOBAL_ALFRESCO_PROPERTY_FILE));
                properties.putAll(getProperties(DEFAULT_PROPERTY_FILE));
            } catch (final IOException e) {
                throw new IllegalStateException(
                        "Impossible to read the main circabc config file " +
                                "(should be " + DEFAULT_PROPERTY_FILE + ", " +
                                GLOBAL_ALFRESCO_PROPERTY_FILE + " and " +
                                GLOBAL_PROPERTY_FILE +
                                "). Please check the server configuration.", e);
            }


        }
    }

    private CircabcConfiguration() {

    }

    /**
     * Gets Properties from a classpath name
     *
     * @param name The classpath name of the properties file
     * @return the properties
     * @throws IOException if it is unable to read the file
     */
    public static Properties getProperties(final String name) throws IOException {
        final Properties props = new Properties();
        final CommonUtils cu = new CommonUtils();
        final InputStream is = cu.getClass().getClassLoader().getResourceAsStream(
                name);

        if (is != null) {
            try {
                props.load(is);
            } finally {
                is.close();
            }
        }

        return props;
    }

    /**
     * Gets the default Properties for CIRCABC
     *
     * @return the properties
     * @throws IOException if it is unable to read the file
     */
    public static Properties getProperties() {

        return properties;
    }

    public static String getProperty(final String key) {
        return getProperties().getProperty(key);
    }

    public static void addApplicationNameToModel(Map<String, Object> model) {
        if (!model.containsKey(APP_NAME)) {
            model.put(APP_NAME, getApplicationName());
        }
        if (!model.containsKey(APP_URL)) {
            model.put(APP_URL, getApplicationUrl());
        }
    }

    public static void addApplicationNameToParams(Map<String, String> params) {
        if (!params.containsKey(APP_NAME)) {
            params.put(APP_NAME, getApplicationName());
        }
    }

    public static String getApplicationName() {
        return getProperty(BUILD_APPLICATION_NAME);
    }

    public static String getApplicationUrl() {
        return getProperty(WEB_ROOT_URL);
    }

    public static String getSwitchNewUiLink() {
        return getProperty(SWITCH_NEW_UI_LINK);
    }


}
