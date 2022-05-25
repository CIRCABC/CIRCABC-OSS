package eu.cec.digit.circabc.action.evaluator;

import eu.cec.digit.circabc.CircabcConfig;
import org.alfresco.web.action.ActionEvaluator;
import org.alfresco.web.bean.repository.Node;

public class CircabcOSSEvaluator implements ActionEvaluator {

    /**
     *
     */
    private static final long serialVersionUID = 8447820182904144401L;

    @Override
    public boolean evaluate(Node node) {

        return CircabcConfig.OSS;
    }

    @Override
    public boolean evaluate(Object obj) {
        return CircabcConfig.OSS;
    }

}
