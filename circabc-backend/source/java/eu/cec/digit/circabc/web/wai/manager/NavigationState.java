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
package eu.cec.digit.circabc.web.wai.manager;

import eu.cec.digit.circabc.web.wai.bean.navigation.WaiNavigator;
import org.alfresco.web.bean.repository.Node;

import java.io.Serializable;


/**
 * Bean that manage the navigation beans. These beans must be an instance of WaiNavigator
 *
 * @author yanick pignot
 * @see eu.cec.digit.circabc.web.wai.bean.navigation.WaiNavigator
 */
public class NavigationState implements Serializable {

    private static final long serialVersionUID = -6703095379617913245L;

    private WaiNavigator bean;
    private Node node;

    /**
     * @param page
     * @param currentBean
     * @param currentNode
     */
    public NavigationState(WaiNavigator currentBean, Node currentNode) {
        super();
        this.bean = currentBean;
        this.node = currentNode;

        currentBean.init(null);
    }

    /**
     * @return the bean
     */
    public WaiNavigator getBean() {
        return bean;
    }

    /**
     * @return the node
     */
    public Node getNode() {
        return node;
    }

    @Override
    public String toString() {
        return "" + this.bean.getClass().getSimpleName() + ":" + (node == null ? "null" : node.getId());
    }

    @Override
    public boolean equals(Object obj) {
        if (super.equals(obj)) {
            return true;
        } else if (obj != null && obj instanceof NavigationState) {
            return obj.toString().equals(this.toString());
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return this.toString().hashCode();
    }

}
