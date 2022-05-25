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
package eu.cec.digit.circabc.service.customisation.mail;

import org.alfresco.service.cmr.repository.NodeRef;

import java.util.Locale;
import java.util.Map;

/**
 * The base mail wrapper
 *
 * @author yanick pignot
 */
public interface MailWrapper {

    /**
     * @return the name of the template
     */
    String getName();

    /**
     * @return the subject of the mail by applying the given model
     */
    String getSubject(final Map<String, Object> model);

    /**
     * @return the subject of the mail by applying the given model
     */
    String getSubject(final Map<String, Object> model, final Locale language);

    /**
     * @return the body of the mail by applying the given model
     */
    String getBody(final Map<String, Object> model);

    /**
     * @return the body of the mail by applying the given model
     */
    String getBody(final Map<String, Object> model, final Locale language);

    /**
     * return if this mail is the default one
     */
    boolean isOriginalTemplate();

    /**
     * @return the template type of the mail.
     */
    MailTemplate getMailTemplate();

    /**
     * @return the nodereference of the mail template
     */
    NodeRef getTemplateNodeRef();
}
