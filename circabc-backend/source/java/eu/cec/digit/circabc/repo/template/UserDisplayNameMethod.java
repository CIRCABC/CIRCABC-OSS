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
package eu.cec.digit.circabc.repo.template;

import freemarker.ext.beans.BeanModel;
import freemarker.template.SimpleScalar;
import freemarker.template.TemplateBooleanModel;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.template.BaseTemplateProcessorExtension;
import org.alfresco.repo.template.TemplateNode;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;

import java.io.Serializable;
import java.util.List;

/**
 * Freemarker method to display the full name (first + last) of an user
 *
 * @author Yanick Pignot
 */
public class UserDisplayNameMethod extends BaseTemplateProcessorExtension
        implements TemplateMethodModelEx {

    private ServiceRegistry services;

    /**
     * @see freemarker.template.TemplateMethodModel#exec(java.util.List)
     */
    public Object exec(List args) throws TemplateModelException {
        String result = "";

        if (args.size() > 0) {
            boolean addEmail = false;
            final Object arg0 = args.get(0);

            if (args.size() > 1) {
                final Object arg1 = args.get(1);

                if (arg1 instanceof TemplateBooleanModel) {
                    addEmail = ((TemplateBooleanModel) arg1).getAsBoolean();
                }
            }

            if (arg0 instanceof BeanModel) {
                final BeanModel arg0BeanModel = (BeanModel) args.get(0);

                if (arg0BeanModel.getWrappedObject() instanceof TemplateNode) {
                    final NodeRef user = ((TemplateNode) arg0BeanModel.getWrappedObject()).getNodeRef();
                    result = getUserFullName(user, addEmail);
                }
            } else if (arg0 instanceof SimpleScalar) {
                final String userName = ((SimpleScalar) arg0).getAsString();

                if (services.getPersonService().personExists(userName)) {
                    result = getUserFullName(services.getPersonService().getPerson(userName), addEmail);
                } else {
                    result = userName;
                }
            }
        }

        return result;
    }

    protected String getUserFullName(NodeRef user, Boolean addEmail) {
        String result = "";

        final Serializable firstName =
                services.getNodeService().getProperty(user, ContentModel.PROP_FIRSTNAME);
        final Serializable lastName =
                services.getNodeService().getProperty(user, ContentModel.PROP_LASTNAME);
        final Serializable email =
                addEmail ? services.getNodeService().getProperty(user, ContentModel.PROP_EMAIL) : null;
        if (firstName == null && lastName == null) {
            final Serializable username =
                    services.getNodeService().getProperty(user, ContentModel.PROP_USERNAME);
            if (username != null) {
                result = username.toString();
            }
        } else {
            result =
                    (firstName == null ? "" : firstName.toString() + " ")
                            + (lastName == null ? "" : lastName.toString())
                            + (email == null ? "" : " (" + email + ")");
        }

        return result;
    }

    /**
     * @return the services
     */
    protected final ServiceRegistry getServiceRegistry() {
        return services;
    }

    /**
     * @param services the services to set
     */
    public final void setServiceRegistry(ServiceRegistry services) {
        this.services = services;
    }
}
