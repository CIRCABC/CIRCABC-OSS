/**
 *
 */
package eu.cec.digit.circabc.service.customisation;

import eu.cec.digit.circabc.error.CircabcRuntimeException;
import org.alfresco.service.cmr.repository.NodeRef;

import java.io.File;
import java.util.List;

/** @author beaurpi */
public interface ApplicationCustomisationService {

    NodeRef getDefaultLogoNodeRef();

    void updateDefaultLogo(File gif) throws CircabcRuntimeException;

    List<NodeRef> getListOfTemplates();

    void updateTemplate(File tempFile, NodeRef templateRef) throws CircabcRuntimeException;

    NodeRef getDefaultLogoDisclaimerNodeRef();

    void updateDefaultLogoDisclaimer(File gif) throws CircabcRuntimeException;

    void updateContactLink(String link);

    String getContactlink();

    NodeRef getBannerLogoRef();

    void updateBannerLogoRef(File png);

    void removeBannerLogoRef();

    Boolean getDisplaySearchLink();

    void setDisplaySearchLink(Boolean b);

    Boolean getDisplayLegalLink();

    void setDisplayLegalLink(Boolean b);

    String geteLearningLink();

    void seteLearningLink(String s);

    Boolean getDisplayeLearningLink();

    void setDisplayeLearningLink(Boolean b);

    String getErrorMessageContent();

    void setErrorMessageContent(String s);
}
