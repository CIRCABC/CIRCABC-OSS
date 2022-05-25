package eu.cec.digit.circabc.action.evaluator;

import eu.cec.digit.circabc.CircabcConfig;
import org.alfresco.web.action.ActionEvaluator;
import org.alfresco.web.bean.repository.Node;

public class CircabcENTEvaluator implements ActionEvaluator {

    /**
     *
     */
    private static final long serialVersionUID = -8204797722059321417L;

    @Override
    public boolean evaluate(Node node) {
        return CircabcConfig.ENT;
    }

    @Override
    public boolean evaluate(Object obj) {
        return CircabcConfig.ENT;
    }

}
