package eu.cec.digit.circabc.action.evaluator;

import org.alfresco.model.ContentModel;
import org.alfresco.web.action.evaluator.BaseActionEvaluator;
import org.alfresco.web.bean.repository.Node;

public class ConfigureAutoUploadEvaluator extends BaseActionEvaluator {

    /**
     *
     */
    private static final long serialVersionUID = 7548517978606575803L;

    public boolean evaluate(final Node node) {
        return !node.isLocked() && !node.hasAspect(ContentModel.ASPECT_WORKING_COPY);
    }
}
